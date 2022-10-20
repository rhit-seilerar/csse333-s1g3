use StardewHoes10
go

create or alter procedure insert_Seed (
	@Name varchar(20) = null,
	@BasePrice int = null,
	@Season varchar(6) = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Seed where ID = @ID) begin
		raiserror('ERROR in insert_Seed: The seed with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	if @Season not in ('Spring', 'Summer', 'Fall', 'Spring/Summer', 'Spring/Fall', 'Summer/Fall', 'All', 'None') begin
		print 'ERROR in insert_Seed: Season must be one of ''Spring'', ''Summer'', ''Fall'', ''Spring/Summer'', ''Spring/Fall'', ''Summer/Fall'', ''All'', or ''None''.'
		return 2
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, null, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Seed (ID, Season)
	values (@ID, @Season)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Seed: Failed to insert the seed %s with price %d and season %d into the Seed table.', 14, 1, @Name, @BasePrice, @Season)
		return @Status
	end

	print 'insert_Seed: Successfully inserted the seed ' + @Name + ' with price ' + convert(varchar(40), @BasePrice) + ' and season ' + @Season + ' into the Seed table.'
go