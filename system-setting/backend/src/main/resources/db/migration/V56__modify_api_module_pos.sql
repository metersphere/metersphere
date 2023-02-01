SET SESSION innodb_lock_wait_timeout = 7200;
set @rownum=0;
update api_module set pos=(select @rownum := @rownum +1) where pos is null;
update api_scenario_module set pos=(select @rownum := @rownum +1) where pos is null;
SET SESSION innodb_lock_wait_timeout = DEFAULT;
