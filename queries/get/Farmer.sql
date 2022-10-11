USE StardewHoes
GO
CREATE PROCEDURE get_farmer(
	@FarmerID int
) AS
	IF @FarmerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farmer] WHERE Farmer.[VillagerID] = @FarmerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM [dbo].Farmer JOIN Villager ON Villager.ID = Farmer.VillagerID
		WHERE [Farmer].VillagerID = @FarmerID
	END

	RETURN 0
