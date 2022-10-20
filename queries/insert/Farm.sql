use StardewHoes10
go

create or alter procedure insert_Farm (
	@Name varchar(30) = null,
	@Season varchar(6) = null,
	@ID int = null output
) as
	declare @Status int

	if @Name is null or @Season is null begin
		raiserror('ERROR in insert_Farm: Name and Season cannot be null.', 14, 1)
		return 1
	end
	
	insert into Farm (Name, Season)
	values (@Name, @Season)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Farm: Failed to insert the farm %s with season %s into the Farm table.', 14, 1, @Name, @Season)
		return @Status
	end

	print 'insert_Farm: Successfully inserted the farm ' + @Name + ' with season ' + @Season + ' into the Farm table.'
	return 0
go