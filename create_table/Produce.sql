use StardewHoes
go

create table Produce (
	ID int
	Primary key (ID),
	Foreign key (ID) references Item(ID)
	on delete cascade
)