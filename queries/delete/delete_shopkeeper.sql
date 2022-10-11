USE StardewHoes
GO
CREATE PROCEDURE delete_shopkeeper(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Shopkeeper] WHERE Shopkeeper.ID = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing shopkeeper', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].[Shopkeeper] WHERE [Shopkeeper].ID = @ID
		DELETE FROM [dbo].[Villager] WHERE [Villager].ID = @ID
		DELETE FROM [dbo].[Shop] WHERE [Shop].OwnerID = @ID
	END

	RETURN 0
