ALTER TABLE `operating_log` ADD INDEX oper_time_index ( `oper_time` );

CREATE TABLE `operating_log_resource`
(
    `id` varchar(50) NOT NULL COMMENT 'ID',
    `operating_log_id`   varchar(50) NOT NULL COMMENT 'Operating log ID',
    `source_id`    varchar(50) NOT NULL COMMENT 'operating source id',
    PRIMARY KEY (`id`),
 KEY `operating_log_id_index` (`operating_log_id`),
 KEY `source_id_index` (`source_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE utf8mb4_general_ci;

-- permission
DROP PROCEDURE IF EXISTS test_personal;
DELIMITER //
CREATE PROCEDURE test_personal()
    BEGIN
        #声明结束标识
        DECLARE end_flag int DEFAULT 0;

        DECLARE groupId varchar(64);

        #声明游标 group_curosr
        DECLARE group_curosr CURSOR FOR SELECT DISTINCT group_id FROM user_group;

        #设置终止标志
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET end_flag=1;

        #打开游标
        OPEN group_curosr;

        #遍历游标
        REPEAT
            #获取当前游标指针记录，取出值赋给自定义的变量
            FETCH group_curosr INTO groupId;
            #利用取到的值进行数据库的操作
            INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
            VALUES (uuid(), groupId, 'PERSONAL_INFORMATION:READ+EDIT', 'PERSONAL_INFORMATION'),
                   (uuid(), groupId, 'PERSONAL_INFORMATION:READ+THIRD_ACCOUNT', 'PERSONAL_INFORMATION'),
                   (uuid(), groupId, 'PERSONAL_INFORMATION:READ+API_KEYS', 'PERSONAL_INFORMATION'),
                   (uuid(), groupId, 'PERSONAL_INFORMATION:READ+EDIT_PASSWORD', 'PERSONAL_INFORMATION');
            # 根据 end_flag 判断是否结束
        UNTIL end_flag END REPEAT;

        #关闭游标
        close group_curosr;

    END
//
DELIMITER ;

CALL test_personal();
DROP PROCEDURE IF EXISTS test_personal;
