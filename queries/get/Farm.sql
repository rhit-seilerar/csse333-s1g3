use StardewHoes10
go

create or alter procedure get_Farm (
	@ID int = null,
	@Name varchar(30) = null,
	@Season varchar(6) = null
) as
	declare @Status int
	
	select *
	from Farm
	where (@ID is null or ID = @ID) and (@Name is null or Name = @Name) and (@Season is null or Season = @Season)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Farm: Failed to retrieve the data for farm %s.', 14, 1, @Name)
		return @Status
	end

	print 'get_Farm: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
go