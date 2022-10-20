use StardewHoes10
go

create or alter procedure insert_Item (
	@Name varchar(20),
	@Quality tinyint = null,
	@BasePrice int,
	@ID int = null output
) as
	declare @Status int

	if @Name is null or @BasePrice is null begin
		raiserror('ERROR in insert_Item: Name, Quality, and BasePrice cannot be null.', 14, 1)
		return 1
	end
	
	insert into Item (Name, Quality, BasePrice)
	values (@Name, @Quality, @BasePrice)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Item: Failed to insert the item %s with quality %d and price %d into the Item table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Item: Successfully inserted the item ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Item table.'
	return 0
go