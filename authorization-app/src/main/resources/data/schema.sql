create table authusers (
  id bigint not null,
  username varchar(100) not null,
  password varchar(100) not null,
  firstname varchar(100) not null,
  lastname varchar(100) not null,
  email varchar(100),
  create_time timestamp,
  update_time timestamp
);

create table user_roles (
  user_id bigint not null,
  role varchar(100) not null
);