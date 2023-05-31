DROP TABLE IF EXISTS test_plan_api_case;
CREATE TABLE test_plan_api_case(
                                   `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                   `test_plan_id` VARCHAR(50) NOT NULL   COMMENT '测试计划ID' ,
                                   `api_case_id` VARCHAR(50) NOT NULL   COMMENT '接口用例ID' ,
                                   `environment_type` VARCHAR(20)    COMMENT '环境类型' ,
                                   `environment` LONGTEXT    COMMENT '所属环境' ,
                                   `environment_group_id` VARCHAR(50)    COMMENT '环境组ID' ,
                                   `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                   `create_user` VARCHAR(40) NOT NULL   COMMENT '创建人' ,
                                   `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔5000' ,
                                   PRIMARY KEY (id)
)  COMMENT = '测试计划关联接口用例';

INSERT INTO test_plan_api_case value ('test-plan-api-case-id', UUID(), UUID(), null, null, null, CURRENT_TIMESTAMP, 'admin', 1);