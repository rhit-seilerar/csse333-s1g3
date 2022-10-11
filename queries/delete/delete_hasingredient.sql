USE StardewHoes10
GO
CREATE PROCEDURE delete_hasIngredient(
	@IngredientID int,
	@FoodID int
) AS
	IF @IngredientID IS NULL OR @FoodID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasIngredient] WHERE HasIngredient.[FoodID] = @FoodID AND HasIngredient.IngredientID = @IngredientID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID
	END

	RETURN 0
