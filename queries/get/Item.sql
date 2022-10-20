use StardewHoes10
go

create or alter procedure get_Item (
	@ID int = null,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	declare @Status int

	select ID, Name, Quality, BasePrice
	from Item
	where (@ID is null or ID = @ID) and (@Name is null or Name = @Name) and (@Quality is null or Quality = @Quality) and (@BasePrice is null or BasePrice = @BasePrice)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s with quality %d and price %d.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'get_Item: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
go