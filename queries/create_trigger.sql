use StardewHoes10
go

create or alter trigger after_delete_Item on Item after delete as
	delete from Generates
	where ProductID in (select ID from Deleted) or ProduceID in (select ID from Deleted)

	delete from Produces
	where ProductID in (select ID from Deleted) or AnimalID in (select ID from Deleted)
go