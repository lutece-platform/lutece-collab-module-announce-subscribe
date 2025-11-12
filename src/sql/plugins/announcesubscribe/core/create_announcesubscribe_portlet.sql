-- liquibase formatted sql
-- changeset announce-subscribe:create_announcesubscribe_portlet.sql
-- preconditions onFail:MARK_RAN onError:WARN
ALTER TABLE subscribe_subscription ADD email_subscribes VARCHAR (255) ;