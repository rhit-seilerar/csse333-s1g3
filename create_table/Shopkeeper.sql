use StardewHoes
go

create table Shopkeeper (
	ID int,
	IsDeleted bit default 0
	Primary Key (ID),
	Foreign Key (ID) references Villager(ID)
	on delete cascade
)