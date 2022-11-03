use StardewHoes10
go

create or alter procedure update_Login (
	@Username varchar(30),
	@Hash varbinary(16),
	@Salt varbinary(16)
) as
	if @Username is null or @Hash is null or @Salt is null begin
		raiserror('ERROR in update_Login: None of Username, Hash, or Salt can be null.', 14, 1)
		return 1
	end
	if not exists (select * from Login where Username = @Username) begin
		raiserror('ERROR in update_Login: The username %s does not exist.', 14, 1, @Username)
		return 2
	end
	
	declare @Status int

	update Login
	set Hash = @Hash, Salt = @Salt
	where Username = @Username
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in update_Login: Could not update the password of %s.', 14, 1, @Username)
		return @Status
	end
	
	print 'update_Login: Successfully updated the password for ' + @Username + '.'
go