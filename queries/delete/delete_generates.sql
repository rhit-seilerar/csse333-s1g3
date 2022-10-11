USE StardewHoes10
GO
CREATE PROCEDURE delete_generates(
	@ProduceID int,
	@ProductID int
) AS
	IF @ProduceID IS NULL OR @ProductID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Generates] WHERE Generates.[ProduceID] = @ProduceID AND Generates.ProductID = @ProductID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Generates WHERE Generates.ProduceID = @ProduceID AND Generates.ProductID = @ProductID
	END

	RETURN 0
