USE StardewHoes10
GO
CREATE PROCEDURE delete_produces(
	@AnimalID int,
	@ProductID int
) AS
	IF @AnimalID IS NULL OR @ProductID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Produces] WHERE Produces.[AnimalID] = @AnimalID AND Produces.ProductID = @ProductID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Produces WHERE Produces.AnimalID = @AnimalID AND Produces.ProductID = @ProductID
	END

	RETURN 0
