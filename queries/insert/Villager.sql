use StardewHoes10
go

create or alter procedure insert_Villager (
	@Name varchar(30) = null,
	@ID int = null output
) as
	declare @Status int

	if @Name is null begin
		raiserror('ERROR in insert_Villager: Name cannot be null.', 14, 1)
		return 1
	end
	
	insert into Villager (Name)
	values (@Name)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Villager: Failed to insert the villager %s into the Villager table.', 14, 1, @Name)
		return @Status
	end

	print 'insert_Villager: Successfully inserted the villager ' + @Name + ' into the Villager table.'
	return 0
go