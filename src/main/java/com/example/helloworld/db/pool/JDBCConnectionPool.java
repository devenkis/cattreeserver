package com.example.helloworld.db.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.example.helloworld.db.sql.GenericJDBCInvoker;
import com.example.helloworld.db.sql.SQLiteJDBCInvoker;


public class JDBCConnectionPool extends ObjectPool<Connection> {
	private STORAGETYPE mDBType;
	private String mDBUrl, mDBUser, mDBPassword;
	boolean autoCommit;
	private GenericJDBCInvoker mJDBCInvoker;

	/**
	 * 
	 * 
	 * @param driver
	 *            can be null but needed for sqlite inmemory version
	 * @param dsn
	 * @param dbUser
	 * @param dbPassword
	 */
	public JDBCConnectionPool(String dbUrl, String dbUser, String dbPassword, boolean autoCommit) {
		super();
		System.out.println(" JDBCConnectionPool: Configured with - URL: " + dbUrl + " user : " + dbUser);
		this.mDBUrl = dbUrl;
		this.mDBUser = dbUser;
		this.mDBPassword = dbPassword;
		this.autoCommit = autoCommit;
		if (this.mDBUrl.toLowerCase().contains("sqlite")) {
			this.mDBType = STORAGETYPE.SQLITE;
			this.mJDBCInvoker = new SQLiteJDBCInvoker();
		} else if (this.mDBUrl.toLowerCase().contains("postgresql")) {
			this.mDBType = STORAGETYPE.POSTGRES;
			this.mJDBCInvoker = new GenericJDBCInvoker();
		} else if (this.mDBUrl.toLowerCase().contains("sqlserver")) {
			this.mDBType = STORAGETYPE.MSSQL;
			this.mJDBCInvoker = new GenericJDBCInvoker();
		} else if (this.mDBUrl.toLowerCase().contains("sap")) {
			this.mDBType = STORAGETYPE.SAPHANA;
			this.mJDBCInvoker = new GenericJDBCInvoker();
		} else {
			this.mDBType = STORAGETYPE.ORACLE;
			this.mJDBCInvoker = new GenericJDBCInvoker();
		}
	}

	public STORAGETYPE getSTORAGEType() {
		return this.mDBType;
	}

	public boolean isAutoCommit() {
		return this.autoCommit;
	}

	public GenericJDBCInvoker getCoreInvoker() {
		return this.mJDBCInvoker;
	}

	@Override
	protected Connection create() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(mDBUrl, mDBUser, mDBPassword);
			con.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			System.out.println("JDBCConnectionPool: create(): Exception: " + e);
		}
		return con;
	}

	@Override
	public void expire(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			System.out.println("JDBCConnectionPool: expire(): Exception: " + e);
		}
	}

	@Override
	public boolean validate(Connection con) {
		try {
			return !con.isClosed();
		} catch (SQLException e) {
			System.out.println("JDBCConnectionPool: validate(): Exception: " + e);
			return (false);
		}
	}

	@Override
	public void handleExceptionOnObjectInPool(Connection con, Exception e) {
		System.out.println("JDBCConnectionPool: handleExceptionOnObjectInPool(): " + e);
		// rollback silently
		try {
			con.rollback();
		} catch (Exception ex) {
			System.out.println("JDBCConnectionPool: handleExceptionOnObjectInPool(): Unable to rollback and close connection."+
					ex);
		}
		// close statements

		// close or expire connection
		expire(con);
		// Checkin the connection after closing as this will be validated on
		// next request and a new connection will be created
		checkIn(con);
	}
}
