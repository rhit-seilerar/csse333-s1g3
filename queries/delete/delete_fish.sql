USE StardewHoes10
GO
CREATE PROCEDURE delete_fish(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Fish] WHERE Fish.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing fish', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in fish, delete it
		DELETE FROM [dbo].Fish WHERE Fish.ID = @ID
	END

	RETURN 0
