use StardewHoes10
go

create or alter procedure get_Login (
	@Username varchar(30) = null,
	@Type tinyint = null
) as
	declare @Status int

	select *
	from Login
	where (@Username is null or Username = @Username) and (@Type is null or Type = @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Login: Failed to retrieve the login data for %s.', 14, 1, @Username)
		return @Status
	end

	print 'get_Login: Successfully retrieved the login data for ' + @Username + '.'
go