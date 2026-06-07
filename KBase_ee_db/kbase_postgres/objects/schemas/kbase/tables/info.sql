-- Table: kbase.info

-- DROP TABLE IF EXISTS kbase.info;

CREATE TABLE IF NOT EXISTS kbase.info
(
    id bigint NOT NULL DEFAULT nextval('kbase.seq_info'::regclass),
    sectionid bigint NOT NULL,
    infotypeid bigint NOT NULL,
    infoid bigint NOT NULL,
    template_style_id bigint,
    "position" bigint,
    name character varying(255) COLLATE pg_catalog."default",
    descr character varying(255) COLLATE pg_catalog."default",
    date_created timestamp without time zone DEFAULT now(),
    date_modified timestamp without time zone DEFAULT now(),
    user_created character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    user_modified character varying(30) COLLATE pg_catalog."default" DEFAULT "current_user"(),
    CONSTRAINT pk_info_id PRIMARY KEY (id),
    CONSTRAINT fk_info_infotypeid FOREIGN KEY (infotypeid)
        REFERENCES kbase.infotype (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_info_sectionid FOREIGN KEY (sectionid)
        REFERENCES kbase.sections (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_info_template_style_id FOREIGN KEY (template_style_id)
        REFERENCES kbase.template_style (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS kbase.info
    OWNER to kbase;

REVOKE ALL ON TABLE kbase.info FROM kbase_user;

GRANT ALL ON TABLE kbase.info TO kbase;

GRANT SELECT ON TABLE kbase.info TO kbase_user;

COMMENT ON TABLE kbase.info
    IS 'Заголовки инфоблоков.';

COMMENT ON COLUMN kbase.info.infotypeid
    IS 'тип информационного блока (текст, ссылка, картинка и тд)';

COMMENT ON COLUMN kbase.info.infoid
    IS 'ссылка на инфу в таблице с информационным блоком соответствующего типа';

COMMENT ON COLUMN kbase.info.template_style_id
    IS 'Стиль шаблона инфо блока.';

COMMENT ON COLUMN kbase.info."position"
    IS 'Положение (порядковый номер) в документе.';

COMMENT ON COLUMN kbase.info.descr
    IS 'краткое описание информационного блока';