use StardewHoes10
go

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'zellneae' or u.name = 'zellneae'
) begin
   create user zellneae for login zellneae with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'zellneae'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'henderae' or u.name = 'henderae'
) begin
   create user henderae for login henderae with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'henderae'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'seilerar' or u.name = 'seilerar'
) begin
   create user seilerar for login seilerar with default_schema = dbo
   exec sp_addrolemember 'db_owner', 'seilerar'
end

if not exists (
   select u.name
   from syslogins l
   join sysusers u on l.sid = u.sid
   where l.name = 'StardewHoesapp10' or u.name = 'StardewHoesapp10'
) begin
   create user StardewHoesapp10 for login StardewHoesapp10
   grant execute to StardewHoesapp10
end