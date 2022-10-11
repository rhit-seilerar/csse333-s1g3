USE StardewHoes10
GO
CREATE PROCEDURE delete_profession(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Profession] WHERE Profession.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Profession WHERE Profession.ID = @ID
		IF EXISTS(SELECT * FROM HasProfession WHERE HasProfession.ProfessionID = @ID)
		BEGIN
			DELETE FROM HasProfession WHERE HasProfession.ProfessionID = @ID
		END
	END

	RETURN 0
