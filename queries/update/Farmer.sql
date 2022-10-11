use StardewHoes10
go

create procedure update_Farmer (
	@VillagerID int,
	@FarmID int
) as
	if @VillagerID is null begin
		print 'ERROR in update_Farmer: VillagerID cannot be null.'
		return 1
	end
	if @FarmID is null begin
		print 'ERROR in update_Farmer: FarmID cannot be null.'
		return 2
	end
	if not exists (select * from Farmer where VillagerID = @VillagerID) begin
		print 'ERROR in update_Farmer: The farmer with VillagerID ' + convert(varchar(30), @VillagerID) + ' does not exist.'
		return 3
	end
	if not exists (select * from Farm where ID = @FarmID) begin
		print 'ERROR in update_Farmer: The farm with ID ' + convert(varchar(30), @FarmID) + ' does not exist.'
		return 4
	end

	declare @Status int
	update Farmer
	set FarmID = @FarmID
	where VillagerID = @VillagerID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Farmer: Could not update the farm of the farmer with ID ' + convert(varchar(20), @VillagerID) + '.'
		return @Status
	end

	print 'update_Farmer: Successfully updated the farm of the farmer with ID ' + convert(varchar(20), @VillagerID) + '.'
	return 0
go