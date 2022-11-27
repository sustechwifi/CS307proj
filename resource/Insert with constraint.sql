create table company(
    name varchar(30) unique not null,
    id serial primary key
);

create table city(
    name varchar(20) unique not null,
    id serial primary key
);

create table ship(
    name varchar(20) unique not null,
    state int,
    --0 Docking  1 Shipping
    company_id int not null,
    city_id int,
    id serial primary key,
    foreign key (company_id) references company(id),
    foreign key (city_id) references city(id)
);

create table container(
    code varchar(15) unique not null,
    type varchar(30) not null,
    state int,
    --0 Unpacked  1 Packed  2 Shipping
    ship_id int,
    city_id int,
    id serial primary key,
    foreign key (city_id) references city(id),
    foreign key (ship_id) references ship(id)
);


create table staff(
    phone bigint,
    name varchar(20) unique not null,
    birth_year int,
    gender int8,
    --0 for female
    --1 for male
    type int not null,
    --1 for SUSTC Department Manager
    --2 for Company Manager
    --3 for Seaport Officer
    --4 for Courier
    password varchar(20) not null,
    id serial primary key,

    company_id int,
    city_id int,
    foreign key (company_id) references company(id),
    foreign key (city_id) references city(id)
);

create table record(
    item_name varchar(18) unique not null,
    item_class varchar(18) not null,
    item_price bigint not null,
    state int,
    --0 Start  1 Pickup  2 To-export Transporting  3 Export Checking
    --4 Packing to Container  5 Waiting for Shipping  6 Shipping
    --7 Unpacking for Container  8 Import Checking  9 From-Import Transporting
    --10 Delivering  11 Finish
    --103 Export Check Fail
    --108 Import Check Fail
    company_id int not null,
    container_id int,
    id serial primary key,
    foreign key (container_id) references container(id),
    foreign key (company_id) references company(id)
);

create table undertake(
    type int not null,
    --type = 1: Retrieval
    --type = 2: Export Transportation
    --type = 3: Export Checking (Tax)
    --type = 4: Import Checking (Tax)
    --type = 5: Import Transporting
    --type = 6: Delivery
    record_id int not null,
    staff_id int,
    city_id int,
    tax numeric(24,6),

    id serial primary key,
    unique(record_id,staff_id,type),
    foreign key (staff_id) references staff(id),
    foreign key (record_id) references record(id),
    foreign key (city_id) references city(id)
);


-- select * from city;
-- drop table handle,city,company,container,courier,transit,ship,record;
-- truncate handle,city,company,container,courier,transit,ship,record cascade ;