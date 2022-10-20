use StardewHoes10
go

create or alter procedure insert_Produces (
	@AnimalID int = null,
	@ProductID int = null
) as
	
	if @AnimalID is not null and exists (select * from Produces where AnimalID = @AnimalID) and @ProductID is not null and exists (select * from Produces where ProductID = @ProductID) begin
		raiserror('ERROR in insert_Produces: The tuple with AnimalID %d  and ProductID %d already exists.', 14, 1, @AnimalID, @ProductID)
		return 1
	end
	if @AnimalID is null begin
		raiserror('ERROR in insert_Produces: AnimalID cannot be null.', 14, 2)
		return 2
	end
	if @ProductID is null begin
		raiserror('ERROR in insert_Produces: ProductID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into Produces (AnimalID, ProductID)
	values (@AnimalID, @ProductID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Produces: Failed to insert into the Produces table.', 14, 1)
		return @Status
	end

	print 'insert_Produces: Successfully inserted the tuple with AnimalID ' + convert(varchar(15), @AnimalID) + ' and ProductID ' + convert(varchar(15), @ProductID) + ' into the Produces table.'
go