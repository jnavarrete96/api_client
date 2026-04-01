CREATE TABLE IF NOT EXISTS client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shared_key VARCHAR(255) NOT NULL,
    business_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE,
    data_added DATE NOT NULL,
    CONSTRAINT uk_shared_key UNIQUE (shared_key)
);