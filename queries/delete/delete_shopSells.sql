USE StardewHoes
GO
CREATE PROCEDURE delete_shopSells(
	@ShopName varchar(20),
	@ItemID int
) AS
	IF @ShopName IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ShopSells] WHERE ShopSells.ShopID = @ShopName AND ShopSells.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing shop + item relation', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].ShopSells WHERE [ShopSells].[ShopID] = @ShopName AND ShopSells.ItemID = @ItemID
	END

	RETURN 0
