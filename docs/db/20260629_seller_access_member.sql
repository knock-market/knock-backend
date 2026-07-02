-- Knock invite/group-only marketplace access
-- Apply before running non-local profiles because db-core.yml uses spring.jpa.hibernate.ddl-auto=validate.

CREATE TABLE seller_access_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    source_share_link_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_seller_access_member_seller_member UNIQUE (seller_id, member_id),
    CONSTRAINT fk_seller_access_member_seller FOREIGN KEY (seller_id) REFERENCES member (id),
    CONSTRAINT fk_seller_access_member_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_seller_access_member_share_link FOREIGN KEY (source_share_link_id) REFERENCES seller_share_link (id),
    INDEX idx_seller_access_member_member_status (member_id, status),
    INDEX idx_seller_access_member_seller_status (seller_id, status),
    INDEX idx_seller_access_member_source_share_link (source_share_link_id)
);
