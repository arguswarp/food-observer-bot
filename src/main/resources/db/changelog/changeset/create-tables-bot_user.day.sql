-- liquibase formatted sql
-- changeset argus:1
create table bot_user
(
    id   bigint primary key generated by default as identity,
    name varchar(100),
    user_state varchar(30),
    telegram_id bigint
);

create table day
(
    id         bigint primary key generated by default as identity,
    user_id    int not null references bot_user (id),
    date       date unique,
    is_bloody  boolean,
    is_pimple  boolean
);

alter table food_record
    add day_id integer;

alter table food_record
    add constraint food_record_day_id_fk
        foreign key (day_id) references day (id);

alter table food_record
    drop column is_bloody;

alter table food_record
    drop column is_pimple;

alter table food_record
    drop column username;