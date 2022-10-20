use StardewHoes10
go

create or alter procedure insert_Animal (
	@Name varchar(20) = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Animal where ID = @ID) begin
		raiserror('ERROR in insert_Animal: The animal with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, null, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Animal (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Animal: Failed to insert the animal %s with price %d into the Animal table.', 14, 1, @Name, @BasePrice)
		return @Status
	end

	print 'insert_Animal: Successfully inserted the animal ' + @Name + ' with price ' + convert(varchar(40), @BasePrice) + ' into the Animal table.'
go