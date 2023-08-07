-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS ui_element(
                           `id` VARCHAR(50) NOT NULL   COMMENT '元素id' ,
                           `num` INT NOT NULL   COMMENT '元素num' ,
                           `module_id` VARCHAR(50) NOT NULL   COMMENT '元素所属模块id' ,
                           `project_id` VARCHAR(50) NOT NULL   COMMENT '项目id' ,
                           `name` VARCHAR(255) NOT NULL   COMMENT '名称' ,
                           `location_type` VARCHAR(30) NOT NULL   COMMENT '定位类型' ,
                           `location` VARCHAR(300) NOT NULL   COMMENT '元素定位' ,
                           `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                           `update_user` VARCHAR(50) NOT NULL   COMMENT '更新人' ,
                           `version_id` VARCHAR(50) NOT NULL   COMMENT '版本ID' ,
                           `ref_id` VARCHAR(50) NOT NULL   COMMENT '指向初始版本ID' ,
                           `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔5000' ,
                           `latest` BIT NOT NULL  DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
                           `description` VARCHAR(500)    COMMENT '元素描述' ,
                           `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                           `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                           PRIMARY KEY (id)
)  COMMENT = '元素';


CREATE INDEX idx_pos ON ui_element(pos);
CREATE INDEX idx_project_id ON ui_element(project_id);
CREATE INDEX idx_module_id ON ui_element(module_id);
CREATE INDEX idx_version_id ON ui_element(version_id);
CREATE INDEX idx_ref_id ON ui_element(ref_id);
CREATE INDEX idx_create_time ON ui_element(create_time);
CREATE INDEX idx_name ON ui_element(name);
CREATE INDEX idx_update_time ON ui_element(update_time);
CREATE INDEX idx_create_user ON ui_element(create_user);
CREATE INDEX idx_update_user ON ui_element(update_user);

CREATE TABLE IF NOT EXISTS ui_element_module(
                                  `id` VARCHAR(50) NOT NULL   COMMENT '模块ID' ,
                                  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                  `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
                                  `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级ID' ,
                                  `level` INT NOT NULL  DEFAULT 1 COMMENT '模块等级' ,
                                  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                  `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                  `pos` DOUBLE NOT NULL   COMMENT '自定义排序' ,
                                  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                  PRIMARY KEY (id)
)  COMMENT = '元素模块';


CREATE INDEX idx_project_id ON ui_element_module(project_id);
CREATE INDEX idx_name ON ui_element_module(name);
CREATE INDEX idx_create_time ON ui_element_module(create_time);
CREATE INDEX idx_update_time ON ui_element_module(update_time);
CREATE INDEX idx_create_user ON ui_element_module(create_user);

CREATE TABLE IF NOT EXISTS ui_element_scenario_reference(
                                              `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                              `element_id` VARCHAR(50) NOT NULL   COMMENT '元素ID' ,
                                              `element_module_id` VARCHAR(50) NOT NULL   COMMENT '元素模块ID' ,
                                              `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                              `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                              PRIMARY KEY (id)
)  COMMENT = '元素场景关系表';


CREATE INDEX idx_element_id ON ui_element_scenario_reference(element_id);
CREATE INDEX idx_scenario_id ON ui_element_scenario_reference(scenario_id);
CREATE INDEX idx_project_id ON ui_element_scenario_reference(project_id);
CREATE INDEX idx_element_module_id ON ui_element_scenario_reference(element_module_id);

