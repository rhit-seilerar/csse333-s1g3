use StardewHoes10
go

create or alter procedure insert_Farmer (
	@Name varchar(20) = null,
	@FarmID int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Farmer where VillagerID = @ID) begin
		raiserror('ERROR in insert_Farmer: The Farmer with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	if @FarmID is null begin
		raiserror('ERROR in insert_Farmer: FarmID cannot be null.', 14, 2, @ID)
		return 2
	end
	if @FarmID is not null and not exists(select* from Farm where ID = @FarmID) begin
		raiserror('ERROR in insert_Farmer: The Farm with ID %d does not exist.', 14, 3, @FarmID)
		return 3
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Villager where ID = @ID) begin
		execute @Status = insert_Villager @Name, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Farmer (VillagerID, FarmID)
	values (@ID, @FarmID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Farmer: Failed to insert the farmer %s  into the Farmer table.', 14, 1, @Name)
		return @Status
	end

	print 'insert_Farmer: Successfully inserted the farmer ' + @Name + ' into the Farmer table.'
go