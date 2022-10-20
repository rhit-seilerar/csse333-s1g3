use StardewHoes10
go

create or alter procedure insert_AnimalProduct (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	
	if @ID is not null and exists (select * from AnimalProduct where ID = @ID) begin
		raiserror('ERROR in insert_AnimalProduct: The product with ID %d already exists.', 14, 1, @ID)
		return 3
	end

	declare @Status int

	if @ID is null or not exists (select * from Produce where ID = @ID) begin
		execute @Status = insert_Produce @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into AnimalProduct (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_AnimalProduct: Failed to insert the produce %s with quality %d and price %d into the AnimalProduct table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_AnimalProduct: Successfully inserted the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the AnimalProduct table.'
go