-->>
-- 
CREATE TABLE settings (
	id NUMERIC NOT NULL,
	alias TEXT(50) NOT NULL,
	"section" TEXT(50),
	subject TEXT(50),
	name TEXT(50),
	value TEXT(50),
	descr TEXT(200),
	date_created TEXT,
	date_modified TEXT,
	CONSTRAINT settings_pk PRIMARY KEY (id)
);
-- 
INSERT INTO settings (id, alias,"section",subject,"name",value,descr,date_created,date_modified) VALUES
	 (1, 'VERSION_DB_BEGIN_DATE','Version','Db','Begin date','14.03.2025 15:37','','2025-03-14 15:37:00','2025-03-14 15:37:00')
;
-- 
INSERT INTO settings (id, alias,"section",subject,"name",value,descr,date_created,date_modified) VALUES
	 (2, 'VERSION_DB_END_DATE','Version','Db','End date','14.03.2025 15:37','','2025-03-14 15:37:00','2025-03-14 15:37:00')
;
-- 
INSERT INTO settings (id, alias,"section",subject,"name",value,descr,date_created,date_modified) VALUES
	 (3, 'VERSION_DB_NUMBER','Version','Db','number','1.00.00.001','','2025-03-14 15:37:00','2025-03-14 15:37:00')
;
--<<