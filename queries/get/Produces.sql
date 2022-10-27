use StardewHoes10
go

create or alter procedure get_Produces (
	@AnimalID int,
	@ProductID int
) as begin
	select *
	from Produces
	where AnimalID = @AnimalID and ProductID = @ProductID
end
go