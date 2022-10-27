use StardewHoes10
go

create or alter procedure get_ArtisanGood (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output,
	@Multiplier real = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end
select Multiplier
	from ArtisanGood
	where (@ID is null or ID = @ID) and (@Multiplier is null or Multiplier = @Multiplier)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s.', 14, 1, @Name)
		return @Status
	end
print 'get_ArtisanGood: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
go