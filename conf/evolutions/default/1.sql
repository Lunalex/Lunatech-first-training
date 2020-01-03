

# --- !Ups

create table product (
  ean                           varchar(255) not null,
  name                          varchar(255),
  description                   varchar(255),
  picture                       varbinary(883647),
  constraint pk_product primary key (ean)
);


# --- !Downs

drop table if exists product;

