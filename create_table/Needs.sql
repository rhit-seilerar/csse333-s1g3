use StardewHoes
go

create table Needs (
	VillagerID int,
	ItemID int,
	Reward int,
	Quantity int
	Primary key (VillagerID, ItemID),
	Foreign key (VillagerID) references Villager(ID),
	Foreign key (ItemID) references Item(ID)
)