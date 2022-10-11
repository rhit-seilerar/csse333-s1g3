use StardewHoes10
go

create procedure get_ShopBuys (
	@ShopID int,
	@ItemID int
) as begin
	select *
	from ShopBuys
	where ShopID = @ShopID and ItemID = @ItemID
end
go