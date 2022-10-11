USE StardewHoes10
GO
CREATE PROCEDURE delete_villager(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Villager] WHERE Villager.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Villager WHERE Villager.ID = @ID
		IF EXISTS(SELECT * FROM Farmer WHERE Farmer.VillagerID = @ID)
		BEGIN
			DELETE FROM [dbo].Farmer WHERE [Farmer].VillagerID = @ID
			IF EXISTS(SELECT * FROM FarmSells WHERE FarmSells.FarmerID = @ID)
			BEGIN
				DELETE FROM [dbo].FarmSells WHERE [FarmSells].FarmerID = @ID
			END
			IF EXISTS(SELECT * FROM HasProfession WHERE HasProfession.FarmerID = @ID)
			BEGIN
				DELETE FROM [dbo].HasProfession WHERE [HasProfession].FarmerID = @ID
			END
		END
		IF EXISTS(SELECT * FROM Shopkeeper WHERE Shopkeeper.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Shopkeeper WHERE [Shopkeeper].ID = @ID
			DELETE FROM [dbo].Shop WHERE [Shop].OwnerID = @ID
			IF EXISTS(SELECT * FROM ShopSells WHERE ShopSells.ShopID = @ID)
			BEGIN
				DELETE FROM [dbo].ShopSells WHERE [ShopSells].ShopID = @ID
			END
			IF EXISTS(SELECT * FROM ShopBuys WHERE ShopBuys.ShopID = @ID)
			BEGIN
				DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].ShopID = @ID
			END
		END
	END

	RETURN 0
