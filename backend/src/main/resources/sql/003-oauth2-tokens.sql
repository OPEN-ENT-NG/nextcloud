ALTER TABLE nextcloud.user
    ADD COLUMN access_token character varying(255),
    ADD COLUMN refresh_token character varying(255),
    ADD COLUMN token_expires_at timestamp without time zone;

ALTER TABLE nextcloud.user
    ALTER COLUMN password DROP NOT NULL;
