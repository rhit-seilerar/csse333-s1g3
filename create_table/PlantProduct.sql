use StardewHoes
go

create table PlantProduct (
	ProduceID int,
	Type varchar(20)
	Primary key (ProduceID)
	Foreign key (ProduceID) references Produce(ID)
)