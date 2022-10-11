USE StardewHoes10
GO
CREATE PROCEDURE delete_shopBuys(
	@ShopName varchar(20),
	@ItemID int
) AS
	IF @ShopName IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ShopBuys] WHERE ShopBuys.ShopID = @ShopName AND ShopBuys.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing shop + item relation', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].[ShopID] = @ShopName AND ShopBuys.ItemID = @ItemID
	END

	RETURN 0
