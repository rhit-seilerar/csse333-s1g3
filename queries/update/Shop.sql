use StardewHoes
go

create procedure update_Shop (
	@OwnerID int,
	@Name varchar(20) = null,
	@Address varchar(40) = null,
	@Schedule varchar(100) = null
) as
	if @OwnerID is null begin
		print 'ERROR in update_Shop: OwnerID cannot be null.'
		return 1
	end
	if @Name is null and @Address is null and @Schedule is null begin
		print 'ERROR in update_Shop: At least one of Name, Address, or Schedule must be non-null.'
		return 2
	end
	if not exists (select * from Shop where OwnerID = @OwnerID) begin
		print 'ERROR in update_Shop: The Shop with OwnerID ' + convert(varchar(30), @OwnerID) + ' does not exist.'
		return 3
	end
	if exists (select * from Shop where OwnerID != @OwnerID and Name = @Name) begin
		print 'ERROR in update_Shop: A shop with the name ' + @Name + ' already exists.'
		return 4
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrAddress varchar(40), @CurrSchedule varchar(100)
	select @CurrName = Name, @CurrAddress = Address, @CurrSchedule = Schedule
	from Shop
	where OwnerID = @OwnerID
	
	if @Name     is null begin set @Name     = @CurrName     end
	if @Address  is null begin set @Address  = @CurrAddress  end
	if @Schedule is null begin set @Schedule = @CurrSchedule end
	
	update Shop
	set Name = @Name, Address = @Address, Schedule = @Schedule
	where OwnerID = @OwnerID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Shop: Could not update the data of the Shop with ID ' + convert(varchar(20), @OwnerID) + '.'
		return @Status
	end
	
	print 'update_Shop: Successfully updated the data for the Shop with ID ' + convert(varchar(20), @OwnerID) + '.'
	return 0
go