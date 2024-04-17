-- liquibase formatted sql
-- changeset argus:11
alter table bot_user
    add column user_role varchar(30) not null default 'USER';

