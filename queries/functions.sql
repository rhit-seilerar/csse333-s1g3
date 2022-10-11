use StardewHoes10
go

create procedure assert_int_not_null (
	@Param int = null,
	@ParamName varchar(20) = 'parameter',
	@LocName varchar(40) = 'stored procedure'
) as
if @Param is null begin
	print 'ERROR in ' + @LocName + ': ' + @ParamName + ' cannot be null.'
	return 1
end
return 0
go