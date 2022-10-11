use StardewHoes10
go

create procedure get_Item (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int

if @ID is null begin
	print 'ERROR in get_Item: ID cannot be null.'
	return 1
end

select @Name = Name, @Quality = Quality, @BasePrice = BasePrice
from Item
where ID = @ID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_Item: Failed to retrieve the data for the record with ID ' + convert(varchar(20), @ID) + '.'
	return @Status
end

if @Name is null begin
	print 'ERROR in get_Item: The data for the record with ID ' + convert(varchar(20), @ID) + ' does not exist.'
	return 1
end

print 'get_Item: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
go