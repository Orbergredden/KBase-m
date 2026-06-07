@rem -- --

@rem SET PGCLIENTENCODING=utf-8
@rem chcp 65001
@rem SET PGCLIENTENCODING=WIN1251
@chcp 1251

%MC_PG%\bin\psql --host localhost --port 5432 --username "postgres"

pause
@rem -- -