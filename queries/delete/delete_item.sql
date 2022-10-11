USE StardewHoes10
GO
CREATE PROCEDURE delete_item(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Item] WHERE Item.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Item WHERE [Item].ID = @ID
		--If it's in shopsells, delete it
		IF EXISTS(SELECT * FROM ShopSells WHERE ShopSells.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].ShopSells WHERE [ShopSells].ItemID = @ID
		END
		--If it's in shopbuys, delete it
		IF EXISTS(SELECT * FROM ShopBuys WHERE ShopBuys.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].ItemID = @ID
		END
		--If it's in produce, delete it + check if it's in PlantProduct, AnimalProduct, Generates --Done
		IF EXISTS(SELECT * FROM Produce WHERE Produce.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Produce WHERE [Produce].ID = @ID
			IF EXISTS(SELECT * FROM PlantProduct WHERE PlantProduct.ID = @ID)
			BEGIN
				DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
			END
			IF EXISTS(SELECT * FROM AnimalProduct WHERE AnimalProduct.ID = @ID)
			BEGIN
				DELETE FROM [dbo].AnimalProduct WHERE [AnimalProduct].ID = @ID
				IF EXISTS(SELECT * FROM Produces WHERE Produces.ProductID = @ID)
				BEGIN
					DELETE FROM [dbo].Produces WHERE Produces.ProductID = @ID
				END
			END
			IF EXISTS(SELECT * FROM Generates WHERE Generates.ProduceID = @ID)
			BEGIN
				DELETE FROM Generates WHERE Generates.ProduceID = @ID
			END
		END
		--If it's in food, delete it + check if it's in HasIngredient --Done
		IF EXISTS(SELECT * FROM Food WHERE Food.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Food WHERE [Food].ID = @ID
			IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.FoodID = @ID)
			BEGIN
				DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @ID
			END
		END
		--If it's in Seed, delete it
		IF EXISTS(SELECT * FROM Seed WHERE Seed.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Seed WHERE [Seed].ID = @ID
		END
		--If it's in fish, delete it
		IF EXISTS(SELECT * FROM Fish WHERE Fish.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Fish WHERE Fish.ID = @ID
		END
		--If it's in artisan good, delete it + check Generates --Done
		IF EXISTS(SELECT * FROM ArtisanGood WHERE ArtisanGood.ID = @ID)
		BEGIN
			DELETE FROM [dbo].ArtisanGood WHERE ArtisanGood.ID = @ID
			IF EXISTS(SELECT * FROM Generates WHERE Generates.ProductID = @ID)
			BEGIN
				DELETE FROM Generates WHERE Generates.ProductID = @ID
			END
		END
		--If it's in animal, delete it + check Produces --Done
		IF EXISTS(SELECT * FROM Animal WHERE Animal.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Animal WHERE Animal.ID = @ID
			IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
			BEGIN
				DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
			END
		END
		--If it's in farm selss, delete it
		IF EXISTS(SELECT * FROM FarmSells WHERE FarmSells.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].FarmSells WHERE FarmSells.ItemID = @ID
		END
		--If it's in hasingredient, delete it
		IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.IngredientID = @ID)
		BEGIN
			DELETE FROM [dbo].HasIngredient WHERE HasIngredient.IngredientID = @ID
		END
	END

	RETURN 0
