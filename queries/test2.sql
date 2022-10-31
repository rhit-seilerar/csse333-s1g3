use StardewHoes10
go

delete from Item
delete from Villager
delete from Profession

select * from Item

select * from Villager

select * from Profession

select Item.* from Item join Produce on Item.ID = Produce.ID

select Item.*, Season from Item join Seed on Item.ID = Seed.ID

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Fruit'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Vegetable'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Flower'

select Item.*, Type from Item join PlantProduct on Item.ID = PlantProduct.ID where Type = 'Forage'

select Item.* from Item join AnimalProduct P on Item.ID = P.ID

select Item.* from Item join Animal on Item.ID = Animal.ID

select Item.* from Item join ArtisanGood P on Item.ID = P.ID

select item.* from Item join Food on Item.ID = Food.ID

select I.*, F.* from HasIngredient H join Item I on H.IngredientID = I.ID join Food F on H.FoodID = F.ID

select I.*, F.* from Generates H join Item I on H.ProduceID = I.ID join Item F on H.ProductID = F.ID where F.Name = 'Juice'

select A.*, D.* from Produces P join Item A on P.AnimalID = A.ID join Item D on P.ProductID = D.ID

select * from Shop

select * from ShopBuys

select * from Villager