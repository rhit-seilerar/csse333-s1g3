use StardewHoes10
go

create or alter procedure insert_Shop (
	@OwnerID int,
	@Name varchar(20),
	@Address varchar(100),
	@Schedule varchar(100)
) as
	declare @Status int

	if @OwnerID is null or @Name is null or @Address is null or @Schedule is null begin
		raiserror('ERROR in insert_Shop: OwnerID, Name, Address, and BasePrice cannot be null.', 14, 1)
		return 1
	end
	
	insert into Shop(OwnerID, Name, Address, Schedule)
	values (@OwnerID, @Name, @Address, @Schedule)
	set @Status = @@ERROR

	if @Status != 0 begin
		raiserror('ERROR in insert_Shop: Failed to insert the Shop %s with Address %s into the Shop table.', 14, 1, @Name, @Address)
		return @Status
	end

	print 'insert_Shop: Successfully inserted the Shop ' + @Name + ' with Address ' + @Address + ' into the Shop table.'
	return 0
go