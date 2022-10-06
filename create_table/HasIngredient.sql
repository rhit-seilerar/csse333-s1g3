USE StardewHoes
GO
CREATE TABLE HasIngredient(
	IngredientID int NOT NULL,
	FoodID int NOT NULL,
	PRIMARY KEY(IngredientID, FoodID),
	FOREIGN KEY(IngredientID) REFERENCES Item(ID),
	FOREIGN KEY(FoodID) REFERENCES Food(ItemID))