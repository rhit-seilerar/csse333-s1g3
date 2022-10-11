use StardewHoes
go

create procedure update_Farm (
	@ID int,
	@Season varchar(6) = null,
	@Name varchar(30) = null
) as
	if @ID is null begin
		print 'ERROR in update_Farm: ID cannot be null.'
		return 1
	end
	if @Season is null and @Name is null begin
		print 'ERROR in update_Farm: At least one of Season or Name must be non-null'
		return 2
	end
	if not exists (select * from Farm where ID = @ID) begin
		print 'ERROR in update_Farm: The farm with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	if @Season not in ('Spring', 'Summer', 'Fall', 'Winter') begin
		print 'ERROR in update_Farm: Season must be one of ''Spring'', ''Summer'', ''Fall'', or ''Winter''.'
		return 4
	end

	declare @Status int
	declare @CurrSeason varchar(6), @CurrName varchar(30)
	select @CurrSeason = Season, @CurrName = Name
	from Farm
	where ID = @ID
	
	if @Season is null begin set @Season = @CurrSeason end
	if @Name   is null begin set @Name   = @CurrName   end

	update Farm
	set Season = @Season, Name = @Name
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Farm: Could not update the data of the farm with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Farm: Successfully updated the data for the farm with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go