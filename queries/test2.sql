use StardewHoes10
go

delete from Item
delete from Villager
delete from Profession

select * from Item


select Item.* from Item join Produce on Item.ID = Produce.ID

select Item.*, Season from Item join Seed on Item.ID = Seed.ID

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Fruit'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Vegetable'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Flower'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Forage'

select Item.* from Item join Animal on Item.ID = Animal.ID

