use StardewHoes
go

create procedure insert_Produce (
	@Name varchar(20),
	@Quality tinyint,
	@BasePrice int,
	@ID int = null output
) as

declare @Status int
execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
if @Status != 0 begin return @Status end

insert into Produce (ID)
values (@ID)
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in insert_Produce: Failed to insert the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
	return @Status
end

print 'insert_Produce: Successfully inserted the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
return 0

go