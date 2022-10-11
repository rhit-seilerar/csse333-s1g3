use StardewHoes10
go

create procedure get_Shopkeeper (
	@ID int,
	@Name varchar(30) = null output,
	@IsDeleted bit = null output
) as

declare @Status int

if @ID is null begin
	print 'ERROR in get_Shopkeeper: ID must not be null.'
	return 1
end

select @Name = Name, @IsDeleted = IsDeleted
from Villager
join Shopkeeper on Villager.ID = Shopkeeper.ID
where Shopkeeper.ID = @ID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_Shopkeeper: Failed to retrieve the data for the shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
	return @Status
end

if @Name is null begin
	print 'ERROR in get_Shopkeeper: The data for the shopkeeper with ID ' + convert(varchar(20), @ID) + ' does not exist.'
	return 1
end

print 'get_Shop: Successfully retrieved the data for the shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
return 0
go