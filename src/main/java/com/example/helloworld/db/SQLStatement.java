package com.example.helloworld.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.example.helloworld.db.sql.GenericJDBCInvoker;

public class SQLStatement {
	private PreparedStatement mStatement = null;
	private PreparedStatement mLastInsertedIDStatement = null;
	private String mSQL = null;
	private HashMap<Integer, Object> mParameters;
	private GenericJDBCInvoker mJDBCInvoker;

	public SQLStatement(final Connection connection, final String sql, GenericJDBCInvoker jdbcInvoker)
			throws SQLException {
		mStatement = connection.prepareStatement(sql);
		//TODO: Make it db independent here or decouple this to dynamic binding/strategiese depending on dbvendor
		mLastInsertedIDStatement = connection.prepareStatement("SELECT last_insert_rowid()");
		mSQL = sql;
		mParameters = new HashMap();
		this.mJDBCInvoker = jdbcInvoker;
	}

	public String getSQL() {
		return mSQL;
	}

	public void setBigDecimal(final int parIndex, final BigDecimal par) throws SQLException {
		this.mJDBCInvoker.setBigDecimal(this.getStatement(), parIndex, par);
		this.setNewParameter(parIndex, par);
	}

	public void setFloat(final int parIndex, final float par) throws SQLException {
		this.mJDBCInvoker.setFloat(this.getStatement(), parIndex, par);
		this.setNewParameter(parIndex, par);
	}
	
	public void setInt(final int parIndex, final int par) throws SQLException {
		this.mJDBCInvoker.setInt(this.getStatement(), parIndex, par);
		this.setNewParameter(parIndex, par);
	}

	public void setNString(final int parIndex, final String par) throws SQLException {
		this.mJDBCInvoker.setNString(this.getStatement(), parIndex, par);
		this.setNewParameter(parIndex, par);
	}

	public void setNull(final int parIndex, int dataType)  throws SQLException {
		this.mJDBCInvoker.setNull(this.getStatement(), parIndex, dataType);
		this.setNewParameter(parIndex, null);
	}
	
	public int executeInsert() throws SQLException {
		int ret =-100;
		System.out.println("Going to execute SQL statement : " + this.getSQL());
		System.out.println("      parameters   : " + mParameters.toString());
		try {
			this.getStatement().executeUpdate();//insert the data
			ResultSet rs = this.getLastInsertedIdStatement().executeQuery();//retrieve the id
			
			while ( rs.next() ) {
				ret = rs.getInt(1);
			}
			
		} catch (SQLException e) {
			System.out.println("Caught SQLException with error code : " + e.getErrorCode() + " state : " + e.getSQLState()
						+ " for statement: " + this.getSQL() + " with the following parameters: "
						+ mParameters.toString() + e);
			throw e;
		}
		return ret;
	}
	
	public int executeUpdate() throws SQLException {
		final int ret;
			System.out.println("Going to execute SQL statement : " + this.getSQL());
			System.out.println("      parameters   : " + mParameters.toString());
		try {
			ret = this.getStatement().executeUpdate();
		} catch (SQLException e) {
			System.out.println("Caught SQLException with error code : " + e.getErrorCode() + " state : " + e.getSQLState()
						+ " for statement: " + this.getSQL() + " with the following parameters: "
						+ mParameters.toString() + e);
			throw e;
		}
		return ret;
	}

	public void close() {
		if (this.getStatement() != null) {
			System.out.println("Going to close SQL statement : " + this.getSQL());
			
			try {
				this.getStatement().close();
			} catch (SQLException e) {
				System.out.println("close statement failed : " + this.getSQL() + e);
			}
		}
	}

	protected void setNewParameter(int parIndex, Object par) {
		mParameters.put(parIndex, par);
	}

	protected PreparedStatement getStatement() {
		return mStatement;
	}
	
	protected PreparedStatement getLastInsertedIdStatement() {
		return mLastInsertedIDStatement;
	}
}
