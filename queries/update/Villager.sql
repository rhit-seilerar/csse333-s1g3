use StardewHoes10
go

create procedure update_Villager (
	@ID int,
	@Name varchar(30)
) as
	if @ID is null begin
		print 'ERROR in update_Villager: ID cannot be null.'
		return 1
	end
	if @Name is null begin
		print 'ERROR in update_Villager: Name cannot be null.'
		return 2
	end
	if not exists (select * from Villager where ID = @ID) begin
		print 'ERROR in update_Villager: The villager with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int
	update Villager
	set Name = @Name
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Villager: Could not update the name of the villager with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end

	print 'update_Villager: Successfully updated the data for the villager with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go