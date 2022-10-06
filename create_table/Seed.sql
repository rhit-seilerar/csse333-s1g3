use StardewHoes
go

create table Seed (
	ID int,
	Season varchar(6)
	Primary Key (ID),
	Foreign Key (ID) references Item(ID)
	on delete cascade,
	Check(Season in ('Spring', 'Summer', 'Fall', 'Spring/Summer', 'Spring/Fall', 'Summer/Fall', 'All', 'None'))
)