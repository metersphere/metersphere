SET SESSION innodb_lock_wait_timeout = 7200;

-- 评审的用例和用户的中间表
CREATE TABLE IF NOT EXISTS test_case_review_test_case_users
(
    case_id   varchar(50) null,
    review_id varchar(50) null,
    user_id   varchar(50) null,
    constraint test_case_review_test_case_users_pk
        unique (case_id, review_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

SET SESSION innodb_lock_wait_timeout = DEFAULT;