CREATE TABLE IF NOT EXISTS ui_scenario(
                            `id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                            `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                            `tags` VARCHAR(1000)    COMMENT '标签' ,
                            `module_id` VARCHAR(50) NOT NULL   COMMENT '模块ID' ,
                            `name` VARCHAR(255) NOT NULL   COMMENT '场景名称' ,
                            `level` VARCHAR(100) NOT NULL   COMMENT '用例等级' ,
                            `status` VARCHAR(100) NOT NULL   COMMENT '状态' ,
                            `principal` VARCHAR(50) NOT NULL   COMMENT '责任人' ,
                            `step_total` INT NOT NULL  DEFAULT 0 COMMENT '步骤总数' ,
                            `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                            `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                            `last_result` VARCHAR(100)    COMMENT '最后执行结果' ,
                            `report_id` VARCHAR(50)    COMMENT '最后执行结果的报告ID' ,
                            `num` INT NOT NULL   COMMENT 'num' ,
                            `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
                            `custom_num` VARCHAR(64)    COMMENT '自定义num' ,
                            `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                            `delete_time` BIGINT    COMMENT '删除时间' ,
                            `delete_user` VARCHAR(50)    COMMENT '删除人' ,
                            `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔5000' ,
                            `version_id` VARCHAR(50) NOT NULL   COMMENT '版本ID' ,
                            `ref_id` VARCHAR(50) NOT NULL   COMMENT '指向初始版本ID' ,
                            `latest` BIT NOT NULL  DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
                            `description` VARCHAR(500)    COMMENT '描述' ,
                            PRIMARY KEY (id)
)  COMMENT = '场景';


CREATE INDEX idx_ref_id ON ui_scenario(ref_id);
CREATE INDEX idx_version_id ON ui_scenario(version_id);
CREATE INDEX idx_project_id ON ui_scenario(project_id);
CREATE INDEX idx_module_id ON ui_scenario(module_id);
CREATE INDEX idx_name ON ui_scenario(name);
CREATE INDEX idx_status ON ui_scenario(status);
CREATE INDEX idx_principal ON ui_scenario(principal);
CREATE INDEX idx_create_time ON ui_scenario(create_time);
CREATE INDEX idx_update_time ON ui_scenario(update_time);
CREATE INDEX idx_num ON ui_scenario(num);
CREATE INDEX idx_deleted ON ui_scenario(deleted);
CREATE INDEX idx_create_user ON ui_scenario(create_user);

CREATE TABLE IF NOT EXISTS ui_scenario_reference(
                                      `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                      `ui_scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                      `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                      `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                      `reference_id` VARCHAR(50) NOT NULL   COMMENT '被引用的ID' ,
                                      `data_type` VARCHAR(20) NOT NULL   COMMENT '引用的数据类型（场景，指令）' ,
                                      PRIMARY KEY (id)
)  COMMENT = '场景引用关系';


CREATE INDEX idx_ui_scenario_id ON ui_scenario_reference(ui_scenario_id);
CREATE INDEX idx_reference_id ON ui_scenario_reference(reference_id);
CREATE INDEX idx_data_type ON ui_scenario_reference(data_type);

CREATE TABLE IF NOT EXISTS ui_scenario_report(
                                   `id` VARCHAR(50) NOT NULL   COMMENT '报告ID' ,
                                   `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                   `name` VARCHAR(255) NOT NULL   COMMENT '报告名称' ,
                                   `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                   `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                   `status` VARCHAR(64) NOT NULL   COMMENT '报告状态' ,
                                   `trigger_mode` VARCHAR(64) NOT NULL   COMMENT '触发模式（手动，定时，批量，测试计划）' ,
                                   `execute_type` VARCHAR(200) NOT NULL  DEFAULT 'SERIAL' COMMENT '执行类型（并行，串行）' ,
                                   `scenario_name` VARCHAR(255) NOT NULL   COMMENT '场景名称' ,
                                   `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                   `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                   `pool_id` VARCHAR(50) NOT NULL   COMMENT '资源池ID' ,
                                   `end_time` BIGINT NOT NULL   COMMENT '结束时间' ,
                                   `integrated` BIT NOT NULL  DEFAULT 0 COMMENT '报告类型（集合，独立）' ,
                                   `relevance_test_plan_report_id` VARCHAR(50)    COMMENT '关联的测试计划报告ID（可以为空)' ,
                                   `report_source` VARCHAR(64) NOT NULL   COMMENT '报告生成来源(生成报告，后端调试，本地调试)' ,
                                   PRIMARY KEY (id)
)  COMMENT = '场景报告';


