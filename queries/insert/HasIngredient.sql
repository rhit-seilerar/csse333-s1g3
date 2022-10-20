use StardewHoes10
go

create or alter procedure insert_HasIngredient (
	@IngredientID int = null,
	@FoodID int = null
) as
	
	if @IngredientID is not null and exists (select * from HasIngredient where IngredientID = @IngredientID) and @FoodID is not null and exists (select * from HasIngredient where FoodID = @FoodID) begin
		raiserror('ERROR in insert_HasIngredient: The tuple with IngredientID %d  and FoodID %d already exists.', 14, 1, @IngredientID, @FoodID)
		return 1
	end
	if @IngredientID is null begin
		raiserror('ERROR in insert_HasIngredient: IngredientID cannot be null.', 14, 2)
		return 2
	end
	if @FoodID is null begin
		raiserror('ERROR in insert_HasIngredient: FoodID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into HasIngredient (IngredientID, FoodID)
	values (@IngredientID, @FoodID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_HasIngredient: Failed to insert into the HasIngredient table.', 14, 1)
		return @Status
	end

	print 'insert_HasIngredient: Successfully inserted the tuple with Ingredient ID ' + convert(varchar(15), @IngredientID) + ' and Food ID ' + convert(varchar(15), @FoodID) + ' into the HasIngredient table.'
go