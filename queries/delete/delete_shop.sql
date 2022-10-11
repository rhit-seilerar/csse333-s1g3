USE StardewHoes
GO
CREATE PROCEDURE delete_shop(
	@Name varchar(20)
) AS
	IF @Name IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Shop] WHERE Shop.[Name] = @Name)
	BEGIN
		RAISERROR('Must try to delete an existing shop', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Shopkeeper WHERE [Shopkeeper].ID = (SELECT Shop.OwnerID FROM Shop WHERE Shop.[Name] = @Name)
		DELETE FROM [dbo].Villager WHERE [Villager].ID = (SELECT Shop.OwnerID FROM Shop WHERE Shop.[Name] = @Name)
		DELETE FROM [dbo].[Shop] WHERE [Shop].[Name] = @Name
	END

	RETURN 0
