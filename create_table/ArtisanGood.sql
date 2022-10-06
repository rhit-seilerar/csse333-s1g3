use StardewHoes
go

create table ArtisanGood (
	ID int,
	Multiplier real not null
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade,
	Check (Multiplier >= 0)
)