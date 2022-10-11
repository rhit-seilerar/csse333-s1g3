USE StardewHoes
GO
CREATE PROCEDURE delete_animal(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Animal] WHERE Animal.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in animal, delete it + check Produces --Done
		DELETE FROM [dbo].Animal WHERE Animal.ID = @ID
		IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
		BEGIN
			DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
		END
	END

	RETURN 0
