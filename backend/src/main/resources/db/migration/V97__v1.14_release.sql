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
    DECLARE done INT DEFAULT 0;
    DECLARE cursor1 CURSOR FOR (SELECT user_id, source_id, group_id
                                FROM user_group
                                WHERE group_id IN ('org_admin', 'org_member'));


    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor1;
    outer_loop:
    LOOP
        FETCH cursor1 INTO userId, sourceId, groupId;
        IF done
        THEN
            LEAVE outer_loop;
        END IF;
        INSERT INTO user_group (id, user_id, group_id, source_id, create_time, update_time)
        SELECT UUID(), userId, REPLACE(groupId, 'org', 'ws'), id, create_time, update_time
        FROM workspace
        WHERE organization_id = sourceId;
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

ALTER TABLE test_case_review ADD COLUMN follow_people;
ALTER TABLE test_plan ADD COLUMN follow_people;

