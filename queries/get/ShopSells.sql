use StardewHoes
go

create procedure get_ShopSells (
	@ShopID int,
	@ItemID int
) as begin
	select *
	from ShopSells
	where ShopID = @ShopID and ItemID = @ItemID
end
go