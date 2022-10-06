use StardewHoes
go

create table ShopBuys (
	ShopName varchar(20),
	ItemID int
	Primary Key (ShopName, ItemID),
	Foreign Key (ShopName) references Shop(Name)
	on delete cascade,
	Foreign Key (ItemID) references Item(ID)
	on delete cascade
)