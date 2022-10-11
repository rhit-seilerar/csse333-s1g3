use StardewHoes
go

create procedure update_Item (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Item: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null begin
		print 'ERROR in update_Item: At least one of Name, Quality, or BasePrice must be non-null.'
		return 2
	end
	if not exists (select * from Item where ID = @ID) begin
		print 'ERROR in update_Item: The item with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice
	from Item
	where ID = @ID
	
	if @Name      is null begin set @Name      = @CurrName      end
	if @Quality   is null begin set @Quality   = @CurrQuality   end
	if @BasePrice is null begin set @BasePrice = @CurrBasePrice end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Item: Could not update the data of the item with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Item: Successfully updated the data for the item with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go