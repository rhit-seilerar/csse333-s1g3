use StardewHoes
go

create table Produce (
	ItemID int
	Primary key (ItemID),
	Foreign key (ItemID) references Item(ID)
)