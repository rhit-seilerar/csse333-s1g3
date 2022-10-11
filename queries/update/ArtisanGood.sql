use StardewHoes
go

create procedure update_ArtisanGood (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Multiplier decimal = null
) as
	if @ID is null begin
		print 'ERROR in update_ArtisanGood: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null and @Multiplier is null begin
		print 'ERROR in update_ArtisanGood: At least one of Name, Quality, BasePrice, or Multiplier must be non-null.'
		return 2
	end
	if not exists (select * from ArtisanGood where ID = @ID) begin
		print 'ERROR in update_ArtisanGood: The artisan good with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int, @CurrMultiplier decimal
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice, @CurrMultiplier = Multiplier
	from ArtisanGood
	join Item on ArtisanGood.ID = Item.ID
	where ArtisanGood.ID = @ID
	
	if @Name       is null begin set @Name       = @CurrName       end
	if @Quality    is null begin set @Quality    = @CurrQuality    end
	if @BasePrice  is null begin set @BasePrice  = @CurrBasePrice  end
	if @Multiplier is null begin set @Multiplier = @CurrMultiplier end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_ArtisanGood: Could not update the item data of the artisan good with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	update ArtisanGood
	set Multiplier = @Multiplier
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_ArtisanGood: Could not update the multiplier of the artisan good with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_ArtisanGood: Successfully updated the data for the artisan good with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go