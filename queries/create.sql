use master
go

create database [StardewHoes15]
on (
	name = [StardewHoes15],
	filename = 'D:\Database\MSSQL15.MSSQLSERVER\MSSQL\DATA\StardewHoes15.mdf',
	size = 8mb,
   maxsize = unlimited,
	filegrowth = 10%
)
log on (
	name = [StardewHoes15_log],
	filename = 'D:\Database\MSSQL15.MSSQLSERVER\MSSQL\DATA\StardewHoes15_log.ldf',
	size = 4mb,
	maxsize = 2tb,
	filegrowth = 10%
)

use StardewHoes15
go

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'zellneae' or u.name = 'zellneae'
) begin
   create user zellneae for login zellneae with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'zellneae'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'henderae' or u.name = 'henderae'
) begin
   create user henderae for login henderae with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'henderae'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'seilerar' or u.name = 'seilerar'
) begin
   create user seilerar for login seilerar with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'seilerar'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'StardewHoesapp10' or u.name = 'StardewHoesapp10'
) begin
   create user StardewHoesapp10 for login StardewHoesapp10
   grant execute to StardewHoesapp10
end
CREATE TABLE Villager(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	Name varchar(30) NOT NULL)

create table Item (
	ID int identity(0,1),
	Name varchar(40) not null,
	Quality tinyint,
	BasePrice int not null
	Primary key (ID),
	Check(Quality <= 3),
	Check(BasePrice >= 0)
)

create table Animal (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)

create table Produce (
	ID int
	Primary key (ID),
	Foreign key (ID) references Item(ID)
	on delete cascade
)

create table AnimalProduct (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Produce(ID)
	on delete cascade
)

create table PlantProduct (
	ID int,
	Type varchar(20) not null
	Primary key (ID)
	Foreign key (ID) references Produce(ID)
	on delete cascade,
	Check (Type in ('Vegetable', 'Fruit', 'Flower', 'Forage'))
)

create table ArtisanGood (
	ID int,
	Multiplier real not null
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade,
	Check (Multiplier >= 0)
)

create table Seed (
	ID int,
	Season varchar(6)
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade,
	Check(Season in ('Spring', 'Summer', 'Fall', 'Spring/Summer', 'Spring/Fall', 'Summer/Fall', 'All', 'None'))
)

create table Food (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)

create table Fish (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)

create table Produces (
	AnimalID int,
	ProductID int
	Primary Key (AnimalID, ProductID)
	Foreign Key (AnimalID) references Animal(ID)
	on delete no action,
	Foreign Key (ProductID) references Produce(ID)
	on delete no action
)

create table Generates (
	ProduceID int,
	ProductID int
	Primary Key (ProduceID, ProductID),
	Foreign Key (ProduceID) references Item(ID)
	on delete no action,
	Foreign Key (ProductID) references Item(ID)
	on delete no action
)

create table Needs (
	VillagerID int,
	ItemID int,
	Reward int,
	Quantity int
	Primary key (VillagerID, ItemID),
	Foreign key (VillagerID) references Villager(ID)
	on delete cascade,
	Foreign key (ItemID) references Item(ID)
	on delete cascade,
	Check (Reward > 0),
	Check (Quantity > 0)
)

CREATE TABLE Farm(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	Season varchar(6) NULL,
	[Name] varchar(30) NULL,
	CHECK  (Season IN ('Spring', 'Summer', 'Fall', 'Winter')))

CREATE TABLE Farmer(
	VillagerID int NOT NULL PRIMARY KEY,
	FarmID int NOT NULL,
	FOREIGN KEY(FarmID) REFERENCES Farm(ID)
	on delete cascade,
	FOREIGN KEY(VillagerID) REFERENCES Villager(ID)
	on delete cascade)

CREATE TABLE HasIngredient(
	IngredientID int NOT NULL,
	FoodID int NOT NULL,
	PRIMARY KEY(IngredientID, FoodID),
	FOREIGN KEY(IngredientID) REFERENCES Item(ID)
	on delete no action,
	FOREIGN KEY(FoodID) REFERENCES Food(ID)
	on delete no action)

CREATE TABLE Profession(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	BoostCategory varchar(10) NULL,
	BoostMultiplier decimal(8, 4) NULL)

CREATE TABLE HasProfession(
	ProfessionID int NOT NULL,
	FarmerID int NOT NULL,
	PRIMARY KEY (ProfessionID, FarmerID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID)
	on delete cascade,
	FOREIGN KEY(ProfessionID) REFERENCES Profession(ID)
	on delete cascade)

CREATE TABLE FarmSells(
	FarmerID int NOT NULL,
	ItemID int NOT NULL,
	Price money NULL,
	PRIMARY KEY (FarmerID, ItemID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID)
	on delete cascade,
	FOREIGN KEY(ItemID) REFERENCES Item(ID)
	on delete cascade)

create table Shopkeeper (
	ID int,
	IsDeleted bit default 0
	Primary Key (ID),
	Foreign Key (ID) references Villager(ID)
	on delete cascade
)

create table Shop (
	OwnerID int,
	Address varchar(100) not null,
	Schedule varchar(100) not null,
	Name varchar(20) not null unique,
	Primary Key (OwnerID),
	Foreign Key (OwnerID) references Shopkeeper(ID)
)

create table ShopBuys (
	ShopID int,
	ItemID int
	Primary Key (ShopID, ItemID),
	Foreign Key (ShopID) references Shop(OwnerID)
	on delete cascade,
	Foreign Key (ItemID) references Item(ID)
	on delete cascade
)

create table ShopSells (
	ShopID int,
	ItemID int
	Primary Key (ShopID, ItemID),
	Foreign Key (ShopID) references Shop(OwnerID)
	on delete cascade,
	Foreign Key (ItemID) references Item(ID)
	on delete cascade
)

create table Login (
	Username varchar(30),
	Hash binary(16) not null,
	Salt binary(16) not null,
	Type tinyint not null,
	Primary Key (Username),
	Check (Type <= 7)
)
create or alter trigger after_delete_Item on Item instead of delete as
	delete from Generates
	where ProductID in (select ID from Deleted) or ProduceID in (select ID from Deleted)

	delete from Produces
	where ProductID in (select ID from Deleted) or AnimalID in (select ID from Deleted)
	
	delete from HasIngredient
	where IngredientID in (select ID from Deleted) or FoodID in (select ID from Deleted)

	delete from Item
	where ID in (select ID from Deleted)
create or alter procedure assert_int_not_null (
	@Param int = null,
	@ParamName varchar(20) = 'parameter',
	@LocName varchar(40) = 'stored procedure'
) as
if @Param is null begin
	print 'ERROR in ' + @LocName + ': ' + @ParamName + ' cannot be null.'
	return 1
