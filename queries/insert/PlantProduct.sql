use StardewHoes10
go

create or alter procedure insert_PlantProduct (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Type varchar(20),
	@ID int = null output
) as
	if @Type is null begin
		raiserror('ERROR in insert_PlantProduct: Type cannot be null', 14, 1)
		return 1
	end
	if @Type not in ('Fruit', 'Vegetable', 'Forage', 'Flower') begin
		raiserror('ERROR in insert_PlantProduct: Type must be one of ''Fruit'', ''Vegetable'', ''Flower'', or ''Forage''.', 14, 1)
		return 2
	end
	if @ID is not null and exists (select * from PlantProduct where ID = @ID) begin
		raiserror('ERROR in insert_PlantProduct: The product with ID %d already exists.', 14, 1, @ID)
		return 3
	end

	declare @Status int

	if @ID is null or not exists (select * from Produce where ID = @ID) begin
		execute @Status = insert_Produce @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into PlantProduct (ID, Type)
	values (@ID, @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_PlantProduct: Failed to insert the produce %s (%s) with quality %d and price %d into the PlantProduct table.', 14, 1, @Name, @Type, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_PlantProduct: Successfully inserted the produce ' + @Name + ' (' + @Type + ') with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the PlantProduct table.'
go