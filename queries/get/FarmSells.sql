USE StardewHoes
GO
CREATE PROCEDURE get_farmsells (
	@FarmerID int,
	@ItemID int
) AS
	IF @FarmerID IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[FarmSells] WHERE FarmSells.FarmerID = @FarmerID AND FarmSells.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM FarmSells JOIN Farmer ON Farmer.VillagerID = FarmSells.FarmerID
		JOIN Villager ON Farmer.VillagerID = Villager.ID
		JOIN Item ON Item.ID = FarmSells.ItemID
		WHERE FarmSells.FarmerID = @FarmerID AND FarmSells.ItemID = @ItemID
	END
	RETURN 0
GO