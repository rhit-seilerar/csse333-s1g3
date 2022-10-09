use StardewHoes
go

CREATE TABLE Villager(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	Name varchar(30) NOT NULL)

create table Item (
	ID int identity(0,1),
	Name varchar(20) not null,
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

create table AnimalProduct (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Produce(ID)
	on delete cascade
)

create table Produce (
	ID int
	Primary key (ID),
	Foreign key (ID) references Item(ID)
	on delete cascade
)

create table PlantProduct (
	ProduceID int,
	Type varchar(20)
	Primary key (ProduceID)
	Foreign key (ProduceID) references Produce(ID)
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
	on delete cascade
)

create table Generates (
	ProduceID int,
	ProductID int
	Primary Key (ProduceID, ProductID),
	Foreign Key (ProduceID) references Produce(ID)
	on delete cascade,
	Foreign Key (ProductID) references ArtisanGood(ID)
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
	CHECK  (Season IN ('Spring', 'Winter', 'Fall', 'Summer')))

CREATE TABLE Farmer(
	VillagerID int NOT NULL PRIMARY KEY,
	FarmID int NOT NULL,
	FOREIGN KEY(FarmID) REFERENCES Farm(ID),
	FOREIGN KEY(VillagerID) REFERENCES Villager(ID))

CREATE TABLE HasIngredient(
	IngredientID int NOT NULL,
	FoodID int NOT NULL,
	PRIMARY KEY(IngredientID, FoodID),
	FOREIGN KEY(IngredientID) REFERENCES Item(ID),
	FOREIGN KEY(FoodID) REFERENCES Food(ID))

CREATE TABLE Profession(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	BoostCategory varchar(10) NULL,
	BoostMultiplier decimal(18, 0) NULL)

CREATE TABLE HasProfession(
	ProfessionID int NOT NULL,
	FarmerID int NOT NULL,
	PRIMARY KEY (ProfessionID, FarmerID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID),
	FOREIGN KEY(ProfessionID) REFERENCES Profession(ID))

CREATE TABLE FarmSells(
	FarmerID int NOT NULL,
	ItemID int NOT NULL,
	Price money NULL,
	PRIMARY KEY (FarmerID, ItemID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID),
	FOREIGN KEY(ItemID) REFERENCES Item(ID))

create table Shop (
	Name varchar(20),
	Address varchar(40),
	Schedule varchar(100),
	OwnerID int
	Primary Key (Name),
	Foreign Key (OwnerID) references Shopkeeper(ID)
	on delete set null
)

create table Shopkeeper (
	ID int,
	IsDeleted bit default 0
	Primary Key (ID),
	Foreign Key (ID) references Villager(ID)
	on delete cascade
)

create table ShopBuys (
	ShopName varchar(20),
	ItemID int
	Primary Key (ShopName, ItemID),
	Foreign Key (ShopName) references Shop(Name)
	on delete cascade,
	Foreign Key (ItemID) references Item(ID)
	on delete cascade
)

create table ShopSells (
	ShopName varchar(20),
	ItemID int
	Primary Key (ShopName, ItemID),
	Foreign Key (ShopName) references Shop(Name)
	on delete cascade,
	Foreign Key (ItemID) references Item(ID)
	on delete cascade
)