use StardewHoes10
go

create or alter procedure insert_Produce (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Produce where ID = @ID) begin
		raiserror('ERROR in insert_Produce: The produce with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Produce (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Produce: Failed to insert the produce %s with quality %d and price %d into the Produce table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Produce: Successfully inserted the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
go