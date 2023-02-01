SET SESSION innodb_lock_wait_timeout = 7200;
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('ui.loginImage', NULL, 'file', 3);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('ui.loginLogo', NULL, 'file', 2);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('ui.loginTitle', '', 'text', 4);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('ui.logo', NULL, 'file', 1);
INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('ui.title', '', 'text', 5);
SET SESSION innodb_lock_wait_timeout = DEFAULT;
