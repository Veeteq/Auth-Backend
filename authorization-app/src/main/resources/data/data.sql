insert into authusers (user_id, username, password, firstname, lastname) values (1001, 'jmclane', '$2a$12$oeAreHh93nVs8KpK5.XcDeequ01puB898iBGZbr.bqUtfTxKjDgem', 'John', 'McLane');
insert into user_roles(user_id, role_name) values (1001, 'USER_ROLE');
insert into user_roles(user_id, role_name) values (1001, 'ACCOUNT_ADMIN');
insert into user_roles(user_id, role_name) values (1001, 'DOCUMENT_ADMIN');
insert into user_roles(user_id, role_name) values (1001, 'ITEM_ADMIN');