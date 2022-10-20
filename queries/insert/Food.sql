use StardewHoes10
go

create or alter procedure insert_Food (
	@Name varchar(20) = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Food where ID = @ID) begin
		raiserror('ERROR in insert_Food: The food with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, null, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Food (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Food: Failed to insert the food %s with quality %d and price %d into the Food table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Food: Successfully inserted the food ' + @Name + ' with price ' + convert(varchar(40), @BasePrice) + ' into the Food table.'
go