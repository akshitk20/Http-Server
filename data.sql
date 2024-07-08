# connect to psql using docker container
psql -d http-server -U admin app -W

create table item(id SERIAL primary key, name varchar(255), description varchar(255));
