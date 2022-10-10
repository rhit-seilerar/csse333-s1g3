use StardewHoes
go

create procedure insert_PlantProduct (
	@Name varchar(20),
	@Quality tinyint,
	@BasePrice int,
	@Type varchar(20),
	@ID int = null output
) as

if @Type is null begin
	print 'ERROR in insert_PlantProduct: Type cannot be null'
	return 1
end
if @Type not in ('Fruit', 'Vegetable', 'Forage', 'Flower') begin
	print 'ERROR in insert_PlantProduct: Type must be one of ''Fruit'', ''Vegetable'', ''Flower'', or ''Forage''.'
	return 2
end

declare @Status int
execute @Status = insert_Produce @Name, @Quality, @BasePrice, @ID output
if @Status != 0 begin return @Status end

insert into PlantProduct (ID, Type)
values (@ID, @Type)
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in insert_PlantProduct: Failed to insert the produce ' + @Name + ' (' + @Type + ') with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the PlantProduct table.'
	return @Status
end

print 'insert_PlantProduct: Successfully inserted the produce ' + @Name + ' (' + @Type + ') with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the PlantProduct table.'
return 0

go