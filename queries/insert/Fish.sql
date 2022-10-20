use StardewHoes10
go

create or alter procedure insert_Fish (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Fish where ID = @ID) begin
		raiserror('ERROR in insert_Fish: The fish with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Fish (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Fish: Failed to insert the fish %s with quality %d and price %d into the Fish table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Fish: Successfully inserted the fish ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Fish table.'
go