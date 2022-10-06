use StardewHoes
go

create table Produces (
	AnimalID int,
	ProductID int
	Primary Key (AnimalID, ProductID)
	Foreign Key (AnimalID) references Animal(ID)
	on delete no action,
	Foreign Key (ProductID) references Produce(ID)
	on delete cascade
)