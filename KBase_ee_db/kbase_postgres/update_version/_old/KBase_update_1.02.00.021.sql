--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '1.02.01.021', 
       descr = 'create log table. Update table and create functions for default infoTypeStyle',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '14.10.2020 10:17', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
-- ######## create table log ############################################################
CREATE SEQUENCE kbase.seq_log;
ALTER SEQUENCE kbase.seq_log OWNER TO kbase;

CREATE TABLE kbase.log
(
    id bigint NOT NULL DEFAULT nextval('seq_log'::regclass),
    log_type character varying(20) COLLATE pg_catalog."default",
	text character varying(255) COLLATE pg_catalog."default",
	date_created timestamp without time zone DEFAULT now(),
	user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_log PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE kbase.log OWNER to kbase;

GRANT ALL ON TABLE kbase.log TO kbase;
GRANT SELECT ON TABLE kbase.log TO kbase_user;

COMMENT ON TABLE kbase.log
    IS 'Various logs';

COMMENT ON COLUMN kbase.log.log_type
    IS 'Log type. For example, error, admin, app.section.delete etc.';

insert into log (log_type,text) values ('system','Table kbase.seq_log created.');
select * from log;

--####### update table current_style ######################################################
--select 'test message' as message;
DO $$
DECLARE 
	l_i          record;
	l_j          record;
	v_MinStyleId bigint;
BEGIN
	FOR l_i IN (select id, name from infotype where id > 0)
    LOOP
		insert into kbase.log (log_type,text) values ('infotype', l_i.id ||' - '|| l_i.name);
	
		for l_j in (select id, name from template_themes)
		loop
			insert into kbase.log (log_type,text) values ('template_themes', l_j.id ||' - '|| l_j.name);

			-- находим самый старый стиль для текущего типа инфо блока и темы
			select min(cs.infoType_style_id)
			  into v_MinStyleId
              from current_style cs
              join infotype_style s    on s.id = cs.infoType_style_id
                                      and s.infotype_id = l_i.id
             where cs.flag = 0
               and cs.theme_id = l_j.id
            ;
			
			if v_MinStyleId is null then
				-- ищем минимальный стиль (с шаблоном) в справочнике
				select min(it.id)
				  into v_MinStyleId
                  from infotype_style it
                 where it.infotype_id = l_i.id
                   and exists (select 1
                                 from templates t
                                where t.infotype_style_id = it.id
					          )
				;
			end if;

			if v_MinStyleId is not null then
				insert into current_style ("user",theme_id,infotype_style_id,flag)
					values (null, l_j.id, v_MinStyleId, 0)
				;
			end if;
			
			insert into kbase.log (log_type,text) values ('v_MinStyleId', ''||v_MinStyleId);
		end loop;
    END LOOP;
END$$;

select * from log order by id;
--select * from log order by id desc limit 1;

--######## create functions ############################################################
CREATE OR REPLACE FUNCTION kbase.InfoTypeStyle_getIdDefault(
	p_themeId bigint, p_infoTypeId bigint, p_searchLevel int)
    RETURNS bigint
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    
AS $BODY$
DECLARE
-- функция возвращает ид дефолтного стиля для указанных типа инфо блока и темы. 
-- если не найден , то возвращает 0
-- p_searchLevel :
-- 		1 - ищем только для текущего пользователя
--		2 - еще и для всех (без пользователя)
--		3 - если ничего не найдено , возвращает стиль с минимальным ид
	v_idDef      bigint;
BEGIN
	SELECT cs.infoType_style_id
	  INTO v_idDef
	  FROM infotype_style s, current_style cs
     WHERE s.id = cs.infoType_style_id
       AND cs.theme_id = p_themeId
       AND s.infotype_id = p_infoTypeId
       AND cs."user" = "current_user"()
       AND cs.flag = 0
	;
	if (v_idDef is null) and (p_searchLevel > 1) then
		SELECT cs.infoType_style_id
	      INTO v_idDef
	      FROM infotype_style s, current_style cs
         WHERE s.id = cs.infoType_style_id
           AND cs.theme_id = p_themeId
           AND s.infotype_id = p_infoTypeId
           AND cs."user" is null
           AND cs.flag = 0
		;
	end if;

	if (v_idDef is null) and (p_searchLevel > 2) then
		-- ищем минимальный стиль (с шаблоном) в справочнике
		select min(it.id)
		  into v_idDef
          from infotype_style it
         where it.infotype_id = p_infoTypeId
           and exists (select 1
                         from templates t
                        where t.infotype_style_id = it.id
			          )
		;
	end if;

	return v_idDef;
END;
$BODY$;

ALTER FUNCTION kbase.InfoTypeStyle_getIdDefault(bigint,bigint,int)
    OWNER TO kbase;

-------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION kbase.InfoTypeStyle_setDefault(
	p_themeId bigint, p_infoTypeStyleId bigint)
    RETURNS int
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    
AS $BODY$
DECLARE
-- функция устанавливает указанный стиль как дефолтный для указанной темы. 
	v_infotype_id    bigint;
	v_idDef          bigint;
BEGIN
	-- находим ид типа инфо блока
	select t.infotype_id
	  into v_infotype_id
	  from infoType_style t
	 where t.id = p_infoTypeStyleId
	;
	
	-- если нет деф. стиля без клиента - инсертим
	v_idDef := InfoTypeStyle_getIdDefault (p_themeId, v_infotype_id, 2);
	
	if v_idDef is null then
		insert into current_style ("user",theme_id,infotype_style_id,flag)
			values (null, p_themeId, p_infoTypeStyleId, 0)
		;
	end if;
	
	-- ищем есть ли для него уже дефолтный стиль 
	v_idDef := InfoTypeStyle_getIdDefault (p_themeId, v_infotype_id, 1);
	
	-- если есть - апдейтим, если нет - инсертим, если совпадает - ничего не делаем
	if v_idDef is null then
		INSERT INTO current_style (theme_id, infotype_style_id, flag)
			VALUES(p_themeId, p_infoTypeStyleId, 0)
		;
	else
		if v_idDef <> p_infoTypeStyleId then
			UPDATE current_style
		       SET infotype_style_id = p_infoTypeStyleId,
		           date_modified = now()
		     WHERE theme_id = p_themeId
		       AND infotype_style_id = v_idDef
		       AND "user" = "current_user"()
			;
		end if;
	end if;

	return 1;
END;
$BODY$;

ALTER FUNCTION kbase.InfoTypeStyle_setDefault(bigint,bigint) 
    OWNER TO kbase;
-------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION kbase.InfoTypeStyle_unsetDefault(
	p_themeId bigint, p_infoTypeStyleId bigint)
    RETURNS int
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    
AS $BODY$
DECLARE
-- функция удаляет указанный стиль как дефолтный для указанной темы. 
	
BEGIN
	DELETE FROM current_style
     WHERE theme_id = p_themeId
       AND infoType_style_id = p_infoTypeStyleId
       AND "user" = "current_user"()
       AND flag = 0
    ;
	return 1;
END;
$BODY$;

ALTER FUNCTION kbase.InfoTypeStyle_unsetDefault(bigint,bigint) 
    OWNER TO kbase;
	
--######## move table current_style #######################################

CREATE SEQUENCE kbase.seq_current_style
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_current_style OWNER TO kbase;

do $$ 
<<first_block>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.current_style
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_current_style', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end first_block $$;
---------------------------------------------------
CREATE TABLE kbase.current_style
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_current_style'::regclass),
    "user" character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    theme_id bigint NOT NULL,
    infotype_style_id bigint NOT NULL,
    flag integer,
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    CONSTRAINT pk_current_style_id PRIMARY KEY (id),
    CONSTRAINT fk_current_style_infotype_style_id FOREIGN KEY (infotype_style_id)
        REFERENCES public.infotype_style (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_current_style_theme_id FOREIGN KEY (theme_id)
        REFERENCES public.template_themes (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
TABLESPACE pg_default;

ALTER TABLE kbase.current_style OWNER to kbase;
GRANT ALL ON TABLE kbase.current_style TO kbase;
GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE kbase.current_style TO kbase_user;

COMMENT ON TABLE kbase.current_style
    IS 'Пользовательские настройки стилей.';

COMMENT ON COLUMN kbase.current_style.flag
    IS '0 - по умолчанию, 1 - последний';

CREATE INDEX ind_current_style_styleid
    ON kbase.current_style USING btree
    (infotype_style_id ASC NULLS LAST)
    TABLESPACE pg_default;
------------------------------------------------
-- копируем информацию
insert into kbase.current_style
select * from public.current_style
;
------------------------------ 
-- удаляем старые обьекты
DROP TABLE public.current_style;
DROP SEQUENCE public.seq_current_style;

--######## move table infotype_style #######################################

CREATE SEQUENCE kbase.seq_infotype_style
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_infotype_style OWNER TO kbase;

do $$ 
<<seq_infotype_style_block>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from public.infotype_style
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_infotype_style', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_infotype_style_block $$;
---------------------------------------
CREATE TABLE kbase.infotype_style
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_infotype_style'::regclass),
    parent_id bigint,
    infotype_id bigint NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    descr character varying(50) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_infotype_style_id PRIMARY KEY (id),
    CONSTRAINT fk_infotype_style_infotype_id FOREIGN KEY (infotype_id)
        REFERENCES public.infotype (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.infotype_style OWNER to kbase;
GRANT ALL ON TABLE kbase.infotype_style TO kbase;
GRANT SELECT ON TABLE kbase.infotype_style TO kbase_user;

COMMENT ON TABLE kbase.infotype_style IS 'Стили типов шаблонов';
---------------------------------------
-- копируем информацию
insert into kbase.infotype_style
select * from public.infotype_style
;
---------------------------------------
-- пересоздаем внешние ключи зависимых таблиц
ALTER TABLE kbase.current_style DROP CONSTRAINT fk_current_style_infotype_style_id;

ALTER TABLE kbase.current_style
   ADD CONSTRAINT fk_current_style_infotype_style_id FOREIGN KEY
          (infotype_style_id)
          REFERENCES kbase.infotype_style (id)
             MATCH SIMPLE
             ON DELETE CASCADE
             ON UPDATE CASCADE
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.info DROP CONSTRAINT fk_info_infotypestyleid;

ALTER TABLE public.info
   ADD CONSTRAINT fk_info_infotypestyleid FOREIGN KEY
          (infotypestyleid)
          REFERENCES kbase.infotype_style (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;

ALTER TABLE public.templates DROP CONSTRAINT fk_templates_infotype_style_id;

ALTER TABLE public.templates
   ADD CONSTRAINT fk_templates_infotype_style_id FOREIGN KEY
          (infotype_style_id)
          REFERENCES kbase.infotype_style (id)
             MATCH SIMPLE
             ON DELETE NO ACTION
             ON UPDATE NO ACTION
          NOT DEFERRABLE INITIALLY IMMEDIATE
;
------------------------------ 
-- удаляем старые обьекты
DROP TABLE public.infotype_style;
DROP SEQUENCE public.seq_infotype_style;
--<<