USE StardewHoes
GO
CREATE PROCEDURE delete_farm(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farm] WHERE Farm.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Farm WHERE Farm.ID = @ID
		IF EXISTS (SELECT * FROM Farmer WHERE Farmer.FarmID = @ID)
		BEGIN
			DELETE FROM Farmer WHERE Farmer.FarmID = @ID
		END
	END

	RETURN 0
