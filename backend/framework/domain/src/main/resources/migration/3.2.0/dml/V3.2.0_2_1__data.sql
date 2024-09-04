-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;


-- 项目管理员、项目成员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'FUNCTIONAL_CASE:READ+EXPORT');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'FUNCTIONAL_CASE:READ+EXPORT');

-- 补充示例数据评审人
INSERT INTO case_review_functional_case_user VALUES ('970662624288768', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('974785792892928', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('978908961497088', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('988271990202368', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('995917031989248', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('649072653148160', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('650996798496768', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('655978960560128', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('668829502709760', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('674739377709056', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('680735152054272', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('682968535048192', '999009408442368', 'admin');
INSERT INTO case_review_functional_case_user VALUES ('693894931849216', '999009408442368', 'admin');


-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
