create database filedetail_db;
create table if not exists file_detail
(
    id integer not null
        constraint file_pk
            primary key,
    file_name varchar not null,
    emails varchar not null
);

alter table file_detail owner to postgres;
create unique index if not exists file_detail_name_uindex
    on file_detail (file_name);
