use StardewHoes
go

create procedure get_PlantProduct (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output,
	@Type varchar(20) = null output
) as

declare @Status int

if @ID is null begin
	print 'ERROR in get_PlantProduct: ID cannot be null.'
	return 1
end

select @Name = Name, @Quality = Quality, @BasePrice = BasePrice, @Type = Type
from PlantProduct
join Item on PlantProduct.ID = Item.ID
where PlantProduct.ID = @ID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_PlantProduct: Failed to retrieve the data for the record with ID ' + convert(varchar(20), @ID) + '.'
	return @Status
end

if @Type is null begin
	print 'ERROR in get_PlantProduct: Failed to retrieve the type for the record with ID ' + convert(varchar(20), @ID) + '.'
	return 1
end

print 'get_PlantProduct: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
go