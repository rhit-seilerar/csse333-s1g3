use StardewHoes
go

create table Animal (
	ID int
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade
)