USE StardewHoes
GO
CREATE PROCEDURE delete_food(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Food] WHERE Food.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in food, delete it + check if it's in HasIngredient --Done
		DELETE FROM [dbo].Food WHERE [Food].ID = @ID
		IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.FoodID = @ID)
		BEGIN
			DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @ID
		END
	END

	RETURN 0
