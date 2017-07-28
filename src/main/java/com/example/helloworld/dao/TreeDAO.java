package com.example.helloworld.dao;

import java.sql.SQLException;
import java.util.List;

import com.example.helloworld.core.Category;
import com.example.helloworld.core.UpdatePositionBean;

public interface TreeDAO {
	public int createNode(Category category) throws Exception ;
	public int checkCategoryBeforeInsertionIdempotency(int parentId, String displayName) throws Exception;
	public List<Category> getNode(final int id) throws Exception;
	public List<Category> getChildren(final int parentId) throws Exception; 
	public int getSiblingCount(int parentId) throws SQLException ;
	public int updatePosition(UpdatePositionBean updatePositionBeanObject ) throws Exception;
	public int updateNode(Category category) throws Exception;
	public int deleteNode(final int id) throws Exception;
	public void close() throws Exception;
	public void commit() throws Exception;
}
