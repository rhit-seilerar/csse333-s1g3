USE StardewHoes
GO
CREATE PROCEDURE get_hasprofession(
	@ProfessionID int,
	@FarmerID int
) AS
	IF @ProfessionID IS NULL OR @FarmerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasProfession] WHERE HasProfession.[ProfessionID] = @ProfessionID AND HasProfession.FarmerID = @FarmerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM [dbo].HasProfession JOIN Profession ON HasProfession.ProfessionID = Profession.ID
		JOIN Farmer ON Farmer.VillagerID = HasProfession.FarmerID
		JOIN Villager ON Villager.ID = Farmer.VillagerID
		WHERE HasProfession.ProfessionID = @ProfessionID AND HasProfession.FarmerID = @FarmerID
	END

	RETURN 0
