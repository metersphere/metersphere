ALTER TABLE test_plan
    DROP COLUMN principal;

-- 组织用户组配置到工作空间上
DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE sourceId VARCHAR(64);
    DECLARE userId VARCHAR(64);
    DECLARE groupId VARCHAR(64);
    DECLARE workspaceId VARCHAR(64);
    DECLARE createTime BIGINT(13);
    DECLARE updateTime BIGINT(13);
    DECLARE done INT DEFAULT 0;
    DECLARE sourceUserGroupId VARCHAR(64);
    DECLARE temp int DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT user_id, source_id, group_id, id, create_time, update_time
                                FROM user_group
                                WHERE group_id IN (SELECT id FROM `group` WHERE type = 'ORGANIZATION'));
    DECLARE cursor2 CURSOR FOR (SELECT id
                                FROM workspace
                                WHERE organization_id = sourceId);


    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO userId, sourceId, groupId, sourceUserGroupId, createTime, updateTime;

        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        OPEN cursor2;
        inner_loop:
        LOOP
            FETCH cursor2 INTO workspaceId;
            IF done
            THEN
                LEAVE inner_loop;
            END IF;
            SET temp = 0;
            SELECT COUNT(*)
            FROM user_group
            WHERE user_id = userId AND group_id = 'ws_admin' AND source_id = workspaceId
            INTO temp;
            SELECT temp;
            -- 不存在就新增数据
            IF temp = 0 THEN
                INSERT INTO user_group (id, user_id, group_id, source_id, create_time, update_time)
                VALUES (UUID(), userId, 'ws_admin', workspaceId, createTime, updateTime);
            END IF;
        END LOOP;
        DELETE FROM user_group WHERE id = sourceUserGroupId;
        SET done = 0;
        CLOSE cursor2;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;

CREATE TABLE IF NOT EXISTS relationship_edge
(
    source_id   varchar(50) NOT NULL COMMENT '源节点的ID',
    target_id   varchar(50) NOT NULL COMMENT '目标节点的ID',
    `type`      varchar(20) NOT NULL COMMENT '边的分类',
    graph_id    varchar(50) NOT NULL COMMENT '所属关系图的ID',
    creator     varchar(50) NOT NULL COMMENT '创建人',
    create_time bigint(13)  NOT NULL,
    PRIMARY KEY (source_id, target_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;

ALTER TABLE api_definition
    ADD remark TEXT NULL;
--
ALTER TABLE message_task
    ADD workspace_id VARCHAR(64) NULL;

-- 消息通知去掉组织


DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE userId VARCHAR(64);
    DECLARE testId VARCHAR(64);
    DECLARE type VARCHAR(64);
    DECLARE event VARCHAR(64);
    DECLARE taskType VARCHAR(64);
    DECLARE webhook VARCHAR(255);
    DECLARE identification VARCHAR(64);
    DECLARE isSet VARCHAR(64);
    DECLARE organizationId VARCHAR(64);
    DECLARE createTime BIGINT;
    DECLARE template TEXT;

    DECLARE done INT DEFAULT 0;
    # 必须用 table_name.column_name
    DECLARE cursor1 CURSOR FOR SELECT message_task.type,
                                      message_task.event,
                                      message_task.user_id,
                                      message_task.task_type,
                                      message_task.webhook,
                                      message_task.identification,
                                      message_task.is_set,
                                      message_task.organization_id,
                                      message_task.test_id,
                                      message_task.create_time,
                                      message_task.template
                               FROM message_task;


    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO type, event, userId, taskType, webhook, identification, isSet,
            organizationId,
            testId, createTime, template;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO message_task(id, type, event, user_id, task_type, webhook, identification, is_set, workspace_id,
                                 test_id, create_time, template)
        SELECT UUID(),
               type,
               event,
               userId,
               taskType,
               webhook,
               identification,
               isSet,
               id,
               testId,
               createTime,
               template
        FROM workspace
        WHERE organization_id = organizationId;
        DELETE FROM message_task WHERE organization_id = organizationId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;
-- 去掉组织id
ALTER TABLE message_task
    DROP COLUMN organization_id;

ALTER TABLE user
    DROP COLUMN last_organization_id;

ALTER TABLE service_integration
    ADD workspace_id varchar(50) NULL;

ALTER TABLE test_case_review
    ADD COLUMN follow_people varchar(50);
ALTER TABLE test_plan
    ADD COLUMN follow_people varchar(50);

-- 服务集成从组织转移到工作空间
DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE organizationId VARCHAR(64);
    DECLARE sourceConfig TEXT;
    DECLARE sourcePlatform VARCHAR(64);
    DECLARE sourceId VARCHAR(64);
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT organization_id, configuration, platform, id FROM service_integration);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO organizationId, sourceConfig, sourcePlatform, sourceId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO service_integration(id, organization_id, workspace_id, configuration, platform)
        SELECT UUID(), organizationId, id, sourceConfig, sourcePlatform
        FROM workspace
        WHERE organization_id = organizationId;
        DELETE FROM service_integration WHERE id = sourceId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;


-- 处理组织级别全局用户组
DELETE
FROM `group`
WHERE type = 'ORGANIZATION'
  AND scope_id = 'global';
-- 处理组织级别非全局用户组
DROP PROCEDURE IF EXISTS test_cursor;
DELIMITER //
CREATE PROCEDURE test_cursor()
BEGIN
    DECLARE sourceGroupId VARCHAR(64);
    DECLARE sourceName VARCHAR(64);
    DECLARE sourceDescription VARCHAR(120);
    DECLARE sourceSystem TINYINT(1);
    DECLARE sourceType VARCHAR(20);
    DECLARE sourceCreateTime BIGINT(13);
    DECLARE sourceUpdateTime BIGINT(13);
    DECLARE sourceCreator VARCHAR(64);
    DECLARE sourceOrganizationId VARCHAR(64);
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT id,
                                       name,
                                       description,
                                       `system`,
                                       type,
                                       create_time,
                                       update_time,
                                       creator,
                                       scope_id
                                FROM `group`
                                WHERE type = 'ORGANIZATION'
                                  AND scope_id != 'global');

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO sourceGroupId, sourceName, sourceDescription, sourceSystem,
            sourceType, sourceCreateTime, sourceUpdateTime, sourceCreator, sourceOrganizationId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO `group` (id, name, description, `system`, type, create_time, update_time, creator, scope_id)
        SELECT UUID(),
               sourceName,
               sourceDescription,
               sourceSystem,
               'WORKSPACE',
               sourceCreateTime,
               sourceUpdateTime,
               sourceCreator,
               id
        FROM workspace
        WHERE organization_id = sourceOrganizationId;
        DELETE FROM `group` WHERE id = sourceGroupId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;

