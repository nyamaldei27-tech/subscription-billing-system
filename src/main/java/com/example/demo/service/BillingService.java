package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.PlanNameAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class BillingService {


    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    // Constructor injection for all repositories
    public BillingService(CustomerRepository customerRepository,
                          PlanRepository planRepository,
                          SubscriptionRepository subscriptionRepository,
                          InvoiceRepository invoiceRepository,
                          PaymentAttemptRepository paymentAttemptRepository) {
        this.customerRepository = customerRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    /**
     * Business Logic: Creates a subscription and automatically triggers the initial invoice generation.
     * Enforced by @Transactional to ensure both happen together or not at all (ACID properties).
     */
    @Transactional
    public Subscription createSubscription(Long customerId, Long planId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer " + customerId + " does not exist."
                ));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan " + planId + " does not exist."
                ));

        // 1. Create and save the subscription
        Subscription subscription = new Subscription();
        subscription.setCustomerId(customer.getId());
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");

        // Extract cycle dynamically from the chosen Plan
        LocalDateTime now = LocalDateTime.now();
        String cycle = plan.getBillingCycle().toUpperCase(Locale.ROOT);

        LocalDateTime initialPeriodEnd = switch (cycle) {
            case "WEEKLY" -> now.plusWeeks(1);
            case "MONTHLY" -> now.plusMonths(1);
            case "YEARLY" -> now.plusYears(1);
            default -> throw new IllegalStateException(
                    "Unsupported billing cycle: " + cycle
            );
        };

        subscription.setCurrentPeriodEnd(initialPeriodEnd);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // 2. Automatically generate the initial invoice
        Invoice invoice = new Invoice();
        invoice.setSubscription(savedSubscription);
        invoice.setAmountCents(plan.getPriceCents());
        invoice.setStatus("PENDING");
        invoice.setDueDate(LocalDateTime.now().plusDays(7)); // Expires in 7 days
        invoiceRepository.save(invoice);

        return savedSubscription;
    }

    /**
     * Business Logic: Simulates paying an invoice and records the transaction attempt history.
     */
    @Transactional
    public Invoice processPayment(Long invoiceId, String paymentStatus) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + invoiceId + " does not exist."
                ));

        // 1. Record the history attempt tracking record
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setInvoice(invoice);
        attempt.setStatus(paymentStatus);
        paymentAttemptRepository.save(attempt);

        // 2. Update the invoice status based on success/failure
        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            invoice.setStatus("PAID");

            Subscription subscription = invoice.getSubscription();

            if ("PAST_DUE".equalsIgnoreCase(subscription.getStatus())) {
                subscription.setStatus("ACTIVE");
                subscriptionRepository.save(subscription);
            }

        } else {
            invoice.setStatus("FAILED");

            Subscription subscription = invoice.getSubscription();
            subscription.setStatus("PAST_DUE");
            subscriptionRepository.save(subscription);
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Plan createPlan(Plan plan) {
        if (planRepository.existsByName(plan.getName())) {
            throw new PlanNameAlreadyExistsException("A plan with this name already exists.");
        }
        return planRepository.save(plan);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new EmailAlreadyExistsException("A customer with this email already exists.");
        }
        // Capitalize the first letters on the backend as a safety backup
        if (customer.getFirstName() != null) {
            customer.setFirstName(customer.getFirstName().substring(0, 1).toUpperCase() + customer.getFirstName().substring(1));
        }
        if (customer.getLastName() != null) {
            customer.setLastName(customer.getLastName().substring(0, 1).toUpperCase() + customer.getLastName().substring(1));
        }
        if (customer.getMiddleName() != null && !customer.getMiddleName().isEmpty()) {
            customer.setMiddleName(customer.getMiddleName().substring(0, 1).toUpperCase() + customer.getMiddleName().substring(1));
        }
        return customerRepository.save(customer);
    }

    // --- Optimized Read Operations ---

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer " + id + " does not exist."
                ));
    }

    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan " + id + " does not exist."
                ));
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PaymentAttempt> getAllPaymentAttempts() {
        return paymentAttemptRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByCustomerId(Long customerId) {
        return invoiceRepository.findBySubscriptionCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription " + id + " does not exist."
                ));
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice " + id + " does not exist."
                ));
    }

    @Transactional(readOnly = true)
    public List<PaymentAttempt> getPaymentAttemptsByCustomerId(Long customerId) {
        return paymentAttemptRepository.findByInvoiceSubscriptionCustomerId(customerId);
    }

    /**
     * Daily Scheduled Job: Finds active subscriptions that reached their
     * period end date, generates a new invoice, and extends the subscription.
     * Cron format: "seconds minutes hours day-of-month month day-of-week"
     * "0 0 0 * * ?" = Runs every day at 00:00:00 (Midnight)
     */
    @Scheduled(initialDelay=5000, fixedRate =10000)
    @Transactional
    public void processDailyBilling() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch all ACTIVE subscriptions due for billing
        List<Subscription> dueSubscriptions =
                subscriptionRepository.findByStatusAndCurrentPeriodEndLessThanEqual("ACTIVE", now);

        for (Subscription subscription : dueSubscriptions) {
            // 2. Automatically generate new invoice for the subscription
            Invoice invoice = new Invoice();
            invoice.setSubscription(subscription);
            invoice.setAmountCents(subscription.getPlan().getPriceCents());
            invoice.setStatus("PENDING");
            invoice.setDueDate(now.plusDays(7)); // Invoice due in 7 days

            invoiceRepository.save(invoice);

            String cycle = subscription.getPlan().getBillingCycle().toUpperCase();
            LocalDateTime nextPeriodEnd = switch (cycle) {
                case "WEEKLY" -> subscription.getCurrentPeriodEnd().plusWeeks(1);
                case "YEARLY" -> subscription.getCurrentPeriodEnd().plusYears(1);
                default -> subscription.getCurrentPeriodEnd().plusMonths(1);
            };

            // 4. Commit the new rolling window timeline
            subscription.setCurrentPeriodEnd(nextPeriodEnd);
            subscriptionRepository.save(subscription);
        }
    }

}


