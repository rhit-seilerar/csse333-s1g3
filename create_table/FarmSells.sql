USE [StardewHoes]
GO
CREATE TABLE FarmSells(
	FarmerID int NOT NULL,
	ItemID int NOT NULL,
	Price money NULL,
	PRIMARY KEY (FarmerID, ItemID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID),
	FOREIGN KEY(ItemID) REFERENCES Item(ID))


