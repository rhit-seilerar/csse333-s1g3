use StardewHoes
go

create table Fish (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)