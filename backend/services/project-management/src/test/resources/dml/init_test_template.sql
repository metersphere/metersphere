INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_id_1', 'functional_default', '', b'1', 1696992836000, 1696992836000, 'admin', 'ORGANIZATION', 'test_project_id_1', b'0', NULL, 'FUNCTIONAL');


INSERT INTO template (id, name, remark, internal, update_time, create_time, create_user, scope_type, scope_id, enable_third_part, ref_id, scene)
VALUES ('test_template_id_2', 'functional_default', '', b'0', 1696992836000, 1696992836000, 'admin', 'ORGANIZATION', 'test_project_id_2', b'0', NULL, 'FUNCTIONAL');

INSERT INTO template_custom_field(id, field_id, template_id, required, pos, api_field_id, default_value) VALUES ('100555929702891957', '100555929702891955', 'test_template_id_2', b'1', 0, NULL, NULL);

INSERT INTO custom_field(id, name, scene, type, remark, internal, scope_type, create_time, update_time, create_user, ref_id, enable_option_key, scope_id) VALUES ('100555929702891955', '测试自定义字段', 'FUNCTIONAL', 'SELECT', '', b'0', 'ORGANIZATION', 1698810592000, 1698810592000, 'admin', NULL, b'0', '100001');

INSERT INTO project_application (project_id, type, type_value) VALUES
    ('100001100001', 'BUG_SYNC_PLATFORM_KEY', 'jira');
