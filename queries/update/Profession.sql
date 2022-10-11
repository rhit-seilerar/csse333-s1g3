use StardewHoes
go

create procedure update_Profession (
	@ID int,
	@BoostCategory varchar(10) = null,
	@BoostMultiplier decimal = null
) as
	if @ID is null begin
		print 'ERROR in update_Profession: ID cannot be null.'
		return 1
	end
	if @BoostCategory is null and @BoostMultiplier is null begin
		print 'ERROR in update_Profession: At least one of BoostCategory or BoostMultiplier must be non-null'
		return 2
	end
	if not exists (select * from Profession where ID = @ID) begin
		print 'ERROR in update_Profession: The profession with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int
	declare @CurrBoostCategory varchar(10), @CurrBoostMultiplier decimal
	select @CurrBoostCategory = BoostCategory, @CurrBoostMultiplier = BoostMultiplier
	from Profession
	where ID = @ID
	
	if @BoostCategory   is null begin set @BoostCategory   = @CurrBoostCategory   end
	if @BoostMultiplier is null begin set @BoostMultiplier = @CurrBoostMultiplier end

	update Profession
	set BoostCategory = @BoostCategory, BoostMultiplier = @BoostMultiplier
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Profession: Could not update the data of the profession with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Profession: Successfully updated the data for the profession with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go