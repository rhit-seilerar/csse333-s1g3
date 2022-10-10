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
delete from Nees

select * from Item
select * from Produce
select * from PlantProduct
select * from Villager
select * from Shopkeeper
select * from Shop
select * from ShopBuys
select * from ShopSells
select * from Needs

insert into Item values (0,0,0)
insert into Produce values (13)
insert into PlantProduct values (13, 'Fruit')
insert into Villager values ('a')
insert into Shopkeeper values (1, 0)
insert into Shop values (1,'a','','')
insert into ShopBuys values (1, 13)
insert into ShopSells values (1, 13)
insert into Needs values (1, 13, 1, 1)

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
