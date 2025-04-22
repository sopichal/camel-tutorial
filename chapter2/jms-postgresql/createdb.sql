-- Create database (run as postgres superuser)
CREATE DATABASE camel_tutorial;

\c camel_tutorial;

-- Create tables
CREATE TABLE IF NOT EXISTS purchase_orders (
    id SERIAL PRIMARY KEY,
    order_name VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    customer VARCHAR(255) NOT NULL,
    received_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create audit log table
CREATE TABLE IF NOT EXISTS message_log (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    source_queue VARCHAR(100),
    processing_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message_content TEXT,
    processing_status VARCHAR(50)
);

-- Create views
CREATE OR REPLACE VIEW order_summary AS
SELECT 
    customer,
    COUNT(*) as total_orders,
    SUM(amount) as total_amount
FROM purchase_orders
GROUP BY customer;

-- Create indexes
CREATE INDEX idx_purchase_orders_customer ON purchase_orders(customer);
CREATE INDEX idx_message_log_message_id ON message_log(message_id);

-- Create user (optional - run as postgres superuser)
-- CREATE USER camel_user WITH PASSWORD 'camel_password';
-- GRANT ALL PRIVILEGES ON DATABASE camel_tutorial TO camel_user;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO camel_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO camel_user;