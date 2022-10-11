use StardewHoes
go

create procedure update_AnimalProduct (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_AnimalProduct: ID cannot be null.'
		return 1
	end
	if not exists (select * from AnimalProduct where ID = @ID) begin
		print 'ERROR in update_AnimalProduct: The animal product with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Produce @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
go