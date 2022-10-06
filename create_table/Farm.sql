USE [StardewHoes]
GO
CREATE TABLE Farm(
	ID int IDENTITY(1,1) NOT NULL PRIMARY KEY,
	Season varchar(6) NULL,
	[Name] varchar(30) NULL,
	CHECK  (Season IN ('Spring', 'Winter', 'Fall', 'Summer')))

