use StardewHoes10
go

create procedure update_Produce (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Produce: ID cannot be null.'
		return 1
	end
	if not exists (select * from Produce where ID = @ID) begin
		print 'ERROR in update_Produce: The produce with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Item @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
go