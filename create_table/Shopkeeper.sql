use StardewHoes
go

create table Shopkeeper (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Villager(ID)
	on delete cascade
)