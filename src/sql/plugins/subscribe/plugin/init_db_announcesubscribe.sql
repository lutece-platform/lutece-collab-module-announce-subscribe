-- liquibase formatted sql
-- changeset announce-subscribe:init_db_announcesubscribe.sql
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN onError:WARN

-- No schema modification needed: the module now uses the existing id_user column
-- from subscribe_subscription to resolve emails via LuteceUserService.
-- The email_subscribes column previously added here has been removed in v2.0.0.
