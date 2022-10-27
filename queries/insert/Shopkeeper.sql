use StardewHoes10
go

create or alter procedure insert_Shopkeeper (
	@Name varchar(20) = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Shopkeeper where ID = @ID) begin
		raiserror('ERROR in insert_Shopkeeper: The Shopkeeper with ID %d already exists.', 14, 1, @ID)
		return 1
	end

	
	declare @Status int

	if @ID is null or not exists (select * from Villager where ID = @ID) begin
		execute @Status = insert_Villager @Name, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Shopkeeper (ID, IsDeleted)
	values (@ID, 0)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Shopkeeper: Failed to insert the Shopkeeper %s  into the Shopkeeper table.', 14, 1, @Name)
		return @Status
	end

	print 'insert_Shopkeeper: Successfully inserted the Shopkeeper ' + @Name + ' into the Shopkeeper table.'
go