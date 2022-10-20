use StardewHoes10
go

create or alter procedure insert_ArtisanGood (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Multiplier real = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from ArtisanGood where ID = @ID) begin
		raiserror('ERROR in insert_ArtisanGood: The ArtisanGood with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	if @Multiplier is null begin
		raiserror('ERROR in insert_ArtisanGood: Multiplier cannot be null.', 14, 1, @ID)
		return 2
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into ArtisanGood (ID, Multiplier)
	values (@ID, @Multiplier)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_ArtisanGood: Failed to insert the item %s with quality %d and price %d into the ArtisanGood table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Produce: Successfully inserted the Artisan Good ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the ArtisanGood table.'
go