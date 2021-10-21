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
    declare temp int default 0;
    DECLARE cursor1 CURSOR FOR (SELECT user_id, source_id, group_id, id, create_time, update_time
                                FROM user_group
                                WHERE group_id IN (select id from `group` where type = 'ORGANIZATION'));
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
            set temp = 0;
            select count(*) from user_group where user_id = userId and group_id = 'ws_admin' and source_id = workspaceId into temp;
            select temp;
            -- 不存在就新增数据
            IF temp = 0 then
                INSERT INTO user_group (id, user_id, group_id, source_id, create_time, update_time)
                values (UUID(), userId, 'ws_admin', workspaceId, createTime, updateTime);
            END IF;
        END LOOP;
        DELETE FROM user_group where id = sourceUserGroupId;
        SET done = 0;
        CLOSE cursor2;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;

create table if not exists relationship_edge (
    source_id varchar(50) not null comment '源节点的ID',
    target_id varchar(50) not null comment '目标节点的ID',
    `type` varchar(20) not null comment '边的分类',
    graph_id  varchar(50) not null comment '所属关系图的ID',
    creator varchar(50) not null comment '创建人',
    create_time bigint(13) not null,
    PRIMARY KEY (source_id, target_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE utf8mb4_general_ci;

ALTER TABLE api_definition ADD remark TEXT NULL;
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

alter table service_integration
    add workspace_id varchar(50) null;

ALTER TABLE test_case_review ADD COLUMN follow_people varchar(50);
ALTER TABLE test_plan ADD COLUMN follow_people varchar(50);

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
    DECLARE cursor1 CURSOR FOR (SELECT organization_id, configuration, platform, id from service_integration);

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
        DELETE FROM service_integration where id = sourceId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;



-- 处理组织级别全局用户组
delete from `group` where type = 'ORGANIZATION' and scope_id = 'global';
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
    DECLARE cursor1 CURSOR FOR (select id, name, description, `system`, type, create_time, update_time, creator, scope_id
                                from `group`
                                where type = 'ORGANIZATION' and scope_id != 'global');

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
        SELECT UUID(), sourceName, sourceDescription, sourceSystem, 'WORKSPACE', sourceCreateTime, sourceUpdateTime, sourceCreator, id
        FROM workspace
        WHERE organization_id = sourceOrganizationId;
        DELETE FROM `group` where id = sourceGroupId;
    END LOOP;
    CLOSE cursor1;
END
//
DELIMITER ;

CALL test_cursor();
DROP PROCEDURE IF EXISTS test_cursor;

-- 工作空间服务集成
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_member', 'WORKSPACE_SERVICE:READ', 'WORKSPACE_SERVICE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_SERVICE:READ+EDIT', 'WORKSPACE_SERVICE');
-- 工作空间消息设置
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_member', 'WORKSPACE_MESSAGE:READ', 'WORKSPACE_MESSAGE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'ws_admin', 'WORKSPACE_MESSAGE:READ+EDIT', 'WORKSPACE_MESSAGE');
-- 项目权限设置
-- jar
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+UPLOAD+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+DELETE+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+UPLOAD+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+DELETE+JAR', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'read_only', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE');
-- file
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+UPLOAD+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_FILE:READ+DELETE+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+UPLOAD+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_FILE:READ+DELETE+FILE', 'PROJECT_FILE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'read_only', 'PROJECT_FILE:READ+FILE', 'PROJECT_FILE');
-- custom code
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+CREATE', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+EDIT', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+DELETE', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_admin', 'PROJECT_CUSTOM_CODE:READ+COPY', 'PROJECT_CUSTOM_CODE');
-- member
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+CREATE', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+EDIT', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+DELETE', 'PROJECT_CUSTOM_CODE');
insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'project_member', 'PROJECT_CUSTOM_CODE:READ+COPY', 'PROJECT_CUSTOM_CODE');

insert into user_group_permission (id, group_id, permission_id, module_id)
values (UUID(), 'read_only', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_CUSTOM_CODE');

-- 删除组织相关权限
delete from user_group_permission where module_id = 'ORGANIZATION_OPERATING_LOG';
delete from user_group_permission where module_id = 'ORGANIZATION_MESSAGE';
delete from user_group_permission where module_id = 'ORGANIZATION_SERVICE';
delete from user_group_permission where module_id = 'ORGANIZATION_GROUP';
delete from user_group_permission where module_id = 'ORGANIZATION_WORKSPACE';


insert into system_parameter (param_key, param_value, type, sort) values ('project.jar.limit.size', 1, 'text', 1);

ALTER TABLE quota
    DROP COLUMN organization_id;

ALTER TABLE service_integration
    DROP COLUMN organization_id;

ALTER TABLE workspace
    DROP COLUMN organization_id;

ALTER TABLE api_test_case ADD COLUMN case_status VARCHAR(100) comment '用例状态等同场景的status';
UPDATE api_test_case set case_status ="Underway" where case_status is null;