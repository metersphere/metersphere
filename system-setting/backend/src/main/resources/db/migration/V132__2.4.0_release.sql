-- v2.4.0 init
--
-- 通知表时间加索引
CREATE INDEX notification_create_time_index
    ON notification (create_time);

-- v2-4-init-permission
-- 	创建人 liyuhao
INSERT INTO user_group_permission (id, group_id, permission_id, module_id)
SELECT UUID(), id, 'PROJECT_TRACK_PLAN:READ+BATCH_DELETE', 'PROJECT_TRACK_PLAN'
FROM `group`
WHERE type = 'PROJECT'
  AND `group`.id IN
      (SELECT group_id FROM user_group_permission WHERE permission_id = 'PROJECT_TRACK_PLAN:READ');


