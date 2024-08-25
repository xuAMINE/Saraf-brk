create table _user (
    id integer auto_increment primary key,
    enabled boolean not null,
    email varchar(255) unique,
    firstname varchar(255),
    lastname varchar(255),
    password varchar(255),
    role varchar(255) check (role in ('USER', 'ADMIN', 'MANAGER'))
);

create table exchange_rate (
    id integer auto_increment primary key,
    current_rate numeric(38, 10) not null
);

create table password_reset_token (
    id integer auto_increment primary key,
    user_id integer not null,
    expiration_time timestamp,
    token varchar(255),
    foreign key (user_id) references _user
);

create table recipient (
    id integer auto_increment primary key,
    do_contact boolean not null,
    is_active boolean not null,
    user_id integer,
    ccp varchar(255),
    firstname varchar(255),
    lastname varchar(255),
    phone_number varchar(255),
    unique (user_id, ccp),
    foreign key (user_id) references _user
);

create table token (
    id integer auto_increment primary key,
    expired boolean not null,
    revoked boolean not null,
    user_id integer,
    token varchar(255) unique,
    token_type varchar(255) check (token_type in ('BEARER')),
    foreign key (user_id) references _user
);

create table transfer (
    id integer auto_increment primary key,
    amount numeric(38, 2),
    amount_received numeric(38, 2),
    recipient_id integer,
    transfer_date date,
    user_id integer,
    receipt varchar(255),
    status varchar(255) check (status in ('RECEIVED', 'PROCESSING', 'PENDING', 'CANCELED')),
    foreign key (recipient_id) references recipient,
    foreign key (user_id) references _user
);

create table verification_token (
    id integer auto_increment primary key,
    user_id integer not null,
    created timestamp,
    expires timestamp,
    validated_at timestamp,
    token varchar(255),
    foreign key (user_id) references _user
);
