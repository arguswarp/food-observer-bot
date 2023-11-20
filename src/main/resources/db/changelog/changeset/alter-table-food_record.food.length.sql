-- liquibase formatted sql
-- changeset argus:4
alter table food_record
    alter column food type text using food::varchar(300);

