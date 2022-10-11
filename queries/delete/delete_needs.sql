USE StardewHoes10
GO
CREATE PROCEDURE delete_needs(
	@ItemID int,
	@VillagerID int
) AS
	IF @ItemID IS NULL OR @VillagerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Needs] WHERE Needs.[VillagerID] = @VillagerID AND Needs.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing row in the needs relationship', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Needs WHERE [Needs].VillagerID = @VillagerID
		DELETE FROM [dbo].Needs WHERE [Needs].ItemID = @ItemID
	END

	RETURN 0
