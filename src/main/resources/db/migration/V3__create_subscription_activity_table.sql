CREATE TABLE subscription_activity (
                                       id BIGSERIAL PRIMARY KEY,

                                       subscription_id BIGINT NOT NULL,

                                       customer_id BIGINT NOT NULL,

                                       event_type VARCHAR(100) NOT NULL,

                                       event_timestamp TIMESTAMP NOT NULL
);