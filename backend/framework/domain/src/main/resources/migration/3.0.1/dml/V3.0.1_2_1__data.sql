-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 项目成员权限初始化(遗漏)
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_GROUP:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_MESSAGE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_TEST_PLAN:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_API:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_CASE:READ');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_member', 'PROJECT_APPLICATION_BUG:READ');


UPDATE api_report SET exec_status='COMPLETED' WHERE status IN ('ERROR','SUCCESS','FAKE_ERROR');
UPDATE api_report SET exec_status='STOPPED', status='-' WHERE status IN ('STOPPED');
UPDATE api_report SET exec_status='RUNNING', status='-' WHERE status IN ('RUNNING', 'PENDING');

UPDATE api_scenario_report SET exec_status='COMPLETED' WHERE status IN ('ERROR','SUCCESS','FAKE_ERROR');
UPDATE api_scenario_report SET exec_status='STOPPED', status='-' WHERE status IN ('STOPPED');
UPDATE api_scenario_report SET exec_status='RUNNING', status='-' WHERE status IN ('RUNNING', 'PENDING');

UPDATE message_task SET receivers = '["CREATE_USER"]' WHERE receivers = '[""]';

DELIMITER //

CREATE PROCEDURE insert_into_message()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE a_id VARCHAR(50);
    DECLARE cur CURSOR FOR
SELECT id
FROM project
WHERE project.id != '100001100001';

DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

OPEN cur;

read_loop: LOOP
        FETCH cur INTO a_id;
        IF done THEN
            LEAVE read_loop;
END IF;

        -- 插入到表B中
        SET @robot_in_site_id  = (select id as robotId from project_robot where project_id = a_id limit 1);

        SET @test_plan_task_report_id = UUID_SHORT();
INSERT INTO message_task(id, event, receivers, project_robot_id, task_type, test_id, project_id, enable, create_user, create_time, update_user, update_time, use_default_template, use_default_subject, subject)
VALUES (@test_plan_task_report_id, 'DELETE', '["CREATE_USER"]', @robot_in_site_id, 'TEST_PLAN_REPORT_TASK', 'NONE', a_id, true, 'admin', unix_timestamp() * 1000, 'admin',  unix_timestamp() * 1000, true, true, 'message.title.test_plan_report_task_delete');
INSERT INTO message_task_blob(id, template)
VALUES (@test_plan_task_report_id, 'message.test_plan_report_task_delete');

END LOOP;

CLOSE cur;
END//

DELIMITER ;
CALL insert_into_message();
DROP PROCEDURE IF EXISTS insert_into_message;

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;