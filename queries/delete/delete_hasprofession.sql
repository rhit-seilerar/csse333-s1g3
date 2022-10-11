USE StardewHoes10
GO
CREATE PROCEDURE delete_hasProfession(
	@professionID int,
	@farmerID int
) AS
	IF @farmerID IS NULL OR @professionID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasProfession] WHERE HasProfession.[ProfessionID] = @professionID AND HasProfession.FarmerID = @farmerID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM HasProfession WHERE HasProfession.ProfessionID = @professionID AND HasProfession.FarmerID = @farmerID
	END

	RETURN 0
