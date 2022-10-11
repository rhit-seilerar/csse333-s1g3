USE StardewHoes
GO
CREATE PROCEDURE get_hasingredient (
	@FoodID int,
	@IngredientID int
) AS
	IF @FoodID IS NULL OR @IngredientID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasIngredient] WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM HasIngredient JOIN Food ON HasIngredient.FoodID = Food.ID
		JOIN Item ON Item.ID = Food.ID
		JOIN Item i2 ON i2.ID = HasIngredient.IngredientID
		WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID
	END
	RETURN 0
GO