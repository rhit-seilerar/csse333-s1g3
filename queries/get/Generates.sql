use StardewHoes10
go

create or alter procedure get_Generates (
	@ProduceID int,
	@ProductID int
) as begin
	select *
	from Generates
	where ProduceID = @ProduceID and ProductID = @ProductID
end
go