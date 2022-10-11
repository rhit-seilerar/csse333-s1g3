use StardewHoes
go

delete from Item
delete from Produce
delete from PlantProduct
delete from Villager
delete from Shopkeeper
delete from Shop
delete from ShopBuys
delete from ShopSells
delete from Needs

insert into Item values (0,0,0)
insert into Produce values (13)
insert into PlantProduct values (13, 'Fruit')
insert into Villager values ('a')
insert into Villager values ('d')
insert into Shopkeeper values (1, 0)
insert into Shopkeeper values (2, 0)
insert into Shop values (1,'a','','')
insert into Shop values (2,'b','','')
insert into ShopBuys values (1, 13)
insert into ShopSells values (1, 13)
insert into Needs values (1, 13, 1, 1)
insert into Seed values (13, 'Spring')
insert into ArtisanGood values (13, 1)
insert into Farm values ('Spring','f')
insert into Farmer values (1, 1)
insert into Profession values ('c', 1)

select * from Item
select * from Produce
select * from PlantProduct
select * from Villager
select * from Shopkeeper
select * from Shop
select * from ShopBuys
select * from ShopSells
select * from Needs
select * from Seed

update Villager set Name = 'a' where ID = 1
update Shopkeeper set IsDeleted = 1 where ID = 1

exec get_Item null
exec get_Item 0
exec get_Item 13

exec get_Produce null
exec get_Produce 0
exec get_Produce 13

exec get_PlantProduct null
exec get_PlantProduct 0
exec get_PlantProduct 13

exec get_Shopkeeper null
exec get_Shopkeeper 0
exec get_Shopkeeper 1

exec get_Shop null
exec get_Shop 0
exec get_Shop 1

exec update_Villager null, 'b'
exec update_Villager 1, null
exec update_Villager 0, 'b'
exec update_Villager 1, 'b'

exec update_Shopkeeper null
exec update_Shopkeeper 1
exec update_Shopkeeper 1, null, null
exec update_Shopkeeper 0, 'c', 0
exec update_Shopkeeper 1, null, 0
exec update_Shopkeeper 1, 'c', null
exec update_Shopkeeper 1, 'c', 0

exec update_Shop null
exec update_Shop 0, 's', '', ''
exec update_Shop 2, 'a', '', ''
exec update_Shop 1
exec update_Shop 1, null, null, null
exec update_Shop 1, null, null, 'h'
exec update_Shop 1, null, 'd', null
exec update_Shop 1, 'a', null, null
exec update_Shop 1, 'a', '', ''

exec update_Item null
exec update_Item 0, 0,0,0
exec update_Item 13
exec update_Item 13, null, null, null
exec update_Item 13, null, null, 0
exec update_Item 13, null, 0, null
exec update_Item 13, 0, null, null
exec update_Item 13, 0,0,0

exec update_Produce null
exec update_Produce 0, 0,0,0
exec update_Produce 13
exec update_Produce 13, null, null, null
exec update_Produce 13, null, null, 0
exec update_Produce 13, null, 0, null
exec update_Produce 13, 0, null, null
exec update_Produce 13, 0,0,0

exec update_PlantProduct null
exec update_PlantProduct 0, 0,0,0,'Fruit'
exec update_PlantProduct 13
exec update_PlantProduct 13, null, null, null, null
exec update_PlantProduct 13, null, null, null, 'FFruit'
exec update_PlantProduct 13, null, null, null, 'Fruit'
exec update_PlantProduct 13, null, null, 0, null
exec update_PlantProduct 13, null, 0, null, null
exec update_PlantProduct 13, 0, null, null, null
exec update_PlantProduct 13, 0,0,0,'Fruit'

exec update_Needs null, null
exec update_Needs 1, null
exec update_Needs null, 13
exec update_Needs 1,13
exec update_Needs 0,13, 1, 1
exec update_Needs 1,14, 1, 1
exec update_Needs 1,13, null, null
exec update_Needs 1,13, 1, null
exec update_Needs 1,13, null, 1
exec update_Needs 1,13, 1, 1

exec update_Seed null
exec update_Seed 0, 0,0,0,'Spring'
exec update_Seed 13
exec update_Seed 13, null, null, null, null
exec update_Seed 13, null, null, null, 'SSpring'
exec update_Seed 13, null, null, null, 'Spring'
exec update_Seed 13, null, null, 0, null
exec update_Seed 13, null, 0, null, null
exec update_Seed 13, 0, null, null, null
exec update_Seed 13, 0,0,0,'Spring'

exec update_ArtisanGood null
exec update_ArtisanGood 0, 0,0,0,'1'
exec update_ArtisanGood 13
exec update_ArtisanGood 13, null, null, null, null
exec update_ArtisanGood 14, null, null, null, '1'
exec update_ArtisanGood 13, null, null, null, '1'
exec update_ArtisanGood 13, null, null, 0, null
exec update_ArtisanGood 13, null, 0, null, null
exec update_ArtisanGood 13, 0, null, null, null
exec update_ArtisanGood 13, 0,0,0,'1'

exec update_Farm null
exec update_Farm 1
exec update_Farm 1, null, null
exec update_Farm 0, 'Spring', 0
exec update_Farm 1, null, 0
exec update_Farm 1, 'Spring', null
exec update_Farm 1, 'SSpring', null
exec update_Farm 1, 'Spring', 0

exec update_Farmer null, null
exec update_Farmer null, 1
exec update_Farmer 1, null
exec update_Farmer 0, 1
exec update_Farmer 1, 0
exec update_Farmer 1, 1

exec update_Profession null
exec update_Profession 1
exec update_Profession 1, null, null
exec update_Profession 0, 'c', 0
exec update_Profession 1, null, 0
exec update_Profession 1, 'c', null
exec update_Profession 1, 'c', 0
