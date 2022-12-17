
create role sustcmanager;
create role seaportofficer;
create role courier;
create role seaportofficer;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO sustcmanager;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO courier;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;

grant select on sustc2.public.undertake,city,company,container,staff,ship,record to sustcmanager,courier,seaportofficer,companymanager;
grant all on undertake, record to courier;
grant update on record to seaportofficer, courier;
grant update on record,container,ship to companymanager;

set role postgres;
show role ;