CREATE INDEX idx_project_id ON ui_scenario_report(project_id);
CREATE INDEX idx_create_time ON ui_scenario_report(create_time);
CREATE INDEX idx_update_time ON ui_scenario_report(update_time);
CREATE INDEX idx_report_type ON ui_scenario_report(integrated);
CREATE INDEX idx_scenario_id ON ui_scenario_report(scenario_id);
CREATE INDEX idx_name ON ui_scenario_report(name);
CREATE INDEX idx_trigger_mode ON ui_scenario_report(trigger_mode);
CREATE INDEX idx_status ON ui_scenario_report(status);
CREATE INDEX idx_create_user ON ui_scenario_report(create_user);

CREATE TABLE IF NOT EXISTS ui_scenario_report_detail(
                                          `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                          `resource_id` VARCHAR(50) NOT NULL   COMMENT '资源id（场景，接口）' ,
                                          `report_id` VARCHAR(50) NOT NULL   COMMENT '报告 id' ,
                                          `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                          `status` VARCHAR(100) NOT NULL   COMMENT '结果状态' ,
                                          `request_time` BIGINT NOT NULL   COMMENT '请求时间' ,
                                          `total_assertions` BIGINT    COMMENT '总断言数' ,
                                          `pass_assertions` BIGINT    COMMENT '失败断言数' ,
                                          `content` LONGBLOB NOT NULL   COMMENT '执行结果' ,
                                          `base_info` LONGBLOB    COMMENT '记录截图断言等结果' ,
                                          PRIMARY KEY (id)
)  COMMENT = '报告结果';


CREATE INDEX idx_resource_id ON ui_scenario_report_detail(resource_id);
CREATE INDEX idx_report_id ON ui_scenario_report_detail(report_id);
CREATE INDEX idx_create_time ON ui_scenario_report_detail(create_time);
CREATE INDEX idx_status ON ui_scenario_report_detail(status);

