use StardewHoes10
go

create procedure update_Needs (
	@VillagerID int,
	@ItemID int,
	@Reward int = null,
	@Quantity int = null
) as
	if @VillagerID is null begin
		print 'ERROR in update_Needs: VillagerID cannot be null.'
		return 1
	end
	if @ItemID is null begin
		print 'ERROR in update_Needs: ItemID cannot be null.'
		return 2
	end
	if @Reward is null and @Quantity is null begin
		print 'ERROR in update_Needs: At least one of Reward or Quantity must be non-null.'
		return 3
	end
	if not exists (select * from Needs where VillagerID = @VillagerID and ItemID = @ItemID) begin
		print 'ERROR in update_Needs: The request from the villager with ID ' + convert(varchar(30), @VillagerID) + ' for the item with ID ' + convert(varchar(30), @ItemID) + ' does not exist.'
		return 4
	end
	
	declare @Status int
	declare @CurrReward int, @CurrQuantity int
	select @CurrReward = Reward, @CurrQuantity = Quantity
	from Needs
	where VillagerID = @VillagerID and ItemID = @ItemID
	
	if @Reward   is null begin set @Reward   = @CurrReward   end
	if @Quantity is null begin set @Quantity = @CurrQuantity end
	
	update Needs
	set Reward = @Reward, Quantity = @Quantity
	where VillagerID = @VillagerID and ItemID = @ItemID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Needs: Could not update the data of the request from the villager with ID ' + convert(varchar(30), @VillagerID) + ' for the item with ID ' + convert(varchar(30), @ItemID) + '.'
		return @Status
	end
	
	print 'update_Needs: Successfully updated the data of the request from the villager with ID ' + convert(varchar(30), @VillagerID) + ' for the item with ID ' + convert(varchar(30), @ItemID) + '.'
	return 0
go