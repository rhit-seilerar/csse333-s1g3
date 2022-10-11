use StardewHoes10
go

create procedure get_Needs (
	@VillagerID int,
	@ItemID int
) as begin
	select *
	from Needs
	where VillagerID = @VillagerID and ItemID = @ItemID
end
go