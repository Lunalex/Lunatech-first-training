# --- First database schema
# --- !Ups
insert into product(ean,name,description) values ('12340000', 'Chaise Kullaberg', 'Une chaise pivotante en bois avec 4 pieds, support en mousse souple et confortable.');
insert into product(ean,name,description) values ('78541010', 'Chaise Millverg', 'Chaise de bureau pivotante en cuir plastifié véritable, support en PVC laqué.');
insert into product(ean,name,description) values ('95601310', 'Table de salon', 'Table 8 couverts en chaine plastifié véritable.');
insert into product(ean,name,description) values ('78402603', 'Billy étagère', 'La fameuse étagère blanche où il reste toujours une vis à la fin et on ne sait pas pourquoi.');
insert into product(ean,name,description) values ('75050240', 'Balkon', 'Tabouret en PVC');
insert into product(ean,name,description) values ('34201310', 'Fauteuil Eknéäset', 'Fauteuil en poil de stölku, pied en bois.');
# --- !Downs
delete from product;