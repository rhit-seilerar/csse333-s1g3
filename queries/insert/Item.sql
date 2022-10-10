use StardewHoes
go

create procedure insert_Item (
	@Name varchar(20) = null,
	@Quality tinyint = 0,
	@BasePrice int = 0
) as

if @Name is null begin
	print 'ERROR in insert_Item: Name cannot be null.'
	return 1
end

insert into Item (Name, Quality, BasePrice)
values (@Name, @Quality, @BasePrice)

if @@ERROR != 0 begin
	print 'ERROR in insert_Item: Failed to insert the item ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Item table.'
	return @@ERROR
end

go