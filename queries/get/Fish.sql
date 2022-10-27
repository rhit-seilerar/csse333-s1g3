use StardewHoes10
go

create or alter procedure get_Fish (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_Fish: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
go