GRANT USAGE ON SCHEMA nextcloud TO "apps";
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA nextcloud TO "apps";
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA nextcloud TO "apps";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA nextcloud TO "apps";