use StardewHoes10
go

create or alter procedure insert_ShopSells (
	@ShopID int = null,
	@ItemID int = null
) as
	
	if @ShopID is not null and exists (select * from ShopSells where ShopID = @ShopID) and @ItemID is not null and exists (select * from ShopSells where ItemID = @ItemID) begin
		raiserror('ERROR in insert_ShopSells: The tuple with ShopID %d  and ItemID %d already exists.', 14, 1, @ShopID, @ItemID)
		return 1
	end
	if @ShopID is null begin
		raiserror('ERROR in insert_ShopSells: ShopID cannot be null.', 14, 2)
		return 2
	end
	if @ItemID is null begin
		raiserror('ERROR in insert_ShopSells: ItemID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into ShopSells (ShopID, ItemID)
	values (@ShopID, @ItemID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_ShopSells: Failed to insert into the ShopSells table.', 14, 1)
		return @Status
	end

	print 'insert_ShopSells: Successfully inserted the tuple with ShopID ' + convert(varchar(15), @ShopID) + ' and ItemID ' + convert(varchar(15), @ItemID) + ' into the ShopSells table.'
go