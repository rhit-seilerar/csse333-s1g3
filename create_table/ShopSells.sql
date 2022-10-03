use StardewHoes
go

create table ShopSells (
	ShopName varchar(20),
	ItemID int
	Primary Key (ShopName, ItemID),
	Foreign Key (ShopName) references Shop(Name),
	Foreign Key (ItemID) references Item(ID)
)