end
return 0
create or alter procedure get_Animal (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_Animal: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_AnimalProduct (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_AnimalProduct: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_ArtisanGood (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output,
	@Multiplier real = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end
select Multiplier
	from ArtisanGood
	where (@ID is null or ID = @ID) and (@Multiplier is null or Multiplier = @Multiplier)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s.', 14, 1, @Name)
		return @Status
	end
print 'get_ArtisanGood: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_Farm (
	@ID int = null,
	@Name varchar(30) = null,
	@Season varchar(6) = null
) as
	declare @Status int
	
	select *
	from Farm
	where (@ID is null or ID = @ID) and (@Name is null or Name = @Name) and (@Season is null or Season = @Season)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Farm: Failed to retrieve the data for farm %s.', 14, 1, @Name)
		return @Status
	end

	print 'get_Farm: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
CREATE OR ALTER PROCEDURE get_farmer(
	@FarmerID int
) AS
	IF @FarmerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farmer] WHERE Farmer.[VillagerID] = @FarmerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM [dbo].Farmer JOIN Villager ON Villager.ID = Farmer.VillagerID
		WHERE [Farmer].VillagerID = @FarmerID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE get_farmsells (
	@FarmerID int,
	@ItemID int
) AS
	IF @FarmerID IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[FarmSells] WHERE FarmSells.FarmerID = @FarmerID AND FarmSells.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM FarmSells JOIN Farmer ON Farmer.VillagerID = FarmSells.FarmerID
		JOIN Villager ON Farmer.VillagerID = Villager.ID
		JOIN Item ON Item.ID = FarmSells.ItemID
		WHERE FarmSells.FarmerID = @FarmerID AND FarmSells.ItemID = @ItemID
	END
	RETURN 0
create or alter procedure get_Fish (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_Fish: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_Food (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_Food: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_Generates (
	@ProduceID int,
	@ProductID int
) as begin
	select *
	from Generates
	where ProduceID = @ProduceID and ProductID = @ProductID
end
CREATE OR ALTER PROCEDURE get_hasingredient (
	@FoodID int,
	@IngredientID int
) AS
	IF @FoodID IS NULL OR @IngredientID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasIngredient] WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM HasIngredient JOIN Food ON HasIngredient.FoodID = Food.ID
		JOIN Item ON Item.ID = Food.ID
		JOIN Item i2 ON i2.ID = HasIngredient.IngredientID
		WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID
	END
	RETURN 0
CREATE OR ALTER PROCEDURE get_hasprofession(
	@ProfessionID int,
	@FarmerID int
) AS
	IF @ProfessionID IS NULL OR @FarmerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasProfession] WHERE HasProfession.[ProfessionID] = @ProfessionID AND HasProfession.FarmerID = @FarmerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT *
		FROM [dbo].HasProfession JOIN Profession ON HasProfession.ProfessionID = Profession.ID
		JOIN Farmer ON Farmer.VillagerID = HasProfession.FarmerID
		JOIN Villager ON Villager.ID = Farmer.VillagerID
		WHERE HasProfession.ProfessionID = @ProfessionID AND HasProfession.FarmerID = @FarmerID
	END

	RETURN 0
create or alter procedure get_Item (
	@ID int = null,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	declare @Status int

	select ID, Name, Quality, BasePrice
	from Item
	where (@ID is null or ID = @ID) and (@Name is null or Name = @Name) and (@Quality is null or Quality = @Quality) and (@BasePrice is null or BasePrice = @BasePrice)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s with quality %d and price %d.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'get_Item: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
create or alter procedure get_Login (
	@Username varchar(30) = null,
	@Type tinyint = null
) as
	declare @Status int

	select *
	from Login
	where (@Username is null or Username = @Username) and (@Type is null or Type = @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Login: Failed to retrieve the login data for %s.', 14, 1, @Username)
		return @Status
	end

	print 'get_Login: Successfully retrieved the login data for ' + @Username + '.'
create or alter procedure get_Needs (
	@VillagerID int,
	@ItemID int
) as begin
	select *
	from Needs
	where VillagerID = @VillagerID and ItemID = @ItemID
end
create or alter procedure get_PlantProduct (
	@ID int = null,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Type varchar(20) = null
) as
	declare @Status int

	select I.*, Type
	from PlantProduct P
	join Item I on P.ID = I.ID
	where (@ID is null or P.ID = @ID) and (@Name is null or Name = @Name) and (@Quality is null or Quality = @Quality) and (@BasePrice is null or BasePrice = @BasePrice)
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in get_PlantProduct: Failed to retrieve the requested data.'
		return @Status
	end

	print 'get_PlantProduct: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
create or alter procedure get_Produce (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end

print 'get_Produce: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_Produces (
	@AnimalID int,
	@ProductID int
) as begin
	select *
	from Produces
	where AnimalID = @AnimalID and ProductID = @ProductID
end
CREATE OR ALTER PROCEDURE get_profession(
	@ProfessionID int
) AS
	IF @ProfessionID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Profession] WHERE Profession.[ID] = @ProfessionID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT * FROM [dbo].Profession WHERE Profession.ID = @ProfessionID
	END

	RETURN 0
create or alter procedure get_Seed (
	@ID int,
	@Name varchar(20) = null output,
	@Quality tinyint = null output,
	@BasePrice int = null output,
	@Season varchar(15) = null output
) as

declare @Status int
execute @Status = get_Item @ID, @Name output, @Quality output, @BasePrice output
if @Status != 0 begin return @Status end
select Season
	from Seed
	where (@ID is null or ID = @ID) and (@Season is null or Season = @Season)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in get_Item: Failed to retrieve the data for item %s.', 14, 1, @Name)
		return @Status
	end
print 'get_Seed: Successfully retrieved the data for the record with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_Shop (
	@OwnerID int,
	@Name varchar(20) = null output,
	@Address varchar(40) = null output,
	@Schedule varchar(100) = null output
) as

declare @Status int

if @OwnerID is null begin
	print 'ERROR in get_Shop: OwnerID must not be null.'
	return 1
end

select @Name = Name, @Address = Address, @Schedule = Schedule
from Shop
where OwnerID = @OwnerID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_Shop: Failed to retrieve the data for the shop owned by ' + convert(varchar(20), @OwnerID) + '.'
	return @Status
end

if @Name is null begin
	print 'ERROR in get_Shop: The data for the shop owned by ' + convert(varchar(20), @OwnerID) + ' does not exist.'
	return 1
end

print 'get_Shop: Successfully retrieved the data for the shop owned by ' + convert(varchar(20), @OwnerID) + '.'
return 0
create or alter procedure get_ShopBuys (
	@ShopID int,
	@ItemID int
) as begin
	select *
	from ShopBuys
	where ShopID = @ShopID and ItemID = @ItemID
end
create or alter procedure get_Shopkeeper (
	@ID int,
	@Name varchar(30) = null output,
	@IsDeleted bit = null output
) as

declare @Status int

if @ID is null begin
	print 'ERROR in get_Shopkeeper: ID must not be null.'
	return 1
end

select @Name = Name, @IsDeleted = IsDeleted
from Villager
join Shopkeeper on Villager.ID = Shopkeeper.ID
where Shopkeeper.ID = @ID
set @Status = @@ERROR
if @Status != 0 begin
	print 'ERROR in get_Shopkeeper: Failed to retrieve the data for the shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
	return @Status
end

if @Name is null begin
	print 'ERROR in get_Shopkeeper: The data for the shopkeeper with ID ' + convert(varchar(20), @ID) + ' does not exist.'
	return 1
end

print 'get_Shop: Successfully retrieved the data for the shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
return 0
create or alter procedure get_ShopSells (
	@ShopID int,
	@ItemID int
) as begin
	select *
	from ShopSells
	where ShopID = @ShopID and ItemID = @ItemID
end
CREATE OR ALTER PROCEDURE get_villager(
	@VillagerID int
) AS
	IF @VillagerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Villager] WHERE Villager.[ID] = @VillagerID)
	BEGIN
		RAISERROR('Must try to grab an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		SELECT * FROM [dbo].Villager WHERE [Villager].ID = @VillagerID
	END

	RETURN 0
create or alter procedure insert_Animal (
	@Name varchar(20) = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Animal where ID = @ID) begin
		raiserror('ERROR in insert_Animal: The animal with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, null, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Animal (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Animal: Failed to insert the animal %s with price %d into the Animal table.', 14, 1, @Name, @BasePrice)
		return @Status
	end

	print 'insert_Animal: Successfully inserted the animal ' + @Name + ' with price ' + convert(varchar(40), @BasePrice) + ' into the Animal table.'
create or alter procedure insert_AnimalProduct (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	
	if @ID is not null and exists (select * from AnimalProduct where ID = @ID) begin
		raiserror('ERROR in insert_AnimalProduct: The product with ID %d already exists.', 14, 1, @ID)
		return 3
	end

	declare @Status int

	if @ID is null or not exists (select * from Produce where ID = @ID) begin
		execute @Status = insert_Produce @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into AnimalProduct (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_AnimalProduct: Failed to insert the produce %s with quality %d and price %d into the AnimalProduct table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_AnimalProduct: Successfully inserted the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the AnimalProduct table.'
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
		raiserror('ERROR in insert_ArtisanGood: Multiplier cannot be null.', 14, 2, @ID)
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

	print 'insert_ArtisanGood: Successfully inserted the Artisan Good ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the ArtisanGood table.'
create or alter procedure insert_Farm (
	@Name varchar(30) = null,
	@Season varchar(6) = null,
	@ID int = null output
) as
	declare @Status int

	if @Name is null or @Season is null begin
		raiserror('ERROR in insert_Farm: Name and Season cannot be null.', 14, 1)
		return 1
	end
	
	insert into Farm (Name, Season)
	values (@Name, @Season)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Farm: Failed to insert the farm %s with season %s into the Farm table.', 14, 1, @Name, @Season)
		return @Status
	end

	print 'insert_Farm: Successfully inserted the farm ' + @Name + ' with season ' + @Season + ' into the Farm table.'
	return 0
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
create or alter procedure insert_Fish (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Fish where ID = @ID) begin
		raiserror('ERROR in insert_Fish: The fish with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Fish (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Fish: Failed to insert the fish %s with quality %d and price %d into the Fish table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Fish: Successfully inserted the fish ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Fish table.'
create or alter procedure insert_Food (
	@Name varchar(20) = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Food where ID = @ID) begin
		raiserror('ERROR in insert_Food: The food with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, null, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Food (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Food: Failed to insert the food %s with price %d into the Food table.', 14, 1, @Name, @BasePrice)
		return @Status
	end

	print 'insert_Food: Successfully inserted the food ' + @Name + ' with price ' + convert(varchar(40), @BasePrice) + ' into the Food table.'
create or alter procedure insert_Generates (
	@ProduceID int = null,
	@ProductID int = null
) as
	if @ProduceID is null begin
		raiserror('ERROR in insert_Generates: ProduceID cannot be null.', 14, 1)
		return 1
	end
	if @ProductID is null begin
		raiserror('ERROR in insert_Generates: ProductID cannot be null.', 14, 2)
		return 2
	end
	if exists (select * from Generates where ProduceID = @ProduceID and ProductID = @ProductID) begin
		raiserror('ERROR in insert_Generates: The tuple with ProduceID %d and ProductID %d already exists.', 14, 3, @ProduceID, @ProductID)
		return 3
	end

	declare @Status int

	insert into Generates (ProduceID, ProductID)
	values (@ProduceID, @ProductID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Generates: Failed to insert the into the Generates table.', 14, 1)
		return @Status
	end

	print 'insert_Generates: Successfully inserted the tuple with Produce ID ' + convert(varchar(15), @ProduceID) + ' and Product ID ' + convert(varchar(15), @ProductID) + ' into the Generates table.'
create or alter procedure insert_HasIngredient (
	@IngredientID int,
	@FoodID int
) as
	if @IngredientID is null begin
		raiserror('ERROR in insert_HasIngredient: IngredientID cannot be null.', 14, 1)
		return 1
	end
	if @FoodID is null begin
		raiserror('ERROR in insert_HasIngredient: FoodID cannot be null.', 14, 2)
		return 2
	end
	if exists (select * from HasIngredient where IngredientID = @IngredientID and FoodID = @FoodID) begin
		raiserror('ERROR in insert_HasIngredient: The tuple with IngredientID %d and FoodID %d already exists.', 14, 3, @IngredientID, @FoodID)
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
create or alter procedure insert_Item (
	@Name varchar(20),
	@Quality tinyint = null,
	@BasePrice int,
	@ID int = null output
) as
	declare @Status int

	if @Name is null or @BasePrice is null begin
		raiserror('ERROR in insert_Item: Name, Quality, and BasePrice cannot be null.', 14, 1)
		return 1
	end
	
	insert into Item (Name, Quality, BasePrice)
	values (@Name, @Quality, @BasePrice)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Item: Failed to insert the item %s with quality %d and price %d into the Item table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Item: Successfully inserted the item ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Item table.'
	return 0
create or alter procedure insert_Login (
	@Username varchar(30),
	@Hash binary(16),
	@Salt binary(16),
	@Type tinyint = 0
) as
	declare @Status int

	if @Username is null or @Hash is null or @Salt is null begin
		raiserror('ERROR in insert_Login: Username, Hash, and Salt cannot be null.', 14, 1)
		return 1
	end
	if exists (select * from Login where Username = @Username) begin
		raiserror('ERROR in insert_Login: Username %s already exists.', 14, 1, @Username)
		return 2
	end
	if @Type > 7 begin
		raiserror('ERROR in insert_Login: Type must be less than 8', 14, 1, @Username)
		return 3
	end
	
	insert into Login (Username, Hash, Salt, Type)
	values (@username, @Hash, @Salt, @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Login: Failed to insert the username %s into the Login table.', 14, 1, @Username)
		return @Status
	end

	print 'insert_Login: Successfully inserted ' + @Username + ' into the Login table.'
	return 0
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
create or alter procedure insert_PlantProduct (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Type varchar(20),
	@ID int = null output
) as
	if @Type is null begin
		raiserror('ERROR in insert_PlantProduct: Type cannot be null', 14, 1)
		return 1
	end
	if @Type not in ('Fruit', 'Vegetable', 'Forage', 'Flower') begin
		raiserror('ERROR in insert_PlantProduct: Type must be one of ''Fruit'', ''Vegetable'', ''Flower'', or ''Forage''.', 14, 1)
		return 2
	end
	if @ID is not null and exists (select * from PlantProduct where ID = @ID) begin
		raiserror('ERROR in insert_PlantProduct: The product with ID %d already exists.', 14, 1, @ID)
		return 3
	end

	declare @Status int

	if @ID is null or not exists (select * from Produce where ID = @ID) begin
		execute @Status = insert_Produce @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into PlantProduct (ID, Type)
	values (@ID, @Type)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_PlantProduct: Failed to insert the produce %s (%s) with quality %d and price %d into the PlantProduct table.', 14, 1, @Name, @Type, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_PlantProduct: Successfully inserted the produce ' + @Name + ' (' + @Type + ') with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the PlantProduct table.'
create or alter procedure insert_Produce (
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Produce where ID = @ID) begin
		raiserror('ERROR in insert_Produce: The produce with ID %d already exists.', 14, 1, @ID)
		return 1
	end
	
	declare @Status int

	if @ID is null or not exists (select * from Item where ID = @ID) begin
		execute @Status = insert_Item @Name, @Quality, @BasePrice, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Produce (ID)
	values (@ID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Produce: Failed to insert the produce %s with quality %d and price %d into the Produce table.', 14, 1, @Name, @Quality, @BasePrice)
		return @Status
	end

	print 'insert_Produce: Successfully inserted the produce ' + @Name + ' with quality ' + convert(varchar(2), @Quality) + ' and price ' + convert(varchar(40), @BasePrice) + ' into the Produce table.'
create or alter procedure insert_Produces (
	@AnimalID int,
	@ProductID int
) as
	if @AnimalID is null begin
		raiserror('ERROR in insert_Produces: AnimalID cannot be null.', 14, 1)
		return 1
	end
	if @ProductID is null begin
		raiserror('ERROR in insert_Produces: ProductID cannot be null.', 14, 2)
		return 2
	end
	if exists (select * from Produces where AnimalID = @AnimalID and ProductID = @ProductID) begin
		raiserror('ERROR in insert_Produces: The tuple with AnimalID %d and ProductID %d already exists.', 14, 3, @AnimalID, @ProductID)
		return 3
	end

	declare @Status int

	insert into Produces (AnimalID, ProductID)
	values (@AnimalID, @ProductID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Produces: Failed to insert into the Produces table.', 14, 1)
		return @Status
	end

	print 'insert_Produces: Successfully inserted the tuple with AnimalID ' + convert(varchar(15), @AnimalID) + ' and ProductID ' + convert(varchar(15), @ProductID) + ' into the Produces table.'
create or alter procedure insert_Profession (
	@BoostCategory varchar(10) = null,
	@BoostMultiplier decimal(8,4) = null,
	@ID int = null output
) as
	declare @Status int

	if @BoostCategory is null or @BoostMultiplier is null  begin
		raiserror('ERROR in insert_Profession: BoostCategory and BoostMultiplier cannot be null.', 14, 1)
		return 1
	end
	
	insert into Profession (BoostCategory, BoostMultiplier)
	values (@BoostCategory, @BoostMultiplier)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Profession: Failed to insert the Profession into the Profession table.', 14, 1)
		return @Status
	end

	print 'insert_Profession: Successfully inserted the Profession with BoostCategory ' + @BoostCategory + ' and BoostMultiplier ' + convert(varchar(18), @BoostMultiplier) + ' into the Profession table.'
	return 0
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
create or alter procedure insert_Shop (
	@OwnerID int,
	@Name varchar(20),
	@Address varchar(100),
	@Schedule varchar(100)
) as
	declare @Status int

	if @OwnerID is null or @Name is null or @Address is null or @Schedule is null begin
		raiserror('ERROR in insert_Shop: OwnerID, Name, Address, and BasePrice cannot be null.', 14, 1)
		return 1
	end
	
	insert into Shop(OwnerID, Name, Address, Schedule)
	values (@OwnerID, @Name, @Address, @Schedule)
	set @Status = @@ERROR

	if @Status != 0 begin
		raiserror('ERROR in insert_Shop: Failed to insert the Shop %s with Address %s into the Shop table.', 14, 1, @Name, @Address)
		return @Status
	end

	print 'insert_Shop: Successfully inserted the Shop ' + @Name + ' with Address ' + @Address + ' into the Shop table.'
	return 0
create or alter procedure insert_ShopBuys (
	@ShopID int = null,
	@ItemID int = null
) as
	
	if @ShopID is not null and exists (select * from ShopBuys where ShopID = @ShopID) and @ItemID is not null and exists (select * from ShopBuys where ItemID = @ItemID) begin
		raiserror('ERROR in insert_ShopBuys: The tuple with ShopID %d  and ItemID %d already exists.', 14, 1, @ShopID, @ItemID)
		return 1
	end
	if @ShopID is null begin
		raiserror('ERROR in insert_ShopBuys: ShopID cannot be null.', 14, 2)
		return 2
	end
	if @ItemID is null begin
		raiserror('ERROR in insert_ShopBuys: ItemID cannot be null.', 14, 3)
		return 3
	end

	declare @Status int

	insert into ShopBuys (ShopID, ItemID)
	values (@ShopID, @ItemID)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_ShopBuys: Failed to insert into the ShopBuys table.', 14, 1)
		return @Status
	end

	print 'insert_ShopBuys: Successfully inserted the tuple with ShopID ' + convert(varchar(15), @ShopID) + ' and ItemID ' + convert(varchar(15), @ItemID) + ' into the ShopBuys table.'
create or alter procedure insert_Shopkeeper (
	@Name varchar(20) = null,
	@ID int = null output
) as
	if @ID is not null and exists (select * from Shopkeeper where ID = @ID) begin
		raiserror('ERROR in insert_Shopkeeper: The Shopkeeper with ID %d already exists.', 14, 1, @ID)
		return 1
	end

	
	declare @Status int

	if @ID is null or not exists (select * from Villager where ID = @ID) begin
		execute @Status = insert_Villager @Name, @ID output
		if @Status != 0 begin return @Status end
	end

	insert into Shopkeeper (ID, IsDeleted)
	values (@ID, 0)
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in insert_Shopkeeper: Failed to insert the Shopkeeper %s  into the Shopkeeper table.', 14, 1, @Name)
		return @Status
	end

	print 'insert_Shopkeeper: Successfully inserted the Shopkeeper ' + @Name + ' into the Shopkeeper table.'
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
create or alter procedure insert_Villager (
	@Name varchar(30) = null,
	@ID int = null output
) as
	declare @Status int

	if @Name is null begin
		raiserror('ERROR in insert_Villager: Name cannot be null.', 14, 1)
		return 1
	end
	
	insert into Villager (Name)
	values (@Name)
	set @Status = @@ERROR
	set @ID = @@IDENTITY

	if @Status != 0 begin
		raiserror('ERROR in insert_Villager: Failed to insert the villager %s into the Villager table.', 14, 1, @Name)
		return @Status
	end

	print 'insert_Villager: Successfully inserted the villager ' + @Name + ' into the Villager table.'
	return 0
CREATE OR ALTER PROCEDURE delete_animal(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Animal] WHERE Animal.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in animal, delete it + check Produces --Done
		DELETE FROM [dbo].Animal WHERE Animal.ID = @ID
		IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
		BEGIN
			DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_animalproduct(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[AnimalProduct] WHERE AnimalProduct.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].AnimalProduct WHERE [AnimalProduct].ID = @ID
		IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
		BEGIN
			DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_artisangood(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ArtisanGood] WHERE ArtisanGood.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in artisan good, delete it + check Generates --Done
		DELETE FROM [dbo].ArtisanGood WHERE ArtisanGood.ID = @ID
		IF EXISTS(SELECT * FROM Generates WHERE Generates.ProductID = @ID)
		BEGIN
			DELETE FROM Generates WHERE Generates.ProductID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_farm(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farm] WHERE Farm.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Farm WHERE Farm.ID = @ID
		IF EXISTS (SELECT * FROM Farmer WHERE Farmer.FarmID = @ID)
		BEGIN
			DELETE FROM Farmer WHERE Farmer.FarmID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_farmer(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Farmer] WHERE Farmer.[VillagerID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Farmer WHERE [Farmer].VillagerID = @ID
		IF EXISTS(SELECT * FROM FarmSells WHERE FarmSells.FarmerID = @ID)
		BEGIN
			DELETE FROM [dbo].FarmSells WHERE [FarmSells].FarmerID = @ID
		END
		IF EXISTS(SELECT * FROM HasProfession WHERE HasProfession.FarmerID = @ID)
		BEGIN
			DELETE FROM [dbo].HasProfession WHERE [HasProfession].FarmerID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_farmsells(
	@FarmerID int,
	@ItemID int
) AS
	IF @FarmerID IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[FarmSells] WHERE FarmSells.[FarmerID] = @FarmerID AND FarmSells.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].FarmSells WHERE [FarmSells].FarmerID = @FarmerID AND FarmSells.ItemID = @ItemID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_fish(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Fish] WHERE Fish.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing fish', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in fish, delete it
		DELETE FROM [dbo].Fish WHERE Fish.ID = @ID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_food(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Food] WHERE Food.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		--If it's in food, delete it + check if it's in HasIngredient --Done
		DELETE FROM [dbo].Food WHERE [Food].ID = @ID
		IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.FoodID = @ID)
		BEGIN
			DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_generates(
	@ProduceID int,
	@ProductID int
) AS
	IF @ProduceID IS NULL OR @ProductID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Generates] WHERE Generates.[ProduceID] = @ProduceID AND Generates.ProductID = @ProductID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Generates WHERE Generates.ProduceID = @ProduceID AND Generates.ProductID = @ProductID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_hasIngredient(
	@IngredientID int,
	@FoodID int
) AS
	IF @IngredientID IS NULL OR @FoodID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasIngredient] WHERE HasIngredient.[FoodID] = @FoodID AND HasIngredient.IngredientID = @IngredientID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @FoodID AND HasIngredient.IngredientID = @IngredientID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_hasProfession(
	@professionID int,
	@farmerID int
) AS
	IF @farmerID IS NULL OR @professionID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[HasProfession] WHERE HasProfession.[ProfessionID] = @professionID AND HasProfession.FarmerID = @farmerID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM HasProfession WHERE HasProfession.ProfessionID = @professionID AND HasProfession.FarmerID = @farmerID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_item(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Item] WHERE Item.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Item WHERE [Item].ID = @ID
		--If it's in shopsells, delete it
		IF EXISTS(SELECT * FROM ShopSells WHERE ShopSells.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].ShopSells WHERE [ShopSells].ItemID = @ID
		END
		--If it's in shopbuys, delete it
		IF EXISTS(SELECT * FROM ShopBuys WHERE ShopBuys.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].ItemID = @ID
		END
		--If it's in produce, delete it + check if it's in PlantProduct, AnimalProduct, Generates --Done
		IF EXISTS(SELECT * FROM Produce WHERE Produce.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Produce WHERE [Produce].ID = @ID
			IF EXISTS(SELECT * FROM PlantProduct WHERE PlantProduct.ID = @ID)
			BEGIN
				DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
			END
			IF EXISTS(SELECT * FROM AnimalProduct WHERE AnimalProduct.ID = @ID)
			BEGIN
				DELETE FROM [dbo].AnimalProduct WHERE [AnimalProduct].ID = @ID
				IF EXISTS(SELECT * FROM Produces WHERE Produces.ProductID = @ID)
				BEGIN
					DELETE FROM [dbo].Produces WHERE Produces.ProductID = @ID
				END
			END
			IF EXISTS(SELECT * FROM Generates WHERE Generates.ProduceID = @ID)
			BEGIN
				DELETE FROM Generates WHERE Generates.ProduceID = @ID
			END
		END
		--If it's in food, delete it + check if it's in HasIngredient --Done
		IF EXISTS(SELECT * FROM Food WHERE Food.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Food WHERE [Food].ID = @ID
			IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.FoodID = @ID)
			BEGIN
				DELETE FROM HasIngredient WHERE HasIngredient.FoodID = @ID
			END
		END
		--If it's in Seed, delete it
		IF EXISTS(SELECT * FROM Seed WHERE Seed.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Seed WHERE [Seed].ID = @ID
		END
		--If it's in fish, delete it
		IF EXISTS(SELECT * FROM Fish WHERE Fish.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Fish WHERE Fish.ID = @ID
		END
		--If it's in artisan good, delete it + check Generates --Done
		IF EXISTS(SELECT * FROM ArtisanGood WHERE ArtisanGood.ID = @ID)
		BEGIN
			DELETE FROM [dbo].ArtisanGood WHERE ArtisanGood.ID = @ID
			IF EXISTS(SELECT * FROM Generates WHERE Generates.ProductID = @ID)
			BEGIN
				DELETE FROM Generates WHERE Generates.ProductID = @ID
			END
		END
		--If it's in animal, delete it + check Produces --Done
		IF EXISTS(SELECT * FROM Animal WHERE Animal.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Animal WHERE Animal.ID = @ID
			IF EXISTS(SELECT * FROM Produces WHERE Produces.AnimalID = @ID)
			BEGIN
				DELETE FROM [dbo].Produces WHERE [Produces].AnimalID = @ID
			END
		END
		--If it's in farm selss, delete it
		IF EXISTS(SELECT * FROM FarmSells WHERE FarmSells.ItemID = @ID)
		BEGIN
			DELETE FROM [dbo].FarmSells WHERE FarmSells.ItemID = @ID
		END
		--If it's in hasingredient, delete it
		IF EXISTS(SELECT * FROM HasIngredient WHERE HasIngredient.IngredientID = @ID)
		BEGIN
			DELETE FROM [dbo].HasIngredient WHERE HasIngredient.IngredientID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_needs(
	@ItemID int,
	@VillagerID int
) AS
	IF @ItemID IS NULL OR @VillagerID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Needs] WHERE Needs.[VillagerID] = @VillagerID AND Needs.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing row in the needs relationship', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Needs WHERE [Needs].VillagerID = @VillagerID
		DELETE FROM [dbo].Needs WHERE [Needs].ItemID = @ItemID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_plantProduct(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[PlantProduct] WHERE PlantProduct.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing plant product item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_produce(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Produce] WHERE Produce.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing produce item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Produce WHERE [Produce].ID = @ID
		IF EXISTS (SELECT * FROM PlantProduct WHERE PlantProduct.ID = @ID)
		BEGIN
			DELETE FROM [dbo].PlantProduct WHERE [PlantProduct].ID = @ID
		END
		IF EXISTS (SELECT * FROM AnimalProduct WHERE AnimalProduct.ID = @ID)
		BEGIN
			DELETE FROM AnimalProduct WHERE AnimalProduct.ID = @ID
		END
		IF EXISTS (SELECT * FROM Generates WHERE Generates.ProduceID = @ID)
		BEGIN
			DELETE FROM Generates WHERE Generates.ProduceID = @ID
		END
		IF EXISTS (SELECT * FROM Produces WHERE Produces.ProductID = @ID)
		BEGIN
			DELETE FROM Produces WHERE Produces.ProductID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_produces(
	@AnimalID int,
	@ProductID int
) AS
	IF @AnimalID IS NULL OR @ProductID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Produces] WHERE Produces.[AnimalID] = @AnimalID AND Produces.ProductID = @ProductID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Produces WHERE Produces.AnimalID = @AnimalID AND Produces.ProductID = @ProductID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_profession(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Profession] WHERE Profession.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Profession WHERE Profession.ID = @ID
		IF EXISTS(SELECT * FROM HasProfession WHERE HasProfession.ProfessionID = @ID)
		BEGIN
			DELETE FROM HasProfession WHERE HasProfession.ProfessionID = @ID
		END
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_seed(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Seed] WHERE Seed.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing seed', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Seed WHERE [Seed].ID = @ID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_shop(
	@Name varchar(20)
) AS
	IF @Name IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Shop] WHERE Shop.[Name] = @Name)
	BEGIN
		RAISERROR('Must try to delete an existing shop', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].Shopkeeper WHERE [Shopkeeper].ID = (SELECT Shop.OwnerID FROM Shop WHERE Shop.[Name] = @Name)
		DELETE FROM [dbo].Villager WHERE [Villager].ID = (SELECT Shop.OwnerID FROM Shop WHERE Shop.[Name] = @Name)
		DELETE FROM [dbo].[Shop] WHERE [Shop].[Name] = @Name
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_shopBuys(
	@ShopName varchar(20),
	@ItemID int
) AS
	IF @ShopName IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ShopBuys] WHERE ShopBuys.ShopID = @ShopName AND ShopBuys.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing shop + item relation', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].[ShopID] = @ShopName AND ShopBuys.ItemID = @ItemID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_shopkeeper(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Shopkeeper] WHERE Shopkeeper.ID = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing shopkeeper', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].[Shopkeeper] WHERE [Shopkeeper].ID = @ID
		DELETE FROM [dbo].[Villager] WHERE [Villager].ID = @ID
		DELETE FROM [dbo].[Shop] WHERE [Shop].OwnerID = @ID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_shopSells(
	@ShopName varchar(20),
	@ItemID int
) AS
	IF @ShopName IS NULL OR @ItemID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[ShopSells] WHERE ShopSells.ShopID = @ShopName AND ShopSells.ItemID = @ItemID)
	BEGIN
		RAISERROR('Must try to delete an existing shop + item relation', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM [dbo].ShopSells WHERE [ShopSells].[ShopID] = @ShopName AND ShopSells.ItemID = @ItemID
	END

	RETURN 0
CREATE OR ALTER PROCEDURE delete_villager(
	@ID int
) AS
	IF @ID IS NULL
	BEGIN
		RAISERROR('Parameters cannot be null', 1, 1)
		RETURN 1
	END
	IF NOT EXISTS (SELECT * FROM [dbo].[Villager] WHERE Villager.[ID] = @ID)
	BEGIN
		RAISERROR('Must try to delete an existing item', 2, 1)
		RETURN 2
	END
	ELSE
	BEGIN
		DELETE FROM Villager WHERE Villager.ID = @ID
		IF EXISTS(SELECT * FROM Farmer WHERE Farmer.VillagerID = @ID)
		BEGIN
			DELETE FROM [dbo].Farmer WHERE [Farmer].VillagerID = @ID
			IF EXISTS(SELECT * FROM FarmSells WHERE FarmSells.FarmerID = @ID)
			BEGIN
				DELETE FROM [dbo].FarmSells WHERE [FarmSells].FarmerID = @ID
			END
			IF EXISTS(SELECT * FROM HasProfession WHERE HasProfession.FarmerID = @ID)
			BEGIN
				DELETE FROM [dbo].HasProfession WHERE [HasProfession].FarmerID = @ID
			END
		END
		IF EXISTS(SELECT * FROM Shopkeeper WHERE Shopkeeper.ID = @ID)
		BEGIN
			DELETE FROM [dbo].Shopkeeper WHERE [Shopkeeper].ID = @ID
			DELETE FROM [dbo].Shop WHERE [Shop].OwnerID = @ID
			IF EXISTS(SELECT * FROM ShopSells WHERE ShopSells.ShopID = @ID)
			BEGIN
				DELETE FROM [dbo].ShopSells WHERE [ShopSells].ShopID = @ID
			END
			IF EXISTS(SELECT * FROM ShopBuys WHERE ShopBuys.ShopID = @ID)
			BEGIN
				DELETE FROM [dbo].ShopBuys WHERE [ShopBuys].ShopID = @ID
			END
		END
	END

	RETURN 0
create or alter procedure update_Animal (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Animal: ID cannot be null.'
		return 1
	end
	if not exists (select * from Animal where ID = @ID) begin
		print 'ERROR in update_Animal: The animal with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Item @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
create or alter procedure update_AnimalProduct (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_AnimalProduct: ID cannot be null.'
		return 1
	end
	if not exists (select * from AnimalProduct where ID = @ID) begin
		print 'ERROR in update_AnimalProduct: The animal product with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Produce @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
create or alter procedure update_ArtisanGood (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Multiplier decimal = null
) as
	if @ID is null begin
		print 'ERROR in update_ArtisanGood: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null and @Multiplier is null begin
		print 'ERROR in update_ArtisanGood: At least one of Name, Quality, BasePrice, or Multiplier must be non-null.'
		return 2
	end
	if not exists (select * from ArtisanGood where ID = @ID) begin
		print 'ERROR in update_ArtisanGood: The artisan good with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int, @CurrMultiplier decimal
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice, @CurrMultiplier = Multiplier
	from ArtisanGood
	join Item on ArtisanGood.ID = Item.ID
	where ArtisanGood.ID = @ID
	
	if @Name       is null begin set @Name       = @CurrName       end
	if @Quality    is null begin set @Quality    = @CurrQuality    end
	if @BasePrice  is null begin set @BasePrice  = @CurrBasePrice  end
	if @Multiplier is null begin set @Multiplier = @CurrMultiplier end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_ArtisanGood: Could not update the item data of the artisan good with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	update ArtisanGood
	set Multiplier = @Multiplier
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_ArtisanGood: Could not update the multiplier of the artisan good with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_ArtisanGood: Successfully updated the data for the artisan good with ID ' + convert(varchar(20), @ID) + '.'
	return 0
create or alter procedure update_Farm (
	@ID int,
	@Season varchar(6) = null,
	@Name varchar(30) = null
) as
	if @ID is null begin
		print 'ERROR in update_Farm: ID cannot be null.'
		return 1
	end
	if @Season is null and @Name is null begin
		print 'ERROR in update_Farm: At least one of Season or Name must be non-null'
		return 2
	end
	if not exists (select * from Farm where ID = @ID) begin
		print 'ERROR in update_Farm: The farm with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	if @Season not in ('Spring', 'Summer', 'Fall', 'Winter') begin
		print 'ERROR in update_Farm: Season must be one of ''Spring'', ''Summer'', ''Fall'', or ''Winter''.'
		return 4
	end

	declare @Status int
	declare @CurrSeason varchar(6), @CurrName varchar(30)
	select @CurrSeason = Season, @CurrName = Name
	from Farm
	where ID = @ID
	
	if @Season is null begin set @Season = @CurrSeason end
	if @Name   is null begin set @Name   = @CurrName   end

	update Farm
	set Season = @Season, Name = @Name
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Farm: Could not update the data of the farm with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Farm: Successfully updated the data for the farm with ID ' + convert(varchar(20), @ID) + '.'
	return 0
create or alter procedure update_Farmer (
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
create or alter procedure update_Fish (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Fish: ID cannot be null.'
		return 1
	end
	if not exists (select * from Fish where ID = @ID) begin
		print 'ERROR in update_Fish: The fish with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Item @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
create or alter procedure update_Food (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Food: ID cannot be null.'
		return 1
	end
	if not exists (select * from Food where ID = @ID) begin
		print 'ERROR in update_Food: The food with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Item @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
create or alter procedure update_Item (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Item: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null begin
		print 'ERROR in update_Item: At least one of Name, Quality, or BasePrice must be non-null.'
		return 2
	end
	if not exists (select * from Item where ID = @ID) begin
		print 'ERROR in update_Item: The item with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice
	from Item
	where ID = @ID
	
	if @Name      is null begin set @Name      = @CurrName      end
	if @Quality   is null begin set @Quality   = @CurrQuality   end
	if @BasePrice is null begin set @BasePrice = @CurrBasePrice end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Item: Could not update the data of the item with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Item: Successfully updated the data for the item with ID ' + convert(varchar(20), @ID) + '.'
	return 0
create or alter procedure update_Login (
	@Username varchar(30),
	@Hash varbinary(16),
	@Salt varbinary(16)
) as
	if @Username is null or @Hash is null or @Salt is null begin
		raiserror('ERROR in update_Login: None of Username, Hash, or Salt can be null.', 14, 1)
		return 1
	end
	if not exists (select * from Login where Username = @Username) begin
		raiserror('ERROR in update_Login: The username %s does not exist.', 14, 1, @Username)
		return 2
	end
	
	declare @Status int

	update Login
	set Hash = @Hash, Salt = @Salt
	where Username = @Username
	set @Status = @@ERROR
	if @Status != 0 begin
		raiserror('ERROR in update_Login: Could not update the password of %s.', 14, 1, @Username)
		return @Status
	end
	
	print 'update_Login: Successfully updated the password for ' + @Username + '.'
create or alter procedure update_Needs (
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
create or alter procedure update_PlantProduct (
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
create or alter procedure update_Produce (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null
) as
	if @ID is null begin
		print 'ERROR in update_Produce: ID cannot be null.'
		return 1
	end
	if not exists (select * from Produce where ID = @ID) begin
		print 'ERROR in update_Produce: The produce with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	
	declare @Status int
	execute @Status = update_Item @ID, @Name, @Quality, @BasePrice
	if @Status != 0 begin return @Status end
	
	return 0
create or alter procedure update_Profession (
	@ID int,
	@BoostCategory varchar(10) = null,
	@BoostMultiplier decimal = null
) as
	if @ID is null begin
		print 'ERROR in update_Profession: ID cannot be null.'
		return 1
	end
	if @BoostCategory is null and @BoostMultiplier is null begin
		print 'ERROR in update_Profession: At least one of BoostCategory or BoostMultiplier must be non-null'
		return 2
	end
	if not exists (select * from Profession where ID = @ID) begin
		print 'ERROR in update_Profession: The profession with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int
	declare @CurrBoostCategory varchar(10), @CurrBoostMultiplier decimal
	select @CurrBoostCategory = BoostCategory, @CurrBoostMultiplier = BoostMultiplier
	from Profession
	where ID = @ID
	
	if @BoostCategory   is null begin set @BoostCategory   = @CurrBoostCategory   end
	if @BoostMultiplier is null begin set @BoostMultiplier = @CurrBoostMultiplier end

	update Profession
	set BoostCategory = @BoostCategory, BoostMultiplier = @BoostMultiplier
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Profession: Could not update the data of the profession with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Profession: Successfully updated the data for the profession with ID ' + convert(varchar(20), @ID) + '.'
	return 0
create or alter procedure update_Seed (
	@ID int,
	@Name varchar(20) = null,
	@Quality tinyint = null,
	@BasePrice int = null,
	@Season varchar(6) = null
) as
	if @ID is null begin
		print 'ERROR in update_Seed: ID cannot be null.'
		return 1
	end
	if @Name is null and @Quality is null and @BasePrice is null and @Season is null begin
		print 'ERROR in update_Seed: At least one of Name, Quality, BasePrice, or Season must be non-null.'
		return 2
	end
	if not exists (select * from Seed where ID = @ID) begin
		print 'ERROR in update_Seed: The seed with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end
	if @Season not in ('Spring', 'Summer', 'Fall', 'Spring/Summer', 'Spring/Fall', 'Summer/Fall', 'All', 'None') begin
		print 'ERROR in update_Seed: Season must be one of ''Spring'', ''Summer'', ''Fall'', ''Spring/Summer'', ''Spring/Fall'', ''Summer/Fall'', ''All'', or ''None''.'
		return 4
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrQuality tinyint, @CurrBasePrice int, @CurrSeason varchar(6)
	select @CurrName = Name, @CurrQuality = Quality, @CurrBasePrice = BasePrice, @CurrSeason = Season
	from Seed
	join Item on Seed.ID = Item.ID
	where Seed.ID = @ID
	
	if @Name      is null begin set @Name      = @CurrName      end
	if @Quality   is null begin set @Quality   = @CurrQuality   end
	if @BasePrice is null begin set @BasePrice = @CurrBasePrice end
	if @Season    is null begin set @Season    = @CurrSeason    end
	
	update Item
	set Name = @Name, Quality = @Quality, BasePrice = @BasePrice
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Seed: Could not update the item data of the seed with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	update Seed
	set Season = @Season
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Seed: Could not update the season of the seed with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end
	
	print 'update_Seed: Successfully updated the data for the seed with ID ' + convert(varchar(20), @ID) + '.'
	return 0
create or alter procedure update_Shop (
	@OwnerID int,
	@Name varchar(20) = null,
	@Address varchar(40) = null,
	@Schedule varchar(100) = null
) as
	if @OwnerID is null begin
		print 'ERROR in update_Shop: OwnerID cannot be null.'
		return 1
	end
	if @Name is null and @Address is null and @Schedule is null begin
		print 'ERROR in update_Shop: At least one of Name, Address, or Schedule must be non-null.'
		return 2
	end
	if not exists (select * from Shop where OwnerID = @OwnerID) begin
		print 'ERROR in update_Shop: The Shop with OwnerID ' + convert(varchar(30), @OwnerID) + ' does not exist.'
		return 3
	end
	if exists (select * from Shop where OwnerID != @OwnerID and Name = @Name) begin
		print 'ERROR in update_Shop: A shop with the name ' + @Name + ' already exists.'
		return 4
	end
	
	declare @Status int
	declare @CurrName varchar(20), @CurrAddress varchar(40), @CurrSchedule varchar(100)
	select @CurrName = Name, @CurrAddress = Address, @CurrSchedule = Schedule
	from Shop
	where OwnerID = @OwnerID
	
	if @Name     is null begin set @Name     = @CurrName     end
	if @Address  is null begin set @Address  = @CurrAddress  end
	if @Schedule is null begin set @Schedule = @CurrSchedule end
	
	update Shop
	set Name = @Name, Address = @Address, Schedule = @Schedule
	where OwnerID = @OwnerID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Shop: Could not update the data of the Shop with ID ' + convert(varchar(20), @OwnerID) + '.'
		return @Status
	end
	
	print 'update_Shop: Successfully updated the data for the Shop with ID ' + convert(varchar(20), @OwnerID) + '.'
	return 0
create or alter procedure update_Shopkeeper (
	@ID int,
	@Name varchar(30) = null,
	@IsDeleted bit = null
) as
	if @ID is null begin
		print 'ERROR in update_Shopkeeper: ID cannot be null.'
		return 1
	end
	if @Name is null and @IsDeleted is null begin
		print 'ERROR in update_Shopkeeper: At least one of Name or IsDeleted must be non-null.'
		return 2
	end
	if not exists (select * from Shopkeeper where ID = @ID) begin
		print 'ERROR in update_Shopkeeper: The Shopkeeper with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int

	if @Name is not null begin
		execute @Status = update_Villager @ID, @Name
		if @Status != 0 begin return @Status end
	end

	if @IsDeleted is not null begin
		update Shopkeeper
		set IsDeleted = @IsDeleted
		where ID = @ID
		set @Status = @@ERROR
		if @Status != 0 begin
			print 'ERROR in update_Shopkeeper: Could not update the deletion status of the Shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
			return @Status
		end
		print 'update_Shopkeeper: Successfully updated the data for the Shopkeeper with ID ' + convert(varchar(20), @ID) + '.'
	end

	return 0
create or alter procedure update_Villager (
	@ID int,
	@Name varchar(30)
) as
	if @ID is null begin
		print 'ERROR in update_Villager: ID cannot be null.'
		return 1
	end
	if @Name is null begin
		print 'ERROR in update_Villager: Name cannot be null.'
		return 2
	end
	if not exists (select * from Villager where ID = @ID) begin
		print 'ERROR in update_Villager: The villager with ID ' + convert(varchar(30), @ID) + ' does not exist.'
		return 3
	end

	declare @Status int
	update Villager
	set Name = @Name
	where ID = @ID
	set @Status = @@ERROR
	if @Status != 0 begin
		print 'ERROR in update_Villager: Could not update the name of the villager with ID ' + convert(varchar(20), @ID) + '.'
		return @Status
	end

	print 'update_Villager: Successfully updated the data for the villager with ID ' + convert(varchar(20), @ID) + '.'
	return 0
