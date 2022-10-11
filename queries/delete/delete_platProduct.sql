USE StardewHoes10
GO
CREATE PROCEDURE delete_plantProduct(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[PlantProduct] WHERE PlantProduct.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing plant product item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
	END

	RETURN 0
