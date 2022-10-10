use StardewHoes
go

create procedure insert_PlantProduct (
	@Name varchar(20) = null,
	@Quality tinyint = 0,
	@BasePrice int = 0,
	@Type varchar(20) = null
) as

if @Name is null begin
	print 'ERROR in insert_PlantProduct: Name cannot be null.'
	return 1
end
if @Type is null begin
	print 'ERROR in insert_PlantProduct: Type cannot be null.'
	return 2
end

execute insert_Produce @Name, @Quality, @BasePrice
if @@ERROR != 0 begin
	return @@ERROR
end

declare @ID int;
select @ID = ID from Item where Name = @Name and Quality = @Quality and BasePrice = @BasePrice

insert into PlantProduct (ID, Type)
values (@ID, @Type)

if @@ERROR != 0 begin
	print 'ERROR in insert_PlantProduct: Failed to insert the product ' + @Name + ' (' + @Type + ') with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
	return @@ERROR
end

go