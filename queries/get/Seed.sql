use StardewHoes10
go

create or alter procedure get_Seed (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output,
	@Season varchar(15) = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end
select Season
	from Seed
	where (@ID is null or ID = @ID) and (@Season is null or Season = @Season)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s.', 14, 1, @Name)
		return @Status
	end
print 'get_Seed: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
go