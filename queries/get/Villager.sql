USE StardewHoes
GO
CREATE PROCEDURE get_villager(
	@VillagerID int
) AS
	IF @VillagerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Villager] WHERE Villager.[ID] = @VillagerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT * FROM [dbo].Villager WHERE [Villager].ID = @VillagerID
	END

	RETURN 0
