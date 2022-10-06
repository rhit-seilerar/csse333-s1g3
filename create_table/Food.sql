use StardewHoes
go

create table Food (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)