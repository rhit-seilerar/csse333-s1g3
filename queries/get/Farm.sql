USE StardewHoes
GO
CREATE PROCEDURE get_farm(
	@FarmID int
) AS
	IF @FarmID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farm] WHERE Farm.[ID] = @FarmID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT * FROM [dbo].Farm WHERE [Farm].ID = @FarmID
	END

	RETURN 0
