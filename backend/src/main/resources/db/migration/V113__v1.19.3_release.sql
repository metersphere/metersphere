ALTER TABLE test_plan_report
    MODIFY name varchar(128) NULL COMMENT 'name';

update project set case_template_id = (
    select id
    from test_case_template
    where `global` = 0
      and `system` = 1
      and project_id = project.id limit 1
) where case_template_id is null or case_template_id = '';

update project set case_template_id = (
    select id
    from test_case_template
    where `global` = 1
      and `system` = 1
      and project_id = 'global' limit 1
) where case_template_id is null or case_template_id = '';

CREATE INDEX test_plan_test_case_plan_id_index
    ON test_plan_test_case(plan_id);

CREATE INDEX custom_field_template_field_id_index
    ON custom_field_template(field_id);

CREATE INDEX custom_field_template_template_id_index
    ON custom_field_template(template_id);

CREATE INDEX test_case_review_test_case_review_id_index
    ON test_case_review_test_case(review_id);

