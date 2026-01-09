drop table if exists user_roles;
drop table if exists authusers;

create table authusers (
  user_id bigint not null,
  username varchar(100) not null,
  password varchar(255) not null,
  email varchar(100),
  firstname varchar(100) not null,
  lastname varchar(100) not null,
  enabled boolean default false,
  create_time timestamp,
  update_time timestamp
);
create unique index authusers_idx on authusers (user_id);
create unique index authusers_username_idx on authusers (username);
create unique index authusers_email_idx on authusers (email);
alter table authusers add constraint authusers_pk primary key (user_id);
alter table authusers add constraint authusers_username_uq unique (username);
alter table authusers add constraint authusers_email_uq unique (email);

create table user_roles (
  user_id bigint not null,
  role_name varchar(64) not null
);
alter table user_roles add constraint user_roles_user_fk foreign key (user_id) references authusers (user_id);