-- 工作空间服务集成
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_member', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_SERVICE:READ+EDIT', 'WORKSPACE_SERVICE');
-- 工作空间消息设置
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_member', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'ws_admin', 'WORKSPACE_MESSAGE:READ+EDIT', 'WORKSPACE_MESSAGE');
-- 项目权限设置
-- jar
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+UPLOAD+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+DELETE+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+UPLOAD+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+DELETE+JAR', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'read_only', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
-- file
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+UPLOAD+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_FILE:READ+DELETE+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+UPLOAD+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_FILE:READ+DELETE+FILE', 'PROJECT_FILE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'read_only', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
-- custom code
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+CREATE', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+EDIT', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+DELETE', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+COPY', 'PROJECT_CUSTOM_CODE');
-- member
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+CREATE', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+EDIT', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+DELETE', 'PROJECT_CUSTOM_CODE');
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+COPY', 'PROJECT_CUSTOM_CODE');

INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'read_only', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');

-- 删除组织相关权限
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_OPERATING_LOG';
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_MESSAGE';
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_SERVICE';
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_GROUP';
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_WORKSPACE';
DELETE
FROM user_group_permission
WHERE module_id = 'ORGANIZATION_USER';
DELETE
FROM user_group_permission
WHERE module_id = 'SYSTEM_ORGANIZATION';


INSERT INTO system_parameter (param_key, param_value, type, sort)
VALUES ('project.jar.limit.size', 1, 'text', 1);

ALTER TABLE quota
    DROP
        COLUMN organization_id;

ALTER TABLE service_integration
    DROP
        COLUMN organization_id;

ALTER TABLE workspace
    DROP
        COLUMN organization_id;