CREATE TABLE IF NOT EXISTS ui_scenario_report_structure(
                                             `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                             `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
                                             `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                             `resource_tree` LONGBLOB NOT NULL   COMMENT '资源步骤结构树' ,
                                             PRIMARY KEY (id)
)  COMMENT = '报告结构';


CREATE INDEX idx_report_id ON ui_scenario_report_structure(report_id);

CREATE TABLE IF NOT EXISTS ui_custom_command(
                                  `id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                  `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                  `tags` VARCHAR(1000)    COMMENT '标签' ,
                                  `module_id` VARCHAR(50) NOT NULL   COMMENT '模块ID' ,
                                  `module_path` VARCHAR(1000) NOT NULL   COMMENT '模块路径' ,
                                  `name` VARCHAR(255) NOT NULL   COMMENT '场景名称' ,
                                  `level` VARCHAR(100)    COMMENT '用例等级' ,
                                  `status` VARCHAR(100) NOT NULL   COMMENT '状态' ,
                                  `principal` VARCHAR(50) NOT NULL   COMMENT '责任人' ,
                                  `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                  `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                  `last_result` VARCHAR(100)    COMMENT '最后执行结果' ,
                                  `num` INT NOT NULL   COMMENT 'num' ,
                                  `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
                                  `custom_num` VARCHAR(64)    COMMENT '自定义num' ,
                                  `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                  `delete_time` BIGINT    COMMENT '删除时间' ,
                                  `delete_user` VARCHAR(50)    COMMENT '删除人' ,
                                  `pos` BIGINT NOT NULL   COMMENT '自定义排序，间隔5000' ,
                                  `version_id` VARCHAR(50) NOT NULL   COMMENT '版本ID' ,
                                  `ref_id` VARCHAR(50) NOT NULL   COMMENT '指向初始版本ID' ,
                                  `latest` BIT NOT NULL  DEFAULT 0 COMMENT '是否为最新版本 0:否，1:是' ,
                                  `description` VARCHAR(500)    COMMENT '描述' ,
                                  PRIMARY KEY (id)
)  COMMENT = '自定义指令';


CREATE INDEX idx_ref_id ON ui_custom_command(ref_id);
CREATE INDEX idx_version_id ON ui_custom_command(version_id);
CREATE INDEX idx_project_id ON ui_custom_command(project_id);
CREATE INDEX idx_module_id ON ui_custom_command(module_id);
CREATE INDEX idx_name ON ui_custom_command(name);
CREATE INDEX idx_status ON ui_custom_command(status);
CREATE INDEX idx_principal ON ui_custom_command(principal);
CREATE INDEX idx_create_time ON ui_custom_command(create_time);
CREATE INDEX idx_update_time ON ui_custom_command(update_time);
CREATE INDEX idx_last_result ON ui_custom_command(last_result);
CREATE INDEX idx_num ON ui_custom_command(num);
CREATE INDEX idx_deleted ON ui_custom_command(deleted);
CREATE INDEX idx_create_user ON ui_custom_command(create_user);

CREATE TABLE IF NOT EXISTS ui_scenario_module(
                                   `id` VARCHAR(50) NOT NULL   COMMENT '模块ID' ,
                                   `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                   `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
                                   `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级ID' ,
                                   `level` INT NOT NULL  DEFAULT 1 COMMENT '模块等级' ,
                                   `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                   `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                   `pos` DOUBLE NOT NULL   COMMENT '自定义排序' ,
                                   `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                   PRIMARY KEY (id)
)  COMMENT = '场景模块';


CREATE INDEX idx_project_id ON ui_scenario_module(project_id);
CREATE INDEX idx_name ON ui_scenario_module(name);
CREATE INDEX idx_create_time ON ui_scenario_module(create_time);
CREATE INDEX idx_update_time ON ui_scenario_module(update_time);
CREATE INDEX idx_create_user ON ui_scenario_module(create_user);

CREATE TABLE IF NOT EXISTS ui_custom_command_module(
                                         `id` VARCHAR(50) NOT NULL   COMMENT '模块ID' ,
                                         `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                         `name` VARCHAR(255) NOT NULL   COMMENT '模块名称' ,
                                         `parent_id` VARCHAR(50) NOT NULL  DEFAULT 'NONE' COMMENT '父级ID' ,
                                         `level` INT NOT NULL  DEFAULT 1 COMMENT '模块等级' ,
                                         `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                         `update_time` BIGINT NOT NULL   COMMENT '更新时间' ,
                                         `pos` DOUBLE NOT NULL   COMMENT '自定义排序' ,
                                         `create_user` VARCHAR(50) NOT NULL   COMMENT '创建人' ,
                                         PRIMARY KEY (id)
)  COMMENT = '自定义指令模块';


CREATE INDEX idx_project_id ON ui_custom_command_module(project_id);
CREATE INDEX idx_name ON ui_custom_command_module(name);
CREATE INDEX idx_create_time ON ui_custom_command_module(create_time);
CREATE INDEX idx_update_time ON ui_custom_command_module(update_time);
CREATE INDEX idx_create_user ON ui_custom_command_module(create_user);

CREATE TABLE IF NOT EXISTS ui_scenario_blob(
                                 `id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                 `scenario_definition` LONGBLOB    COMMENT '场景定义' ,
                                 `environment_json` BLOB    COMMENT '环境' ,
                                 PRIMARY KEY (id)
)  COMMENT = '场景大字段';


CREATE INDEX idx_scenario_id ON ui_scenario_blob(id);

CREATE TABLE IF NOT EXISTS ui_scenario_report_environment(
                                               `report_id` VARCHAR(50) NOT NULL   COMMENT '报告ID' ,
                                               `project_id` VARCHAR(50)    COMMENT '项目ID' ,
                                               `environment_id` VARCHAR(50)    COMMENT '环境ID' ,
                                               PRIMARY KEY (report_id)
)  COMMENT = '场景报告和环境关系';


CREATE INDEX idx_report_id ON ui_scenario_report_environment(report_id);

CREATE TABLE IF NOT EXISTS ui_custom_command_blob(
                                       `id` VARCHAR(50) NOT NULL   COMMENT '指令ID' ,
                                       `scenario_definition` LONGBLOB    COMMENT '场景定义' ,
                                       `command_view_struct` LONGBLOB    COMMENT '自定义结构' ,
                                       PRIMARY KEY (id)
)  COMMENT = '自定义指令大字段';


CREATE INDEX idx_command_id ON ui_custom_command_blob(id);

