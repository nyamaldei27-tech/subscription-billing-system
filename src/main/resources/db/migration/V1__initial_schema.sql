CREATE TABLE plans (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       price_cents BIGINT NOT NULL,
                       billing_cycle VARCHAR(50) NOT NULL,

                       CONSTRAINT chk_plan_price_non_negative
                           CHECK (price_cents >= 0),

                       CONSTRAINT chk_plan_billing_cycle
                           CHECK (billing_cycle IN ('WEEKLY', 'MONTHLY', 'YEARLY'))
);


CREATE TABLE subscriptions (
                               id BIGSERIAL PRIMARY KEY,

                               customer_id BIGINT NOT NULL,

                               plan_id BIGINT NOT NULL,

                               status VARCHAR(50) NOT NULL,

                               current_period_end TIMESTAMP NOT NULL,

                               CONSTRAINT fk_subscription_plan
                                   FOREIGN KEY (plan_id)
                                       REFERENCES plans(id),

                               CONSTRAINT chk_subscription_status
                                   CHECK (status IN ('ACTIVE', 'PAST_DUE', 'CANCELED'))
);


CREATE TABLE invoices (
                          id BIGSERIAL PRIMARY KEY,

                          subscription_id BIGINT NOT NULL,

                          amount_cents BIGINT NOT NULL,

                          status VARCHAR(50) NOT NULL,

                          due_date TIMESTAMP NOT NULL,

                          CONSTRAINT fk_invoice_subscription
                              FOREIGN KEY (subscription_id)
                                  REFERENCES subscriptions(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_invoice_amount_non_negative
                              CHECK (amount_cents >= 0),

                          CONSTRAINT chk_invoice_status
                              CHECK (status IN ('PENDING', 'PAID', 'FAILED'))
);


CREATE TABLE payment_attempts (
                                  id BIGSERIAL PRIMARY KEY,

                                  invoice_id BIGINT NOT NULL,

                                  status VARCHAR(50) NOT NULL,

                                  attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_payment_attempt_invoice
                                      FOREIGN KEY (invoice_id)
                                          REFERENCES invoices(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT chk_payment_attempt_status
                                      CHECK (status IN ('SUCCESS', 'FAILED'))
);