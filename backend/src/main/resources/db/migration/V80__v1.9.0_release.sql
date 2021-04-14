-- add test_case
alter table test_case
    add step_description text null;
alter table test_case
    add expected_result text null;