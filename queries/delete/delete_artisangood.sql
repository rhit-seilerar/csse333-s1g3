USE StardewHoes
GO
CREATE PROCEDURE delete_artisangood(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ArtisanGood] WHERE ArtisanGood.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in artisan good, delete it + check Generates --Done
		DELETE FROM [dbo].ArtisanGood WHERE ArtisanGood.ID = @ID
		IF EXISTS(SELECT * FROM Generates WHERE Generates.ProductID = @ID)
		BEGIN
			DELETE FROM Generates WHERE Generates.ProductID = @ID
		END
	END

	RETURN 0
