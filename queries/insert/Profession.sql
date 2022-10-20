use StardewHoes10
go

create or alter procedure insert_Profession (
	@BoostCategory varchar(10) = null,
	@BoostMultiplier decimal(18,0) = null,
	@ID int = null output
) as
	declare @Status int

	if @BoostCategory is null or @BoostMultiplier is null  begin
		raiserror('ERROR in insert_Profession: BoostCategory and BoostMultiplier cannot be null.', 14, 1)
		return 1
	end
	
	insert into Profession (BoostCategory, BoostMultiplier)
	values (@BoostCategory, @BoostMultiplier)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Profession: Failed to insert the Profession into the Profession table.', 14, 1)
		return @Status
	end

	print 'insert_Profession: Successfully inserted the Profession with BoostCategory ' + @BoostCategory + ' and BoostMultiplier ' + convert(varchar(18), @BoostMultiplier) + ' into the Profession table.'
	return 0
go