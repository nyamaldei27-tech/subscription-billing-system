package com.example.demo.service;

import com.example.demo.client.AccountServiceClient;
import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.SubscriptionResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.InvoiceResponse;
import com.example.demo.dto.PaymentAttemptResponse;
import com.example.demo.kafka.BillingEvent;
import com.example.demo.repository.SubscriptionActivityRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import com.example.demo.kafka.BillingEventProducer;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final AccountServiceClient accountServiceClient;
    private final BillingEventProducer billingEventProducer;
    private final SubscriptionActivityRepository subscriptionActivityRepository;
    private final  ReceiptPdfService receiptPdfService;
    private final MinioStorageService minioStorageService;

    public BillingService(
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            AccountServiceClient accountServiceClient,
            BillingEventProducer billingEventProducer,
            SubscriptionActivityRepository subscriptionActivityRepository,
            ReceiptPdfService receiptPdfService,
            MinioStorageService minioStorageService) {

        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.accountServiceClient = accountServiceClient;
        this.billingEventProducer = billingEventProducer;
        this.subscriptionActivityRepository = subscriptionActivityRepository;
        this.receiptPdfService = receiptPdfService;
        this.minioStorageService = minioStorageService;
    }

    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================
    @Transactional
    public SubscriptionResponse createSubscription(
            Long customerId,
            Long planId,
            Authentication authentication) {

        verifyCustomerOwnership(customerId, authentication);

        // Customer belongs to Account Service.
        // Billing Service does NOT access a CustomerRepository.
        if (!accountServiceClient.customerExists(customerId,
                authentication )) {
            throw new ResourceNotFoundException(
                    "Customer " + customerId + " does not exist."
            );
        }

        // A customer can have only one ACTIVE subscription.
        boolean alreadyActive =
                subscriptionRepository
                        .findByCustomerId(customerId)
                        .stream()
                        .anyMatch(subscription ->
                                "ACTIVE".equals(subscription.getStatus())
                        );

        if (alreadyActive) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer already has an active subscription."
            );
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan " + planId + " does not exist."
                ));

        Subscription subscription = new Subscription();

        // Billing stores only the ID of the customer.
        subscription.setCustomerId(customerId);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");

        LocalDateTime now = LocalDateTime.now();

        String billingCycle = plan.getBillingCycle()
                .toUpperCase(Locale.ROOT);

        LocalDateTime periodEnd = switch (billingCycle) {
            case "WEEKLY" -> now.plusWeeks(1);
            case "MONTHLY" -> now.plusMonths(1);
            case "YEARLY" -> now.plusYears(1);
            default -> throw new IllegalStateException(
                    "Unsupported billing cycle: " + billingCycle
            );
        };

        subscription.setCurrentPeriodEnd(periodEnd);

        Subscription savedSubscription =
                subscriptionRepository.save(subscription);

        // Create the first invoice automatically.
        Invoice invoice = new Invoice();

        invoice.setSubscription(savedSubscription);
        invoice.setAmountCents(plan.getPriceCents());
        invoice.setStatus("PENDING");
        invoice.setDueDate(now.plusDays(7));

        invoiceRepository.save(invoice);

        BillingEvent event = new BillingEvent(
                "SUBSCRIPTION_CREATED",
                savedSubscription.getId(),
                savedSubscription.getCustomerId(),
                savedSubscription.getPlan().getId(),
                LocalDateTime.now()
        );

        billingEventProducer.sendEvent(event);

        return toSubscriptionResponse(savedSubscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {

        return subscriptionRepository.findAll()
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(Long id,
                                                    Authentication authentication) {

        Subscription subscription =
                subscriptionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Subscription " + id + " does not exist."
                        ));
        verifyCustomerOwnership(
                subscription.getCustomerId(),
                authentication
        );

        return toSubscriptionResponse(subscription);
    }
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByCustomerId(
            Long customerId,
            Authentication authentication) {

        verifyCustomerOwnership(customerId, authentication);

        return subscriptionRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id,
                                                   Authentication authentication) {

        Subscription subscription =
                subscriptionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Subscription " + id + " does not exist."
                        ));
        verifyCustomerOwnership(
                subscription.getCustomerId(),
                authentication
        );

        if ("CANCELED".equalsIgnoreCase(subscription.getStatus())) {
            throw new IllegalStateException(
                    "Subscription " + id + " is already canceled."
            );
        }

        subscription.setStatus("CANCELED");

        Subscription savedSubscription =
                subscriptionRepository.save(subscription);

        BillingEvent event = new BillingEvent(
                "SUBSCRIPTION_CANCELED",
                subscription.getId(),
                subscription.getCustomerId(),
                subscription.getPlan().getId(),
                LocalDateTime.now()
        );

        billingEventProducer.sendEvent(event);

        return toSubscriptionResponse(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse changeSubscriptionPlan(
            Long subscriptionId,
            Long planId,
            Authentication authentication) {

        Subscription subscription =
                subscriptionRepository.findById(subscriptionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Subscription "
                                                + subscriptionId
                                                + " does not exist."
                                ));
        verifyCustomerOwnership(
                subscription.getCustomerId(),
                authentication
        );

        if ("CANCELED".equalsIgnoreCase(
                subscription.getStatus())) {

            throw new IllegalStateException(
                    "Cannot change the plan of a canceled subscription."
            );
        }

        Plan newPlan =
                planRepository.findById(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan "
                                                + planId
                                                + " does not exist."
                                ));

        subscription.setPlan(newPlan);

        LocalDateTime now = LocalDateTime.now();

        String billingCycle =
                newPlan.getBillingCycle()
                        .toUpperCase(Locale.ROOT);

        LocalDateTime periodEnd = switch (billingCycle) {
            case "WEEKLY" ->
                    now.plusWeeks(1);

            case "MONTHLY" ->
                    now.plusMonths(1);

            case "YEARLY" ->
                    now.plusYears(1);

            default ->
                    throw new IllegalStateException(
                            "Unsupported billing cycle: "
                                    + billingCycle
                    );
        };

        subscription.setCurrentPeriodEnd(periodEnd);

        Subscription savedSubscription =
                subscriptionRepository.save(subscription);

        BillingEvent event = new BillingEvent(
                "SUBSCRIPTION_PLAN_CHANGED",
                savedSubscription.getId(),
                savedSubscription.getCustomerId(),
                savedSubscription.getPlan().getId(),
                LocalDateTime.now()
        );

        billingEventProducer.sendEvent(event);

        return toSubscriptionResponse(
                savedSubscription
        );
    }

    // =========================================================
    // PAYMENTS
    // =========================================================
    @Transactional
    public InvoiceResponse processPayment(
            Long invoiceId,
            String paymentStatus,
            Authentication authentication) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + invoiceId + " does not exist."
                ));

        Subscription subscription = invoice.getSubscription();

        verifyCustomerOwnership(
                subscription.getCustomerId(),
                authentication
        );

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setInvoice(invoice);
        attempt.setStatus(paymentStatus);

        paymentAttemptRepository.save(attempt);

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

            invoice.setStatus("PAID");
            invoice.setPaidAt(LocalDateTime.now());

            if ("PAST_DUE".equalsIgnoreCase(subscription.getStatus())) {
                subscription.setStatus("ACTIVE");
            }

        } else {

            invoice.setStatus("FAILED");
            subscription.setStatus("PAST_DUE");
        }

        subscriptionRepository.save(subscription);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

            byte[] receiptPdf =
                    receiptPdfService.generateReceipt(savedInvoice);

            String objectName =
                    "receipts/invoice-"
                            + savedInvoice.getId()
                            + ".pdf";

            minioStorageService.upload(
                    objectName,
                    receiptPdf,
                    "application/pdf"
            );
            savedInvoice.setReceiptObjectName(objectName);

            invoiceRepository.save(savedInvoice);

            String receiptUrl =
                    minioStorageService.getPresignedUrl(objectName);

            BillingEvent event = new BillingEvent(
                    "PAYMENT_SUCCEEDED",
                    subscription.getId(),
                    subscription.getCustomerId(),
                    subscription.getPlan().getId(),
                    LocalDateTime.now()
            );

            billingEventProducer.sendEvent(event);

            InvoiceResponse response =
                    toInvoiceResponse(savedInvoice);

            response.setReceiptUrl(receiptUrl);

            return response;

        } else {

            BillingEvent event = new BillingEvent(
                    "PAYMENT_FAILED",
                    subscription.getId(),
                    subscription.getCustomerId(),
                    subscription.getPlan().getId(),
                    LocalDateTime.now()
            );

            billingEventProducer.sendEvent(event);

            return toInvoiceResponse(savedInvoice);
        }
    }

    // =========================================================
    // INVOICES
    // =========================================================

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {

        return invoiceRepository.findAll()
                .stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id,
                                          Authentication authentication) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + id + " does not exist."
                ));
        verifyCustomerOwnership(
                invoice.getSubscription().getCustomerId(),
                authentication
        );

        return toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomerId(Long customerId,
                                                         Authentication authentication) {
        verifyCustomerOwnership(customerId, authentication);

        return invoiceRepository
                .findBySubscriptionCustomerId(customerId)
                .stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    // =========================================================
    // PAYMENT ATTEMPTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentAttemptResponse> getAllPaymentAttempts() {

        return paymentAttemptRepository.findAll()
                .stream()
                .map(this::toPaymentAttemptResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentAttemptResponse> getPaymentAttemptsByCustomerId(
            Long customerId,
            Authentication authentication) {

        verifyCustomerOwnership(customerId, authentication);

        return paymentAttemptRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::toPaymentAttemptResponse)
                .toList();
    }

    public List<SubscriptionActivity> getSubscriptionActivitiesByCustomerId(Long customerId, Authentication authentication) {
        verifyCustomerOwnership(customerId, authentication);
        return subscriptionActivityRepository
                .findByCustomerIdOrderByEventTimestampDesc(customerId);
    }

    // =========================================================
    // AUTOMATIC BILLING
    // =========================================================

    @Scheduled(
            initialDelay = 5000,
            fixedRate = 10000
    )
    @Transactional
    public void processDailyBilling() {

        LocalDateTime now = LocalDateTime.now();

        List<Subscription> dueSubscriptions =
                subscriptionRepository
                        .findByStatusAndCurrentPeriodEndLessThanEqual(
                                "ACTIVE",
                                now
                        );

        for (Subscription subscription : dueSubscriptions) {

            Plan plan = subscription.getPlan();

            // Create the next invoice.
            Invoice invoice = new Invoice();

            invoice.setSubscription(subscription);
            invoice.setAmountCents(plan.getPriceCents());
            invoice.setStatus("PENDING");
            invoice.setDueDate(now.plusDays(7));

            invoiceRepository.save(invoice);

            // Extend the billing period.
            String billingCycle = plan.getBillingCycle()
                    .toUpperCase(Locale.ROOT);

            LocalDateTime currentPeriodEnd =
                    subscription.getCurrentPeriodEnd();

            LocalDateTime nextPeriodEnd = switch (billingCycle) {

                case "WEEKLY" ->
                        currentPeriodEnd.plusWeeks(1);

                case "MONTHLY" ->
                        currentPeriodEnd.plusMonths(1);

                case "YEARLY" ->
                        currentPeriodEnd.plusYears(1);

                default -> throw new IllegalStateException(
                        "Unsupported billing cycle: " + billingCycle
                );
            };

            subscription.setCurrentPeriodEnd(nextPeriodEnd);

            subscriptionRepository.save(subscription);
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private SubscriptionResponse toSubscriptionResponse(
            Subscription subscription) {

        SubscriptionResponse response =
                new SubscriptionResponse();

        response.setId(subscription.getId());
        response.setCustomerId(subscription.getCustomerId());

        Plan plan = subscription.getPlan();

        response.setPlanId(plan.getId());
        response.setPlanName(plan.getName());
        response.setPriceCents(plan.getPriceCents());
        response.setBillingCycle(plan.getBillingCycle());

        response.setStatus(subscription.getStatus());
        response.setCurrentPeriodEnd(
                subscription.getCurrentPeriodEnd()
        );

        return response;
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {

        InvoiceResponse response = new InvoiceResponse();

        response.setId(invoice.getId());
        response.setAmountCents(invoice.getAmountCents());
        response.setStatus(invoice.getStatus());
        response.setDueDate(invoice.getDueDate());
        response.setPaidAt(invoice.getPaidAt());

        Subscription subscription = invoice.getSubscription();

        if (subscription != null) {
            response.setSubscriptionId(subscription.getId());
            response.setCustomerId(subscription.getCustomerId());
        }
        if (invoice.getReceiptObjectName() != null) {
            String receiptUrl =
                    minioStorageService.getPresignedUrl(
                            invoice.getReceiptObjectName()
                    );

            response.setReceiptUrl(receiptUrl);
        }

        return response;
    }

    private PaymentAttemptResponse toPaymentAttemptResponse(
            PaymentAttempt paymentAttempt) {

        PaymentAttemptResponse response =
                new PaymentAttemptResponse();

        response.setId(paymentAttempt.getId());
        response.setStatus(paymentAttempt.getStatus());
        response.setAttemptedAt(paymentAttempt.getAttemptedAt());

        Invoice invoice = paymentAttempt.getInvoice();

        if (invoice != null) {
            response.setInvoiceId(invoice.getId());
        }

        return response;
    }

    private void verifyCustomerOwnership(
            Long customerId,
            Authentication authentication) {

        CustomerResponse currentCustomer =
                accountServiceClient.getCurrentCustomer(authentication);

        if (!currentCustomer.getId().equals(customerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to access this customer's data."
            );
        }
    }
}
