USE StardewHoes10
GO
CREATE PROCEDURE delete_produce(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Produce] WHERE Produce.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing produce item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Produce WHERE [Produce].ID = @ID
		IF EXISTS (SELECT * FROM PlantProduct WHERE PlantProduct.ID = @ID)
		BEGIN
			DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
		END
		IF EXISTS (SELECT * FROM AnimalProduct WHERE AnimalProduct.ID = @ID)
		BEGIN
			DELETE FROM AnimalProduct WHERE AnimalProduct.ID = @ID
		END
		IF EXISTS (SELECT * FROM Generates WHERE Generates.ProduceID = @ID)
		BEGIN
			DELETE FROM Generates WHERE Generates.ProduceID = @ID
		END
		IF EXISTS (SELECT * FROM Produces WHERE Produces.ProductID = @ID)
		BEGIN
			DELETE FROM Produces WHERE Produces.ProductID = @ID
		END
	END

	RETURN 0
