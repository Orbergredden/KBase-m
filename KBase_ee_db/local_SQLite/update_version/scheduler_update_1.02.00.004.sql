-->>
-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.02.00.004', 
       descr = 'add tasks_control_specific.username',
       date_modified = '2025-09-23 15:10:00'
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '2025-09-23 15:10:00', 
       descr = '',
       date_modified = '2025-09-23 15:10:00'
 where alias = 'VERSION_DB_END_DATE'
;

--######## table tasks_control_specific
alter table tasks_control_specific add column username text(50);

--<<

