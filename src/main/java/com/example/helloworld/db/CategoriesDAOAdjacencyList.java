package com.example.helloworld.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import com.example.helloworld.db.pool.ObjectPool;
import com.example.helloworld.db.pool.ObjectPool.STORAGETYPE;
import com.example.helloworld.core.Category;
import com.example.helloworld.core.UpdatePositionBean;
import com.example.helloworld.dao.TreeDAO;

public class CategoriesDAOAdjacencyList implements TreeDAO {
	/*
	 * Default implementation of Tree DAO which uses AdjacencyList Technique to
	 * store the Hierarchical structure in the database
	 */

	// Static Constants
	public static final String DB_TABLENAME = "categories";
	private static final String CATEGORIES_INSERT = "INSERT INTO " + DB_TABLENAME
			+ " (name, displayname, description, parentid, siblingnumber )" + "VALUES (?, ?, ?, ?, ?)";
	private static final String CATEGORIES_UPDATE = "UPDATE " + DB_TABLENAME
			+ " set name = ?, displayname = ?, description = ?, parentid = ?, siblingnumber = ? " + " where id  = ?";
	private static final String CATEGORIES_DELETE = "DELETE FROM " + DB_TABLENAME + " where id  = ?";
	private static final String CATEGORIES_SELECT = " SELECT id, name, displayname, description, parentid, siblingnumber FROM "
			+ DB_TABLENAME;
	private static final String CATEGORIES_SELECT_FILTER = " where id = ?";
	private static final String CATEGORIES_SELECT_SUB_FILTER = " where parentid = ?";
	private static final String CATEGORIES_SELECT_IDEMPOTENCY = " SELECT count(*) FROM " + DB_TABLENAME
			+ " where parentid = ? and displayname = ?";
	private static final String CATEGORIES_SELECT_NOOFCHILDREN = " SELECT count(*) FROM " + DB_TABLENAME
			+ " where parentid = ?";
	private static final String CATEGORIES_ORDERBY_SIBLINGNUMBER = " ORDER BY siblingnumber ";

	// Member variables
	protected ObjectPool<Connection> connectionPool = null;
	protected Connection connection = null;
	private SQLStatement mCategoriesInsert = null;
	private SQLStatement mCategoriesUpdate = null;
	private SQLStatement mCategoriesDelete = null;
	private PreparedStatement mCategoriesSelectAll = null;
	private PreparedStatement mCategoriesSelect = null;
	private PreparedStatement mSubCategoriesSelect = null;
	private PreparedStatement mCategorySelectForIdempotency = null;
	private PreparedStatement mCategoryNoOfChildren = null;

	// Constructor
	public CategoriesDAOAdjacencyList(ObjectPool<Connection> connectionPool) throws SQLException {
		this.connectionPool = connectionPool;
		setConnectionAndStatements();
	}

