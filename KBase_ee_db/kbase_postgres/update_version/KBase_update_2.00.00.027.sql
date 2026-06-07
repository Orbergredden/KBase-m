--
-- PostgeSQL KBase update
--

--\connect kbase_test

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
--SET client_encoding = 'WIN1251';
SET lc_messages TO 'en_US.UTF-8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = kbase, public, pg_catalog;

-- ######## перевіряємо щоб БД була попередньої версії #############################
do $$ 
<<check_version>>
declare
	v_version_old  varchar(50) := '1.04.01.026';
	v_version      varchar(50);
begin
	select s.value 
	  into v_version
	  from settings s 
	 where s.alias = 'VERSION_DB_NUMBER'
	;
	if v_version_old <> v_version then
		--raise notice 'DB version too old, (% <> %)', v_version, v_version_old;
		RAISE EXCEPTION 'DB version too old, (% <> %)', v_version, v_version_old;
	end if;
end check_version $$;

-- ######## update table Settings for new version ##################################
update settings 
   set value = '2.00.00.027', 
       descr = 'new object Dictionaries',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_NUMBER'
;
update settings 
   set value = '03.04.2024', 
       descr = '',
       date_modified = now(),
       user_modified = "current_user"()
 where alias = 'VERSION_DB_END_DATE'
;
--######## create table dict_categories #######################################
CREATE SEQUENCE kbase.seq_dict_categories
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_dict_categories OWNER TO kbase;

CREATE TABLE kbase.dict_categories
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_dict_categories'::regclass),
	parent_id bigint,
    name character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_categories_id PRIMARY KEY (id)
) 
TABLESPACE pg_default;

ALTER TABLE kbase.dict_categories OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_categories TO kbase;
GRANT SELECT ON TABLE kbase.dict_categories TO kbase_user;

COMMENT ON TABLE kbase.dict_categories IS 'Категорії словників (анлійська, перелік термінів, веб лінки, ...)';
COMMENT ON COLUMN kbase.dict_categories.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_categories.user_modified IS 'Той, хто змінював запис в останнє';
-----------------------------
insert into kbase.dict_categories (id,name,descr)
	values (0,'<немає>','')
;
insert into kbase.dict_categories (name, descr)
	values ('english','англійські слова, фрази та тексти для вивчення')
;
--######## create table dict_source #######################################
CREATE SEQUENCE kbase.seq_dict_source
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_dict_source OWNER TO kbase;

CREATE TABLE kbase.dict_source
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_source'::regclass),
	parent_id bigint,
	category_id bigint,
    name character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_source_id PRIMARY KEY (id),
	CONSTRAINT fk_dict_source_category_id FOREIGN KEY (category_id)
        REFERENCES dict_categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
) 
TABLESPACE pg_default;

ALTER TABLE kbase.dict_source OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_source TO kbase;
GRANT SELECT ON TABLE kbase.dict_source TO kbase_user;

COMMENT ON TABLE kbase.dict_source IS 'Sources of dictionary elements';
COMMENT ON COLUMN kbase.dict_source.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_source.user_modified IS 'Той, хто змінював запис в останнє';
-------------------------------------
insert into kbase.dict_source (id, category_id, name, descr)
	values (0, 0, '<немає>','')
;
------------------------------------------------
do $$ 
<<seq_dict_source>>
declare
-- устанавливаем значение сиквенса
	v_maxId   bigint;
	v_result  bigint;
begin
	select max(id)
	  into v_maxId
	  from kbase.dict_source
	;
	
	v_maxId := v_maxId + 1;
	SELECT pg_catalog.setval('kbase.seq_dict_source', v_maxId, true) into v_result;
	raise notice 'v_result =  %', v_result;
end seq_dict_source $$;	

--######## create table dict_theme #######################################
CREATE SEQUENCE kbase.seq_dict_theme
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE kbase.seq_dict_theme OWNER TO kbase;

CREATE TABLE kbase.dict_theme
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_theme'::regclass),
	parent_id bigint,
	category_id bigint,
	source_id bigint,
    name character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_theme_id PRIMARY KEY (id),
	CONSTRAINT fk_dict_theme_category_id FOREIGN KEY (category_id)
        REFERENCES dict_categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_theme_source_id FOREIGN KEY (source_id)
        REFERENCES dict_source (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
) 
TABLESPACE pg_default;

