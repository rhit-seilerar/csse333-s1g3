use StardewHoes10
go

create or alter procedure get_Login (
	@Username varchar(30)
) as
	declare @Status int

	if @Username is null begin
		raiserror('ERROR in get_Login: Username cannot be null.', 14, 1)
		return 1
	end

	select *
	from Login
	where Username = @Username
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Login: Failed to retrieve the login data for %s.', 14, 1, @Username)
		return @Status
	end

	print 'get_Login: Successfully retrieved the login data for ' + @Username + '.'
go