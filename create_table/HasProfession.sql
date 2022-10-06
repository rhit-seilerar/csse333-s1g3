USE [StardewHoes]
GO
CREATE TABLE HasProfession(
	ProfessionID int NOT NULL,
	FarmerID int NOT NULL,
	PRIMARY KEY (ProfessionID, FarmerID),
	FOREIGN KEY(FarmerID) REFERENCES Farmer(VillagerID),
	FOREIGN KEY(ProfessionID) REFERENCES Profession(ID))