DROP TABLE IF EXISTS `test_plan_follow`;
CREATE TABLE `test_plan_follow`
(
    `test_plan_id` varchar(50) DEFAULT NULL,
    `follow_id`    varchar(50) DEFAULT NULL COMMENT '关注人',
    UNIQUE KEY `test_plan_principal_pk` (`test_plan_id`, `follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO test_plan_follow
SELECT id, follow_people
FROM test_plan
WHERE follow_people IS NOT NULL;

ALTER TABLE test_plan
    DROP
        COLUMN follow_people;

DROP TABLE IF EXISTS `test_case_review_follow`;
CREATE TABLE `test_case_review_follow`
(
    `review_id` varchar(50) DEFAULT NULL,
    `follow_id` varchar(50) DEFAULT NULL COMMENT '关注人',
    UNIQUE KEY `test_case_review_users_pk` (`review_id`, `follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT =' review and user association table';

INSERT INTO test_case_review_follow
SELECT id, follow_people
FROM test_case_review
WHERE follow_people IS NOT NULL;

ALTER TABLE test_case_review
    DROP
        COLUMN follow_people;

ALTER TABLE api_test_case
    ADD COLUMN case_status VARCHAR(100) COMMENT '用例状态等同场景的status';
UPDATE api_test_case
SET case_status = 'Underway'
WHERE case_status IS NULL;

-- 性能测试关注人
CREATE TABLE IF NOT EXISTS `load_test_follow`
(
    `test_id`   VARCHAR(50) DEFAULT NULL,
    `follow_id` VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `load_test_follow_test_id_follow_id_pk` (`test_id`, `follow_id`),
    KEY `load_test_follow_follow_id_index` (`follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
-- 性能测试关注人数据迁移
INSERT INTO load_test_follow
SELECT id, follow_people
FROM load_test
WHERE follow_people IS NOT NULL;
ALTER TABLE load_test
    DROP COLUMN follow_people;

-- 自动化关注人
CREATE TABLE IF NOT EXISTS `api_scenario_follow`
(
    `scenario_id` VARCHAR(50) DEFAULT NULL,
    `follow_id`   VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `api_scenario_follow_scenario_id_follow_id_pk` (`scenario_id`, `follow_id`),
    KEY `api_scenario_follow_id_index` (`follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
-- 自动化关注人数据迁移
INSERT INTO api_scenario_follow
SELECT id, follow_people
FROM api_scenario
WHERE follow_people IS NOT NULL;
ALTER TABLE api_scenario
    DROP COLUMN follow_people;

-- 接口定义关注人
CREATE TABLE IF NOT EXISTS `api_definition_follow`
(
    `definition_id` VARCHAR(50) DEFAULT NULL,
    `follow_id`     VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `api_definition_follow_scenario_id_follow_id_pk` (`definition_id`, `follow_id`),
    KEY `api_definition_follow_id_index` (`follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
-- 接口定义数据迁移
INSERT INTO api_definition_follow
SELECT id, follow_people
FROM api_definition
WHERE follow_people IS NOT NULL;
ALTER TABLE api_definition
    DROP COLUMN follow_people;

-- 接口定义关注人
CREATE TABLE IF NOT EXISTS `api_test_case_follow`
(
    `case_id`   VARCHAR(50) DEFAULT NULL,
    `follow_id` VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `api_case_follow_scenario_id_follow_id_pk` (`case_id`, `follow_id`),
    KEY `api_case_follow_id_index` (`follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
-- 接口定义数据迁移
INSERT INTO api_test_case_follow
SELECT id, follow_people
FROM api_test_case
WHERE follow_people IS NOT NULL;
ALTER TABLE api_test_case
    DROP COLUMN follow_people;


-- 测试用例关注人
CREATE TABLE IF NOT EXISTS `test_case_follow`
(
    `case_id`   VARCHAR(50) DEFAULT NULL,
    `follow_id` VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `test_case_follow_scenario_id_follow_id_pk` (`case_id`, `follow_id`),
    KEY `test_case_follow_id_index` (`follow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
-- 测试用例数据迁移
INSERT INTO test_case_follow
SELECT id, follow_people
FROM test_case
WHERE follow_people IS NOT NULL
  AND follow_people != '';
ALTER TABLE test_case
    DROP COLUMN follow_people;
-- 操作日志类型增加普通索引
ALTER TABLE `operating_log`
    ADD INDEX oper_module_index (`oper_module`);

ALTER TABLE issues
    ADD platform_id varchar(50) NOT NULL;
UPDATE issues
SET platform_id = id
WHERE 1;

UPDATE test_case
SET status = 'Prepare'
WHERE status IS NULL;
