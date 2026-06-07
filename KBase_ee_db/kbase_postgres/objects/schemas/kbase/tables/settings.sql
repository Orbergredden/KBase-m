-- Table: kbase.settings

-- DROP TABLE IF EXISTS kbase.settings;

CREATE TABLE IF NOT EXISTS kbase.settings
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_settings'::regclass),
    alias character varying(50) COLLATE pg_catalog."default" NOT NULL,
    section character varying(50) COLLATE pg_catalog."default",
    subject character varying(50) COLLATE pg_catalog."default",
    name character varying(50) COLLATE pg_catalog."default",
    value character varying(50) COLLATE pg_catalog."default",
    descr character varying(200) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_settings_id PRIMARY KEY (id),
    CONSTRAINT k_settings_alias UNIQUE (alias)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.settings
    OWNER to kbase;

REVOKE ALL ON TABLE kbase.settings FROM kbase_user;

GRANT ALL ON TABLE kbase.settings TO kbase;

GRANT SELECT ON TABLE kbase.settings TO kbase_user;

COMMENT ON TABLE kbase.settings
    IS 'Для хранения настроек программы на уровне БД.';

COMMENT ON COLUMN kbase.settings.alias
    IS 'Текстовый уникальный идентификатор';

COMMENT ON COLUMN kbase.settings.user_created
    IS 'Тот, кто создал запись';

COMMENT ON COLUMN kbase.settings.user_modified
    IS 'Тот, кто вносил последние изменения в запись';
-- Index: ind_settings_alias

-- DROP INDEX IF EXISTS kbase.ind_settings_alias;

CREATE UNIQUE INDEX IF NOT EXISTS ind_settings_alias
    ON kbase.settings USING btree
    (alias COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;