package com.example.helloworld.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class SQLiteJDBCInvoker extends GenericJDBCInvoker {
	private static Logger log = Logger.getLogger(SQLiteJDBCInvoker.class);

	public void setNString(PreparedStatement stmt, final int parIndex, final String par) throws SQLException {
		try {
			stmt.setString(parIndex, par);
		} catch (SQLException e) {
			log.error("SQLiteJDBCInvoker.setNString(): index:"+parIndex+" ;value:"+" ;exception:"+e);
			throw e;
		}
	}
}
