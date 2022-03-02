CREATE TABLE IF NOT EXISTS custom_field_test_case
(
    resource_id varchar(50) NOT NULL,
    field_id varchar(50) NOT NULL,
    value varchar(500),
    text_value text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS custom_field_issues
(
    resource_id varchar(50) NOT NULL,
    field_id varchar(50) NOT NULL,
    value varchar(500),
    text_value text,
    PRIMARY KEY (resource_id, field_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE custom_field ADD third_part TINYINT(1) DEFAULT 0 NOT NULL;