ALTER TABLE kbase.dict_theme OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_theme TO kbase;
GRANT SELECT ON TABLE kbase.dict_theme TO kbase_user;

COMMENT ON TABLE kbase.dict_theme IS 'класифікуємо елементи по темам';
COMMENT ON COLUMN kbase.dict_theme.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_theme.user_modified IS 'Той, хто змінював запис в останнє';
-------------------------------------
insert into kbase.dict_theme (id, category_id, source_id, name, descr)
	values (0, 0, 0, '<немає>','')
;
--######## create table dict_words #######################################
CREATE SEQUENCE kbase.seq_dict_words
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_dict_words OWNER TO kbase;
--------------------------------------------------------
CREATE TABLE kbase.dict_words
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_words'::regclass),
	category_id bigint,
	source_id bigint,
	theme_id bigint NOT NULL DEFAULT 0,
	name character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
	rating integer,
	reverse integer,
	status integer,
	date_viewed timestamp without time zone DEFAULT now(),
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_words_id PRIMARY KEY (id),
    CONSTRAINT fk_dict_words_category_id FOREIGN KEY (category_id)
        REFERENCES kbase.dict_categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_words_source_id FOREIGN KEY (source_id)
        REFERENCES dict_source (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_words_theme_id FOREIGN KEY (theme_id)
        REFERENCES dict_theme (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.dict_words OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_words TO kbase;
GRANT SELECT ON TABLE kbase.dict_words TO kbase_user;
GRANT SELECT ON TABLE kbase.dict_words TO kbase_view;

COMMENT ON TABLE kbase.dict_words IS 'Слова або короткі назви';
COMMENT ON COLUMN kbase.dict_words.source_id IS 'джерело, звідки взята ця інформація';
COMMENT ON COLUMN kbase.dict_words.rating IS 'це типу як часто його показувати при навчанні';
COMMENT ON COLUMN kbase.dict_words.reverse IS '1 - показувати при навчанні в зворотньому напрямку';
COMMENT ON COLUMN kbase.dict_words.status IS '0 - не включаємо в навчання, 1 - включаємо';
COMMENT ON COLUMN kbase.dict_words.date_viewed IS 'дата останього показу';
COMMENT ON COLUMN kbase.dict_words.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_words.user_modified IS 'Той, хто змінював запис в останнє';

CREATE INDEX ind_dict_words_category_id
    ON kbase.dict_words USING btree
    (category_id ASC NULLS LAST)
    TABLESPACE pg_default;
	
CREATE INDEX ind_dict_words_date_viewed
    ON kbase.dict_words USING btree
    (date_viewed ASC NULLS LAST)
    TABLESPACE pg_default;
       
--######## create table dict_phrases #######################################
CREATE SEQUENCE kbase.seq_dict_phrases
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_dict_phrases OWNER TO kbase;
--------------------------------------------------------
CREATE TABLE kbase.dict_phrases
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_phrases'::regclass),
	category_id bigint,
	source_id bigint,
	theme_id bigint NOT NULL DEFAULT 0,
	name character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
	rating integer,
	reverse integer,
	status integer,
	date_viewed timestamp without time zone DEFAULT now(),
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_phrases_id PRIMARY KEY (id),
    CONSTRAINT fk_dict_phrases_category_id FOREIGN KEY (category_id)
        REFERENCES kbase.dict_categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_phrases_source_id FOREIGN KEY (source_id)
        REFERENCES dict_source (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_phrases_theme_id FOREIGN KEY (theme_id)
        REFERENCES dict_theme (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.dict_phrases OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_phrases TO kbase;
GRANT SELECT ON TABLE kbase.dict_phrases TO kbase_user;
GRANT SELECT ON TABLE kbase.dict_phrases TO kbase_view;

COMMENT ON TABLE kbase.dict_phrases IS 'Фрази або речення';
COMMENT ON COLUMN kbase.dict_phrases.source_id IS 'джерело, звідки взята ця інформація';
COMMENT ON COLUMN kbase.dict_phrases.rating IS 'це типу як часто його показувати при навчанні';
COMMENT ON COLUMN kbase.dict_phrases.reverse IS '1 - показувати при навчанні в зворотньому напрямку';
COMMENT ON COLUMN kbase.dict_phrases.status IS '0 - не включаємо в навчання, 1 - включаємо';
COMMENT ON COLUMN kbase.dict_phrases.date_viewed IS 'дата останього показу';
COMMENT ON COLUMN kbase.dict_phrases.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_phrases.user_modified IS 'Той, хто змінював запис в останнє';

CREATE INDEX ind_dict_phrases_category_id
    ON kbase.dict_phrases USING btree
    (category_id ASC NULLS LAST)
    TABLESPACE pg_default;
	
CREATE INDEX ind_dict_phrases_date_viewed
    ON kbase.dict_phrases USING btree
    (date_viewed ASC NULLS LAST)
    TABLESPACE pg_default;

--######## create table dict_texts #######################################
CREATE SEQUENCE kbase.seq_dict_texts
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_dict_texts OWNER TO kbase;
--------------------------------------------------------
CREATE TABLE kbase.dict_texts
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_texts'::regclass),
	category_id bigint,
	source_id bigint,
	theme_id bigint NOT NULL DEFAULT 0,
	name text,
    descr text,
	rating integer,
	reverse integer,
	status integer,
	date_viewed timestamp without time zone DEFAULT now(),
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_dict_texts_id PRIMARY KEY (id),
    CONSTRAINT fk_dict_texts_category_id FOREIGN KEY (category_id)
        REFERENCES kbase.dict_categories (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_texts_source_id FOREIGN KEY (source_id)
        REFERENCES dict_source (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
	CONSTRAINT fk_dict_texts_theme_id FOREIGN KEY (theme_id)
        REFERENCES dict_theme (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
TABLESPACE pg_default;

ALTER TABLE kbase.dict_texts OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_texts TO kbase;
GRANT SELECT ON TABLE kbase.dict_texts TO kbase_user;
GRANT SELECT ON TABLE kbase.dict_texts TO kbase_view;

COMMENT ON TABLE kbase.dict_texts IS 'Тексти';
COMMENT ON COLUMN kbase.dict_texts.source_id IS 'джерело, звідки взята ця інформація';
COMMENT ON COLUMN kbase.dict_texts.rating IS 'це типу як часто його показувати при навчанні';
COMMENT ON COLUMN kbase.dict_texts.reverse IS '1 - показувати при навчанні в зворотньому напрямку';
COMMENT ON COLUMN kbase.dict_texts.status IS '0 - не включаємо в навчання, 1 - включаємо';
COMMENT ON COLUMN kbase.dict_texts.date_viewed IS 'дата останього показу';
COMMENT ON COLUMN kbase.dict_texts.user_created IS 'Той, хто створив запис';
COMMENT ON COLUMN kbase.dict_texts.user_modified IS 'Той, хто змінював запис в останнє';

CREATE INDEX ind_dict_texts_category_id
    ON kbase.dict_texts USING btree
    (category_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX ind_dict_texts_date_viewed
    ON kbase.dict_texts USING btree
    (date_viewed ASC NULLS LAST)
    TABLESPACE pg_default;

--######## create table dict_current #######################################
CREATE SEQUENCE kbase.seq_dict_current
    INCREMENT 1
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE kbase.seq_dict_current OWNER TO kbase;
--------------------------------------------------------
CREATE TABLE kbase.dict_current
(
    id bigint NOT NULL DEFAULT nextval('seq_dict_current'::regclass),
	category_id bigint,
	item_type integer,
	item_id integer,
	user_owner character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    CONSTRAINT pk_dict_current_id PRIMARY KEY (id),
    CONSTRAINT fk_dict_current_category_id FOREIGN KEY (category_id)
        REFERENCES kbase.dict_categories (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
)
TABLESPACE pg_default;

ALTER TABLE kbase.dict_current OWNER to kbase;
GRANT ALL ON TABLE kbase.dict_current TO kbase;

COMMENT ON TABLE kbase.dict_current IS 'Поточні елементи словників для процесу навчання';
COMMENT ON COLUMN kbase.dict_current.item_type IS '1 - word, 2 - phrase, 3 - text';
COMMENT ON COLUMN kbase.dict_current.item_id IS 'id таблиці елементів відповідного типу';
COMMENT ON COLUMN kbase.dict_current.user_owner IS 'для якого користувача інформація';

--######## create function random_number() ############################
CREATE OR REPLACE FUNCTION kbase.random_number(min_value INTEGER, max_value INTEGER)
RETURNS INTEGER 
AS $$
-- Повертає випадкове число з вказаного диапазона
DECLARE
    range_width FLOAT;
BEGIN
    -- Check if the range is valid
    IF min_value >= max_value THEN
        RAISE EXCEPTION 'kbase.random_number >>> Invalid range: min_value must be less than max_value.';
    END IF;

    -- Calculate the width of the range
    range_width := max_value - min_value;

    -- Generate a random number within the specified range
    --RETURN min_value + ROUND(random() * range_width - 0.5);
	RETURN min_value + ROUND(random() * range_width);
END;
$$ LANGUAGE plpgsql;

ALTER FUNCTION kbase.random_number(INTEGER, INTEGER) OWNER TO kbase;
COMMENT ON FUNCTION kbase.random_number(INTEGER, INTEGER)
    IS 'Повертає випадкове число з вказаного диапазона';

--######## create function dictionary_get_item_for_learn () ############################
CREATE OR REPLACE FUNCTION kbase.dictionary_get_item_for_learn(
	p_categoryId bigint,
	p_isWord boolean, p_isPhrase boolean, p_isText boolean,
	p_isStatus boolean, p_isRating boolean, p_isDateViewed boolean, 
	p_isRandom boolean, p_isReverse boolean)
    RETURNS TABLE(dict_type integer, dict_id bigint, dict_reverse integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE SECURITY DEFINER PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
-- вибирає за алгорітмом елемент словника для навчання
DECLARE
	cur_type integer;
	cur_type_count integer := 0;    -- скільки типів словників для вибору
	cur_is_present boolean := true; -- чи є запис в таблиці dict_current для наших умов
	cur_id integer;
BEGIN
	--######## обираємо рандомно тип елементу
	if p_isWord   then cur_type_count := cur_type_count + 1; end if;
	if p_isPhrase then cur_type_count := cur_type_count + 1; end if;
	if p_isText   then cur_type_count := cur_type_count + 1; end if;
	
	if cur_type_count = 0 then
		RAISE EXCEPTION 'Не вибраний жоден з типів словника (слово, фраза чи текст)';
	end if;

	cur_type_count := random_number(1,cur_type_count);

	if (cur_type_count = 1) and p_isWord then
		cur_type := 1;
	else
		if (cur_type_count = 1) and p_isPhrase then
			cur_type := 2;
		else
			cur_type := 3;
		end if;
	end if;
	if (cur_type_count = 2) and p_isPhrase and p_isWord then
		cur_type := 2;
	else
		cur_type := 3;
	end if;
	if cur_type_count = 3 then
		cur_type := 3;
	end if;
	
	--######## вибираємо поточні значення
	begin 
		select c.item_id
		  into cur_id
	      from dict_current c
	     where c.category_id = p_categoryId
		   and c.item_type = cur_type
	       and c.user_owner = "current_user"()
		;
	exception
		WHEN NO_DATA_FOUND THEN
			cur_id := 0;
			cur_is_present := false;
	end;
	
	





	--######## зберігаємо поточні значення
	if cur_is_present then
		update dict_current 
	       set item_id = cur_id
		 where category_id = p_categoryId
		   and item_type = cur_type
	       and user_owner = current_user
		;
	else
		insert into dict_current (category_id, item_type, item_id)
			values (p_categoryId, cur_type, cur_id)
		;
	end if;




	dict_type := 2;
	dict_id := 14;
	dict_reverse := 0;
	RETURN NEXT;
	
	
END; 
$BODY$;
--<<