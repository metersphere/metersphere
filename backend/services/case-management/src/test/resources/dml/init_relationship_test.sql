INSERT INTO functional_case(id, num, module_id, project_id, template_id, name, review_status, tags, case_edit_type, pos, version_id, ref_id, last_execute_result, deleted, public_case, latest, create_user, update_user, delete_user, create_time, update_time, delete_time)
VALUES ('wx_relationship_1', 1, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试1', 'UN_REVIEWED', NULL, 'STEP', 0, 'v1.0.0', 'v1.0.0', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_2', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试2', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_3', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试3', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_4', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试4', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_5', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试5', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_6', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试6', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_7', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试7', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_8', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试8', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_9', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试9', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_10', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试10', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_11', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试11', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_12', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试12', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL),
('wx_relationship_13', 2, 'TEST_MODULE_ID', 'wx_relationship', '100001', '前后置测试13', 'UN_REVIEWED', '["测试标签_1"]', 'STEP', 0, 'v1.0.0', 'TEST_REF_ID', 'UN_EXECUTED', b'0', b'0', b'1', 'admin', 'admin', '', 1698058347559, 1698058347559, NULL);



INSERT INTO functional_case_blob(id, steps, text_description, expected_result, prerequisite, description)
VALUES ('wx_relationship_1', 'STEP', '1111', '', '', 'TEST'),
        ('wx_relationship_2', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_3', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_4', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_5', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_6', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_7', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_8', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_9', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_10', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_11', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_12', 'STEP', '1111', '', '', '1111'),
        ('wx_relationship_13', 'STEP', '1111', '', '', '1111');



INSERT INTO functional_case_relationship_edge(id, source_id, target_id, graph_id, create_user, update_time, create_time)
VALUES ('relationship_1', 'wx_relationship_1', 'wx_relationship_2', '1', 'admin', 1698058347559, 1698058347559),
       ('relationship_3', 'wx_relationship_2', 'wx_relationship_3', '1', 'admin', 1698058347559, 1698058347559);


INSERT INTO project_version(id, project_id, name, description, status, latest, publish_time, start_time, end_time, create_time, create_user)
VALUES ('v1.0.0', 'wx_relationship', 'v1.0', NULL, 'open', b'1', 1698810592000, 1698810592000, 1698810592000, 1698810592000, 'admin');
