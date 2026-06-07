package app.module.scheduler.db;

import java.sql.Connection;

public record DbEntry(
		String name, 
		String dbType, 
		String url, 
		String user, 
		String password,
		boolean connectByPlugin,
		Connection conn
		) {}
