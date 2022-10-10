use StardewHoes
go

create procedure get_Shop (
	@OwnerID int,
	@Name varchar(20) = null output,
	@Address varchar(40) = null output,
	@Schedule varchar(100) = null output
) as

declare @Status int

if @OwnerID is null begin
	print 'ERROR in get_Shop: OwnerID must not be null.'
	return 1
end

select @Name = Name, @Address = Address, @Schedule = Schedule
from Shop
where OwnerID = @OwnerID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_Shop: Failed to retrieve the data for the shop owned by ' + convert(varchar(20), @OwnerID) + '.'
	return @Status
end

if @Name is null begin
	print 'ERROR in get_Shop: The data for the shop owned by ' + convert(varchar(20), @OwnerID) + ' does not exist.'
	return 1
end

print 'get_Shop: Successfully retrieved the data for the shop owned by ' + convert(varchar(20), @OwnerID) + '.'
return 0
go