CREATE TABLE IF NOT EXISTS ui_scenario_report_log(
                                       `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                       `report_id` VARCHAR(50) NOT NULL   COMMENT '请求资源 id' ,
                                       `create_time` BIGINT NOT NULL   COMMENT '创建时间' ,
                                       `console` LONGBLOB NOT NULL   COMMENT '执行日志' ,
                                       PRIMARY KEY (id)
)  COMMENT = '报告日志';


CREATE INDEX idx_report_id ON ui_scenario_report_log(report_id);

CREATE TABLE IF NOT EXISTS ui_element_command_reference(
                                             `id` VARCHAR(50) NOT NULL   COMMENT 'ID' ,
                                             `element_id` VARCHAR(50) NOT NULL   COMMENT '元素ID' ,
                                             `element_module_id` VARCHAR(50) NOT NULL   COMMENT '元素模块ID' ,
                                             `command_id` VARCHAR(50) NOT NULL   COMMENT '指令ID' ,
                                             `project_id` VARCHAR(50) NOT NULL   COMMENT '项目ID' ,
                                             PRIMARY KEY (id)
)  COMMENT = '元素指令关系表';


CREATE INDEX idx_element_id ON ui_element_command_reference(element_id);
CREATE INDEX idx_command_id ON ui_element_command_reference(command_id);
CREATE INDEX idx_project_id ON ui_element_command_reference(project_id);
CREATE INDEX idx_element_module_id ON ui_element_command_reference(element_module_id);

CREATE TABLE IF NOT EXISTS ui_scenario_follower(
                                     `scenario_id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                     `user_id` VARCHAR(50) NOT NULL   COMMENT '关注人ID' ,
                                     PRIMARY KEY (scenario_id,user_id)
)  COMMENT = '场景用例关注表';


CREATE INDEX idx_user_id ON ui_scenario_follower(user_id);


DROP TABLE IF EXISTS ui_scenario_variable;
CREATE TABLE ui_scenario_variable(
                                     `resource_id` VARCHAR(50) NOT NULL   COMMENT '场景ID' ,
                                     `type` VARCHAR(100) NOT NULL   COMMENT '变量类型' ,
                                     `value` VARCHAR(1000) NOT NULL   COMMENT '变量值' ,
                                     `name` VARCHAR(255) NOT NULL   COMMENT '变量名称' ,
                                     `description` VARCHAR(500)    COMMENT '描述' ,
                                     PRIMARY KEY (resource_id)
)  COMMENT = '场景变量';


CREATE INDEX idx_resource_id ON ui_scenario_variable(resource_id);
CREATE INDEX idx_name ON ui_scenario_variable(name);
CREATE INDEX idx_type ON ui_scenario_variable(type);

DROP TABLE IF EXISTS ui_custom_variable;
CREATE TABLE ui_custom_variable(
                                   `resource_id` VARCHAR(50) NOT NULL   COMMENT '指令ID' ,
                                   `type` VARCHAR(100) NOT NULL   COMMENT '变量类型' ,
                                   `value` VARCHAR(1000) NOT NULL   COMMENT '变量值' ,
                                   `name` VARCHAR(255) NOT NULL   COMMENT '变量名称' ,
                                   `description` VARCHAR(500)    COMMENT '描述' ,
                                   `deleted` BIT NOT NULL  DEFAULT 0 COMMENT '删除状态' ,
                                   `out_put` BIT NOT NULL  DEFAULT 0 COMMENT '是否是出参' ,
                                   `enable` BIT NOT NULL  DEFAULT 1 COMMENT '启用禁用' ,
                                   PRIMARY KEY (resource_id)
)  COMMENT = '指令初入参变量';


CREATE INDEX idx_resource_id ON ui_custom_variable(resource_id);
CREATE INDEX idx_type ON ui_custom_variable(type);
CREATE INDEX idx_enable ON ui_custom_variable(enable);
CREATE INDEX idx_name ON ui_custom_variable(name);
CREATE INDEX idx_deleted ON ui_custom_variable(deleted);
CREATE INDEX idx_out_put ON ui_custom_variable(out_put);



-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;

