use StardewHoes
go

create table Generates (
	ProduceID int,
	ProductID int
	Primary Key (ProduceID, ProductID),
	Foreign Key (ProduceID) references Produce(ID)
	on delete cascade,
	Foreign Key (ProductID) references ArtisanGood(ID)
	on delete no action
)