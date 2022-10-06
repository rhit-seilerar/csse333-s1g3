USE [StardewHoes]
GO
CREATE TABLE Profession(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	BoostCategory varchar(10) NULL,
	BoostMultiplier decimal(18, 0) NULL)

