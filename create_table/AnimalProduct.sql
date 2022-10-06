use StardewHoes
go

create table AnimalProduct (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Produce(ID)
	on delete cascade
)