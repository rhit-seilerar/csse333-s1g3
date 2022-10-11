USE StardewHoes
GO
CREATE PROCEDURE delete_farmer(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farmer] WHERE Farmer.[VillagerID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
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

	RETURN 0
