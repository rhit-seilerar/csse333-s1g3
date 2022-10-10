use StardewHoes
go

create procedure insert_Produce (
	@Name varchar(20) = null,
	@Quality tinyint = 0,
	@BasePrice int = 0
) as

if @Name is null begin
	print 'ERROR in insert_Produce: Name cannot be null.'
	return 1
end

execute insert_Item @Name, @Quality, @BasePrice
if @@ERROR != 0 begin
	return @@ERROR
end

declare @ID int;

select @ID = ID from Item where Name = @Name and Quality = @Quality and BasePrice = @BasePrice

insert into Produce (ID)
values (@ID)

if @@ERROR != 0 begin
	print 'ERROR in insert_Produce: Failed to insert the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
	return @@ERROR
end

go