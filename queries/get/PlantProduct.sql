use StardewHoes10
go

create or alter procedure get_PlantProduct (
	@ID int = null,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Type varchar(20) = null
) as
	declare @Status int

	select I.*, Type
	from PlantProduct P
	join Item I on P.ID = I.ID
	where (@ID is null or P.ID = @ID) and (@Name is null or Name = @Name) and (@Quality is null or Quality = @Quality) and (@BasePrice is null or BasePrice = @BasePrice)
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in get_PlantProduct: Failed to retrieve the requested data.'
		return @Status
	end

	print 'get_PlantProduct: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
go