use StardewHoes
go

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