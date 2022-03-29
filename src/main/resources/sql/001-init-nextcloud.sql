CREATE SCHEMA nextcloud;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE nextcloud.scripts
(
    filename character varying(255) NOT NULL,
    passed timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT scripts_pkey PRIMARY KEY (filename)
);

CREATE TABLE nextcloud.user
(
    id bigserial NOT NULL,
    user_id character varying(255) NOT NULL,
    login character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    last_modified timestamp without time zone DEFAULT now(),
    PRIMARY KEY (user_id)
);

CREATE INDEX idx_user_id
    ON nextcloud.user
    USING btree
    (user_id);