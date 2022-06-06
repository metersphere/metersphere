ALTER TABLE api_test_case drop COLUMN response;
ALTER TABLE api_test_case add COLUMN last_result_id varchar(64) COMMENT 'Last ApiDefinitionExecResult ID';

UPDATE api_test_case tt
INNER JOIN (
	SELECT
		atc.id,
		ader.id AS result_id
	FROM
		api_test_case atc
		LEFT JOIN `user` u1 ON atc.create_user_id = u1.id
		LEFT JOIN `user` u2 ON atc.update_user_id = u2.id
		LEFT JOIN (
		SELECT
			max( create_time ) create_time,
			STATUS,
			id,
			resource_id
		FROM
			api_definition_exec_result
		GROUP BY
			resource_id
		) AS ader ON atc.id = ader.resource_id
	) tc
	SET tt.last_result_id = tc.result_id
WHERE
	tt.id = tc.id;

-- 场景批量复制
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
VALUES (UUID(), 'project_admin', 'PROJECT_API_SCENARIO:READ+BATCH_COPY', 'PROJECT_API_SCENARIO');