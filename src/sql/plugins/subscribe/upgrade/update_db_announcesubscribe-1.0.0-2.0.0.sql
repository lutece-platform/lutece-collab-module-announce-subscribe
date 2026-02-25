-- liquibase formatted sql
-- changeset announce-subscribe:update_db_announcesubscribe-1.0.0-2.0.0.sql
-- preconditions onFail:MARK_RAN onError:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.COLUMNS WHERE UPPER(TABLE_NAME) = 'SUBSCRIBE_SUBSCRIPTION' AND UPPER(COLUMN_NAME) = 'EMAIL_SUBSCRIBES'

--
-- Remove email_subscribes column: emails are now resolved from id_user
-- via LuteceUserService at notification time
--

ALTER TABLE subscribe_subscription DROP COLUMN email_subscribes;
