use StardewHoes
go

create procedure update_Seed (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Season varchar(6) = null
) as
	if @ID is null begin
		print 'ERROR in update_Seed: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null and @Season is null begin
		print 'ERROR in update_Seed: At least one of Name, Quality, BasePrice, or Season must be non-null.'
		return 2
	end
	if not exists (select * from Seed where ID = @ID) begin
		print 'ERROR in update_Seed: The seed with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	if @Season not in ('Spring', 'Summer', 'Fall', 'Spring/Summer', 'Spring/Fall', 'Summer/Fall', 'All', 'None') begin
		print 'ERROR in update_Seed: Season must be one of ''Spring'', ''Summer'', ''Fall'', ''Spring/Summer'', ''Spring/Fall'', ''Summer/Fall'', ''All'', or ''None''.'
		return 4
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int, @CurrSeason varchar(6)
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice, @CurrSeason = Season
	from Seed
	join Item on Seed.ID = Item.ID
	where Seed.ID = @ID
	
	if @Name      is null begin set @Name      = @CurrName      end
	if @Quality   is null begin set @Quality   = @CurrQuality   end
	if @BasePrice is null begin set @BasePrice = @CurrBasePrice end
	if @Season    is null begin set @Season    = @CurrSeason    end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Seed: Could not update the item data of the seed with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	update Seed
	set Season = @Season
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Seed: Could not update the season of the seed with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Seed: Successfully updated the data for the seed with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go