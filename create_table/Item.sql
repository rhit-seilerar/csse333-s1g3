use StardewHoes
go

create table Item (
	ID int identity(0,1),
	Name varchar(20) not null,
	Quality tinyint,
	BasePrice int not null
	Primary key (ID),
	Check(Quality <= 3),
	Check(BasePrice >= 0)
)