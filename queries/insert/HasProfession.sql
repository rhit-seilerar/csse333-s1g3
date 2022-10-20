use StardewHoes10
go

create or alter procedure insert_HasProfession (
	@ProfessionID int = null,
	@FarmerID int = null
) as
	
	if @ProfessionID is not null and exists (select * from HasProfession where ProfessionID = @ProfessionID) and @FarmerID is not null and exists (select * from HasProfession where FarmerID = @FarmerID) begin
		raiserror('ERROR in insert_HasProfession: The tuple with ProfessionID %d  and FarmerID %d already exists.', 14, 1, @ProfessionID, @FarmerID)
		return 1
	end
	if @ProfessionID is null begin
		raiserror('ERROR in insert_HasProfession: ProfessionID cannot be null.', 14, 2)
		return 2
	end
	if @FarmerID is null begin
		raiserror('ERROR in insert_HasProfession: FarmerID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into HasProfession (ProfessionID, FarmerID)
	values (@ProfessionID, @FarmerID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_HasProfession: Failed to insert into the HasProfession table.', 14, 1)
		return @Status
	end

	print 'insert_HasProfession: Successfully inserted the tuple with ProfessionID ' + convert(varchar(15), @ProfessionID) + ' and FarmerID ' + convert(varchar(15), @FarmerID) + ' into the HasProfession table.'
go