use StardewHoes10
go

create or alter procedure insert_Login (
	@Username varchar(30),
	@Hash binary(16),
	@Salt binary(16),
	@Type tinyint = 0
) as
	declare @Status int

	if @Username is null or @Hash is null or @Salt is null begin
		raiserror('ERROR in insert_Login: Username, Hash, and Salt cannot be null.', 14, 1)
		return 1
	end
	if exists (select * from Login where Username = @Username) begin
		raiserror('ERROR in insert_Login: Username %s already exists.', 14, 1, @Username)
		return 2
	end
	if @Type > 7 begin
		raiserror('ERROR in insert_Login: Type must be less than 8', 14, 1, @Username)
		return 3
	end
	
	insert into Login (Username, Hash, Salt, Type)
	values (@username, @Hash, @Salt, @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Login: Failed to insert the username %s into the Login table.', 14, 1, @Username)
		return @Status
	end

	print 'insert_Login: Successfully inserted ' + @Username + ' into the Login table.'
	return 0
go