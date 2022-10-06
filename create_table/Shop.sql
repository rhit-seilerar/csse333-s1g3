use StardewHoes
go

create table Shop (
	Name varchar(20),
	Address varchar(40),
	Schedule varchar(100),
	OwnerID int
	Primary Key (Name),
	Foreign Key (OwnerID) references Shopkeeper(ID)
	on delete set null
)