package com.example.demo.service;

import com.example.demo.client.AccountServiceClient;
import com.example.demo.dto.SubscriptionResponse;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.PaymentAttempt;
import com.example.demo.entity.Plan;
import com.example.demo.entity.Subscription;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentAttemptRepository;
import com.example.demo.repository.PlanRepository;
import com.example.demo.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.InvoiceResponse;
import com.example.demo.dto.PaymentAttemptResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class BillingService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final AccountServiceClient accountServiceClient;

    public BillingService(
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            AccountServiceClient accountServiceClient) {

        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.accountServiceClient = accountServiceClient;
    }

    // =========================================================
    // SUBSCRIPTIONS
    // =========================================================

    @Transactional
    public SubscriptionResponse createSubscription(
            Long customerId,
            Long planId) {

        // Customer belongs to Account Service.
        // Billing Service does NOT access a CustomerRepository.
        if (!accountServiceClient.customerExists(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer " + customerId + " does not exist."
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
    public SubscriptionResponse getSubscriptionById(Long id) {

        Subscription subscription =
                subscriptionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Subscription " + id + " does not exist."
                        ));

        return toSubscriptionResponse(subscription);
    }
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByCustomerId(
            Long customerId) {

        return subscriptionRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id) {

        Subscription subscription =
                subscriptionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Subscription " + id + " does not exist."
                        ));

        if ("CANCELED".equalsIgnoreCase(subscription.getStatus())) {
            throw new IllegalStateException(
                    "Subscription " + id + " is already canceled."
            );
        }

        subscription.setStatus("CANCELED");

        Subscription savedSubscription =
                subscriptionRepository.save(subscription);

        return toSubscriptionResponse(savedSubscription);
    }

    // =========================================================
    // PAYMENTS
    // =========================================================

    @Transactional
    public InvoiceResponse processPayment(
            Long invoiceId,
            String paymentStatus) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + invoiceId + " does not exist."
                ));

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setInvoice(invoice);
        attempt.setStatus(paymentStatus);

        paymentAttemptRepository.save(attempt);

        Subscription subscription = invoice.getSubscription();

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

        return toInvoiceResponse(savedInvoice);
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
    public InvoiceResponse getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + id + " does not exist."
                ));

        return toInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomerId(Long customerId) {

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
            Long customerId) {

        return paymentAttemptRepository
                .findByInvoiceSubscriptionCustomerId(customerId)
                .stream()
                .map(this::toPaymentAttemptResponse)
                .toList();
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
}
