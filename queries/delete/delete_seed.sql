USE StardewHoes
GO
CREATE PROCEDURE delete_seed(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Seed] WHERE Seed.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing seed', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Seed WHERE [Seed].ID = @ID
	END

	RETURN 0