	@Override
	public int createNode(Category category) throws SQLException {
		int returnValue = 0;
		mCategoriesInsert.setNString(1, toString(category.getName()));
		mCategoriesInsert.setNString(2, toString(category.getDisplayName()));
		mCategoriesInsert.setNString(3, toString(category.getDescription()));
		if (category.getParentId() < 0) // for root node insertion
			mCategoriesInsert.setNull(4, Types.INTEGER);
		else
			mCategoriesInsert.setInt(4, category.getParentId());
		mCategoriesInsert.setFloat(5, category.getSiblingNumber());
		try {
			returnValue = mCategoriesInsert.executeInsert();
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnValue;
	}

	@Override
	public int checkCategoryBeforeInsertionIdempotency(int parentId, String displayName) throws SQLException {
		int returnCount = 0;
		connectionPool.getCoreInvoker().setInt(mCategorySelectForIdempotency, 1, parentId);
		connectionPool.getCoreInvoker().setNString(mCategorySelectForIdempotency, 2, displayName);
		try {
			ResultSet rs = mCategorySelectForIdempotency.executeQuery();
			while (rs.next()) {
				returnCount = rs.getInt(1);
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnCount;
	}

	@Override
	public List<Category> getNode(final int id) throws SQLException {
		List<Category> returnList = new ArrayList<Category>();
		PreparedStatement pStatement;
		if (id < 0) {
			pStatement = mCategoriesSelectAll;
		} else {
			pStatement = mCategoriesSelect;
			pStatement.setInt(1, id);
		}
		try {
			ResultSet rs = pStatement.executeQuery();// retrieve the data
			while (rs.next()) {
				Category category = new Category();
				category.setId(rs.getInt("id"));
				category.setName(rs.getString("name"));
				category.setDisplayName(rs.getString("displayname"));
				category.setDescription(rs.getString("description"));
				category.setParentId(rs.getInt("parentid"));
				category.setSiblingNumber(rs.getFloat("siblingnumber"));
				returnList.add(category);
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnList;
	}

	@Override
	public List<Category> getChildren(final int parentId) throws SQLException {
		List<Category> returnList = new ArrayList<Category>();
		connectionPool.getCoreInvoker().setInt(mSubCategoriesSelect, 1, parentId);
		// mSubCategoriesSelect.setInt(1,parentId);
		try {
			ResultSet rs = mSubCategoriesSelect.executeQuery();// retrieve the
																// data
			while (rs.next()) {
				Category category = new Category();
				category.setId(rs.getInt("id"));
				category.setName(rs.getString("name"));
				category.setDisplayName(rs.getString("displayname"));
				category.setDescription(rs.getString("description"));
				category.setParentId(rs.getInt("parentid"));
				category.setSiblingNumber(rs.getFloat("siblingnumber"));
				returnList.add(category);
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnList;
	}

	@Override
	public int getSiblingCount(int parentId) throws SQLException {
		int returnCount = 0;
		connectionPool.getCoreInvoker().setInt(mCategoryNoOfChildren, 1, parentId);
		try {
			ResultSet rs = mCategoryNoOfChildren.executeQuery();// retrieve the
																// data
			while (rs.next()) {
				returnCount = rs.getInt(1);
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnCount;
	}

	@Override
	public int updatePosition(UpdatePositionBean updatePositionBeanObject) throws SQLException {
		if (updatePositionBeanObject.getSiblingNumbeNextNode() <= 0) {
			float newSiblingNumber = 1 + getSiblingCount(updatePositionBeanObject.getCategoryToUpdate().getParentId());
			// if the boundary case is arrived at ensure you increase the
			// boundary so that it doesnt end up with the same number
			if (updatePositionBeanObject.getSiblingNumberPreviousNode() >= newSiblingNumber)
				newSiblingNumber = updatePositionBeanObject.getSiblingNumberPreviousNode() + 1;
			updatePositionBeanObject.getCategoryToUpdate().setSiblingNumber(newSiblingNumber);
		} else {
			float newSiblingPositionValue = (updatePositionBeanObject.getSiblingNumberPreviousNode()
					+ updatePositionBeanObject.getSiblingNumbeNextNode()) / 2;
			updatePositionBeanObject.getCategoryToUpdate().setSiblingNumber(newSiblingPositionValue);
		}
		return updateNode(updatePositionBeanObject.getCategoryToUpdate());
	}

	@Override
	public int updateNode(Category category) throws SQLException {
		int returnValue = 0;
		mCategoriesUpdate.setNString(1, toString(category.getName()));
		mCategoriesUpdate.setNString(2, toString(category.getDisplayName()));
		mCategoriesUpdate.setNString(3, toString(category.getDescription()));
		mCategoriesUpdate.setInt(4, category.getParentId());
		// add as the latest sibling if the position is not specified
		// ideally the position should be specified as the average of the two
		// sibling between which this node is added
		if (category.getSiblingNumber() <= 0.0f)
			category.setSiblingNumber(getSiblingCount(category.getParentId()) + 1);
		mCategoriesUpdate.setFloat(5, category.getSiblingNumber());
		mCategoriesUpdate.setInt(6, category.getId());
		try {
			returnValue = mCategoriesUpdate.executeUpdate();
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnValue;
	}

	@Override
	public int deleteNode(final int id) throws SQLException {
		int returnValue = 0;
		mCategoriesDelete.setInt(1, id);
		try {
			returnValue = mCategoriesDelete.executeUpdate();
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return returnValue;
	}

	@Override
	public void close() throws SQLException {
		mCategoriesInsert.close();
		mCategoriesUpdate.close();
		mCategoriesDelete.close();
		mCategoriesSelectAll.close();
		mCategoriesSelect.close();
		mSubCategoriesSelect.close();
		mCategorySelectForIdempotency.close();
		mCategoryNoOfChildren.close();
	}

	@Override
	public void commit() throws SQLException {
		if (connectionPool.getSTORAGEType() == STORAGETYPE.SQLITE && connectionPool.isAutoCommit()) {
			System.out.println(
					"Generic: commit(): returning without commit as autocommit is true and is SQLite inmemory db");
			return;
		}
		if (connection != null) {
			try {
				System.out.println("Generic: commit(): DB connection commit");
				connection.commit();
			} catch (SQLException e) {
				System.out.println("Unable to commit transaction." + e);
				connectionPool.handleExceptionOnObjectInPool(connection, e);
				throw e;
			}
		}
	}

	protected void setConnectionAndStatements() throws SQLException {
		connection = this.getConnection();
		mCategoriesInsert = new SQLStatement(connection, CATEGORIES_INSERT, this.connectionPool.getCoreInvoker());
		mCategoriesUpdate = new SQLStatement(connection, CATEGORIES_UPDATE, this.connectionPool.getCoreInvoker());
		mCategoriesDelete = new SQLStatement(connection, CATEGORIES_DELETE, this.connectionPool.getCoreInvoker());
		mCategoriesSelectAll = connection.prepareStatement(CATEGORIES_SELECT + CATEGORIES_ORDERBY_SIBLINGNUMBER);
		mCategoriesSelect = connection
				.prepareStatement(CATEGORIES_SELECT + CATEGORIES_SELECT_FILTER + CATEGORIES_ORDERBY_SIBLINGNUMBER);
		mSubCategoriesSelect = connection
				.prepareStatement(CATEGORIES_SELECT + CATEGORIES_SELECT_SUB_FILTER + CATEGORIES_ORDERBY_SIBLINGNUMBER);
		mCategorySelectForIdempotency = connection.prepareStatement(CATEGORIES_SELECT_IDEMPOTENCY);
		mCategoryNoOfChildren = connection.prepareStatement(CATEGORIES_SELECT_NOOFCHILDREN);
	}

	protected Connection getConnection() {
		return connectionPool.checkOut();
	}

	protected void handleSQLException(SQLException e) throws SQLException {
		connectionPool.handleExceptionOnObjectInPool(connection, e);
		setConnectionAndStatements();
	}

	// Data conversion utilities
	protected String toInteger(final int intValue) {
		return toString(Integer.toString(intValue));
	}

	protected BigDecimal toBigDecimal(final long longValue) {
		return new BigDecimal(longValue);
	}

	protected int toInt(final boolean boolValue) {
		return boolValue ? 1 : 0;
	}

	protected String toString(String value) {
		return value == null || value.length() == 0 ? null : value;
	}

}
