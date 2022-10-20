use StardewHoes10
go

create or alter procedure insert_Needs (
	@VillagerID int = null,
	@ItemID int = null,
	@Reward int = null,
	@Quantity int = null
) as
	
	if @VillagerID is not null and exists (select * from Needs where VillagerID = @VillagerID) and @ItemID is not null and exists (select * from Needs where ItemID = @ItemID) begin
		raiserror('ERROR in insert_Needs: The tuple with VillagerID %d  and ItemID %d already exists.', 14, 1, @VillagerID, @ItemID)
		return 1
	end
	if @VillagerID is null or @ItemID is null or @Reward is null or @Quantity is null begin
		raiserror('ERROR in insert_Needs: VillagerID, ItemID, Reward, and Quantity cannot be null.', 14, 2)
		return 2
	end

	declare @Status int

	insert into Needs (VillagerID, ItemID, Reward, Quantity)
	values (@VillagerID, @ItemID, @Reward, @Quantity)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Needs: Failed to insert into the Needs table.', 14, 1)
		return @Status
	end

	print 'insert_Needs: Successfully inserted the Need with VillagerID ' + convert(varchar(15), @VillagerID) + ' and ItemID ' + convert(varchar(15), @ItemID) + ' into the Needs table.'
go