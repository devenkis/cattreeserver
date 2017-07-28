package com.example.helloworld.db.sql;


import java.sql.SQLException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;

public class GenericJDBCInvoker {

	public void setBigDecimal(PreparedStatement stmt, final int parIndex, final BigDecimal par) throws SQLException {
		try {
			stmt.setBigDecimal(parIndex, par);
		} catch (SQLException e) {
			System.out.println("GenericJDBCInvoker.setBigDecimal(): index:" + parIndex + " ;value:" + " ;exception:" + e);
			throw e;
		}
	}

	public void setFloat(PreparedStatement stmt, final int parIndex, final float par) throws SQLException {
		try {
			stmt.setFloat(parIndex, par);
		} catch (SQLException e) {
			System.out.println("GenericJDBCInvoker.setFloat(): index:" + parIndex + " ;value:" + " ;exception:" + e);
			throw e;
		}
	}
	
	public void setInt(PreparedStatement stmt, final int parIndex, final int par) throws SQLException {
		try {
			stmt.setInt(parIndex, par);
		} catch (SQLException e) {
			System.out.println("GenericJDBCInvoker.setInt(): index:" + parIndex + " ;value:" + " ;exception:" + e);
			throw e;
		}
	}

	public void setNString(PreparedStatement stmt, final int parIndex, final String par) throws SQLException {
		try {
			stmt.setNString(parIndex, par);
		} catch (SQLException e) {
			System.out.println("GenericJDBCInvoker.setNString(): index:" + parIndex + " ;value:" + " ;exception:" + e);
			throw e;
		}
	}
	
	public void setNull(PreparedStatement stmt, final int parIndex, int dataType)throws SQLException {
		try {
			stmt.setNull(parIndex, dataType );
		} catch (SQLException e) {
			System.out.println("GenericJDBCInvoker.setNString(): index:" + parIndex + " ;value:" + " ;exception:" + e);
			throw e;
		}
	}

}

