use StardewHoes
go

create procedure update_PlantProduct (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Type varchar(20) = null
) as
	if @ID is null begin
		print 'ERROR in update_PlantProduct: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null and @Type is null begin
		print 'ERROR in update_PlantProduct: At least one of Name, Quality, BasePrice, or Type must be non-null.'
		return 2
	end
	if @Type not in ('Vegetable', 'Fruit', 'Flower', 'Forage') begin
		print 'ERROR in update_PlantProduct: Type must be one of ''Vegetable'', ''Fruit'', ''Flower'', or ''Forage''.'
		return 3
	end
	if not exists (select * from PlantProduct where ID = @ID) begin
		print 'ERROR in update_PlantProduct: The plant product with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 4
	end

	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int, @CurrType varchar(20)
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice, @CurrType = Type
	from PlantProduct
	join Item on PlantProduct.ID = Item.ID
	where PlantProduct.ID = @ID
	
	if @Name      is null begin set @Name      = @CurrName      end
	if @Quality   is null begin set @Quality   = @CurrQuality   end
	if @BasePrice is null begin set @BasePrice = @CurrBasePrice end
	if @Type      is null begin set @Type      = @CurrType      end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_PlantProduct: Could not update the item data of the plant product with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	update PlantProduct
	set Type = @Type
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_PlantProduct: Could not update the type of the plant product with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_PlantProduct: Successfully updated the data for the plant product with ID ' + convert(varchar(20), @ID) + '.'
	return 0
go