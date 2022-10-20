use StardewHoes10
go

create or alter procedure insert_Generates (
	@ProduceID int = null,
	@ProductID int = null
) as
	
	if @ProduceID is not null and exists (select * from Generates where ProduceID = @ProduceID) and @ProductID is not null and exists (select * from Generates where ProductID = @ProductID) begin
		raiserror('ERROR in insert_Generates: The tuple with ProduceID %d  and ProductID %d already exists.', 14, 1, @ProduceID, @ProductID)
		return 1
	end
	if @ProduceID is null begin
		raiserror('ERROR in insert_Generates: ProduceID cannot be null.', 14, 2)
		return 2
	end
	if @ProductID is null begin
		raiserror('ERROR in insert_Generates: ProductID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into Generates (ProduceID, ProductID)
	values (@ProduceID, @ProductID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Generates: Failed to insert the into the Generates table.', 14, 1)
		return @Status
	end

	print 'insert_Generates: Successfully inserted the tuple with Produce ID ' + convert(varchar(15), @ProduceID) + ' and Product ID ' + convert(varchar(15), @ProductID) + ' into the Generates table.'
go