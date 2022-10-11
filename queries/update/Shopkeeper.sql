use StardewHoes
go

create procedure update_Shopkeeper (
	@ID int,
	@Name varchar(30) = null,
	@IsDeleted bit = null
) as
	if @ID is null begin
		print 'ERROR in update_Shopkeeper: ID cannot be null.'
		return 1
	end
	if @Name is null and @IsDeleted is null begin
		print 'ERROR in update_Shopkeeper: At least one of Name or IsDeleted must be non-null.'
		return 2
	end
	if not exists (select * from Shopkeeper where ID = @ID) begin
		print 'ERROR in update_Shopkeeper: The Shopkeeper with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int

	if @Name is not null begin
		execute @Status = update_Villager @ID, @Name
		if @Status != 0 begin return @Status end
	end

	if @IsDeleted is not null begin
		update Shopkeeper
		set IsDeleted = @IsDeleted
		where ID = @ID
		set @Status = @@ERROR
		if @Status != 0 begin
			print 'ERROR in update_Shopkeeper: Could not update the deletion status of the Shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
			return @Status
		end
		print 'update_Shopkeeper: Successfully updated the data for the Shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
	end

	return 0
go