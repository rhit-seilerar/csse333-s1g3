use StardewHoes
go

create table Item (
	ID int,
	Name varchar(20) not null,
	Quality tinyint,
	BasePrice int
	Primary key (ID),
	Check(Quality <= 3),
	Check(BasePrice > 0)
)