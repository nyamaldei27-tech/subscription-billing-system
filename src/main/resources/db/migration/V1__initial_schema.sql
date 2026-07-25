CREATE TABLE customers(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) UNIQUE NOT NULL ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plans (
    id SERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    price_Cents INTEGER NOT NULL ,
    billing_cycle VARCHAR(50)NOT NULL
);

CREATE TABLE subscriptions(
    id SERIAL PRIMARY KEY ,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE ,
    plan_id INTEGER REFERENCES plans(id),
    status VARCHAR(50) NOT NULL ,
    current_period_end TIMESTAMP NOT NULL
);

CREATE TABLE invoices(
    id SERIAL PRIMARY KEY ,
    subscription_id INTEGER REFERENCES subscriptions(id) ON DELETE CASCADE ,
    amount_Cents INTEGER NOT NULL ,
    status VARCHAR(50) NOT NULL ,
    due_date TIMESTAMP NOT NULL
);

CREATE TABLE payment_attempts(
    id SERIAL PRIMARY KEY ,
    invoices_id INTEGER REFERENCES invoices(id) ON DELETE CASCADE ,
    status VARCHAR(50) NOT NULL ,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);