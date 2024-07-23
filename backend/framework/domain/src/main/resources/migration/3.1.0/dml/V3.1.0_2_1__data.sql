-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

DROP TABLE IF EXISTS functional_minder_extra_node;

-- 清洗测试计划表的pos数据
DROP PROCEDURE IF EXISTS UpdatePosForNoneGroup;
DELIMITER
$$
CREATE PROCEDURE UpdatePosForNoneGroup()
BEGIN
    #声明结束标识
    DECLARE done INT DEFAULT FALSE;
    DECLARE testPlanUpdateId varchar(50);
    DECLARE testPlanProjectId varchar(50);
    DECLARE lastProjectId varchar(50) default '';
    DECLARE current_pos INT DEFAULT 4096;
    DECLARE updateRow CURSOR FOR SELECT id, project_id
                                 FROM test_plan
                                 WHERE `group_id` = 'none'
                                 ORDER BY project_id ASC, num ASC;
    #设置终止标志
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    #打开游标
    OPEN updateRow;
    #获取当前游标指针记录，取出值赋给自定义的变量
    FETCH updateRow INTO testPlanUpdateId,testPlanProjectId;
    #遍历游标
    REPEAT
        #判断是否是同一个项目
        IF testPlanProjectId != lastProjectId THEN
            SET current_pos = 4096;
            SET lastProjectId = testPlanProjectId;
        END IF;
        #利用取到的值进行数据库的操作
        update test_plan set pos = current_pos where id = testPlanUpdateId;
        # 将游标中的值再赋值给变量，供下次循环使用
        FETCH updateRow INTO testPlanUpdateId,testPlanProjectId;
        SET current_pos = current_pos + 4096;
    UNTIL done END REPEAT;
    #关闭游标
    CLOSE updateRow;
END
$$
DELIMITER ;
CALL UpdatePosForNoneGroup();
DROP PROCEDURE IF EXISTS UpdatePosForNoneGroup;
-- 清洗测试计划表的pos数据结束

-- 修改未归档测试计划的存储状态
UPDATE test_plan
SET status = 'NOT_ARCHIVED'
WHERE status != 'ARCHIVED';

-- 组织管理员、项目管理员增加权限
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'org_admin', 'ORGANIZATION_MEMBER:READ+INVITE');
INSERT INTO user_role_permission (id, role_id, permission_id) VALUES (UUID_SHORT(), 'project_admin', 'PROJECT_USER:READ+INVITE');

-- 项目用户组内置基本信息权限位
delete from user_role_permission where role_id in (select id from user_role where type = 'PROJECT') and permission_id = 'PROJECT_BASE_INFO:READ';
insert into user_role_permission (id, role_id, permission_id) SELECT UUID_SHORT(), id, 'PROJECT_BASE_INFO:READ' from user_role where type = 'PROJECT';

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;