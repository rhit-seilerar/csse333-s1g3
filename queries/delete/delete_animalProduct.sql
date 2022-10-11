USE StardewHoes10
GO
CREATE PROCEDURE delete_animalproduct(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[AnimalProduct] WHERE AnimalProduct.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].AnimalProduct WHERE [AnimalProduct].ID = @ID
		IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
		BEGIN
			DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
		END
	END

	RETURN 0
