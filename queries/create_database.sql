use master
go

create database [StardewHoes10]
on (
	name = [StardewHoes10],
	filename = 'D:\Database\MSSQL15.MSSQLSERVER\MSSQL\DATA\StardewHoes10.mdf',
	size = 8mb,
   maxsize = unlimited,
	filegrowth = 10%
)
log on (
	name = [StardewHoes10_log],
	filename = 'D:\Database\MSSQL15.MSSQLSERVER\MSSQL\DATA\StardewHoes10_log.ldf',
	size = 4mb,
	maxsize = 2tb,
	filegrowth = 10%
)