drop table if exists refresh_tokens;
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

drop sequence if exists authuser_seq;
create sequence authuser_seq start with 1 increment by 1 nocache;

create table user_roles (
  user_id bigint not null,
  role_name varchar(64) not null
);
alter table user_roles add constraint user_roles_user_fk foreign key (user_id) references authusers (user_id);

create table refresh_tokens (
  token_id bigint not null,
  token varchar(512) not null,
  user_id bigint not null,
  revoked boolean default false,
  create_time timestamp,
  expires_at timestamp,
  update_time timestamp
);
create unique index refresh_tokens_idx on refresh_tokens (token_id);
create unique index refresh_tokens_token_idx on refresh_tokens (token);
alter table refresh_tokens add constraint refresh_tokens_pk primary key (token_id);
alter table refresh_tokens add constraint refresh_tokens_token_uq unique (token);
alter table refresh_tokens add constraint refresh_token_user_fk foreign key (user_id) references authusers (user_id);