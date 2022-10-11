use StardewHoes10
go

create procedure insert_Item (
	@Name varchar(20),
	@Quality tinyint,
	@BasePrice int,
	@ID int = null output
) as

declare @Status int

if @Name is null or @Quality is null or @BasePrice is null begin
	print 'ERROR in insert_Item: Name, Quality, and BasePrice cannot be null.'
	return 1
end

insert into Item (Name, Quality, BasePrice)
values (@Name, @Quality, @BasePrice)
set @Status = @@ERROR
set @ID = @@IDENTITY

if @Status != 0 begin
	print 'ERROR in insert_Item: Failed to insert the item ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Item table.'
	return @Status
end

print 'insert_Item: Successfully inserted the item ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Item table.'
return 0

go