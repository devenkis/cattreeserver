package com.example.helloworld.dao;

import java.sql.Connection;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.example.helloworld.core.Category;
import com.example.helloworld.db.CategoriesDAOAdjacencyList;
import com.example.helloworld.db.pool.JDBCConnectionPool;
import com.example.helloworld.db.pool.ObjectPool;

public class DAOHandler {

	public static int CATEGORYTREE_ROOTID = 0;
	private static DAOHandler sInstance = null;
	
	private ObjectPool<Connection> connectionPool = null;
	private TreeDAO mCategoriesTableHandler = null;
	
	/**
	 * Using a getInstance() method instead of a public constructor to only
	 * allow one instance of this object across the application.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static DAOHandler getInstance() throws Exception {
		if (sInstance == null) {
			synchronized (DAOHandler.class) {
				if (sInstance == null) {
					sInstance = new DAOHandler();
				}
			}
		}
		return sInstance;
	}
	
	public TreeDAO getCategoriesDAO() {
		return mCategoriesTableHandler;
	}
	
	public boolean resetAndReload() throws Exception {
		mCategoriesTableHandler.deleteAllChildren(CATEGORYTREE_ROOTID);
		loadInitialData(true);
		return true;
	}
	
	private DAOHandler() throws Exception {
		connectionPool = new JDBCConnectionPool(
				// "jdbc:sqlite:file:memdb1?mode=memory&cache=shared"
				/* &read_uncommitted=ON */
				"jdbc:sqlite:file::memory:?cache=shared&foreign_keys=ON", null, null, true);
		try {
			createStorageStructure();
		} catch (Exception e) {
			System.out.println("InMemoryTable creation exception: " + e);
		}
		setAllTableManagers();
		try {
			loadInitialData(false);
		} catch (Exception e) {
			System.out.println("Inital Data load exception: " + e);
		}
	}

	// Creates tables in InMemory db, reading table definitions from
	// dtftables.xml
	private void createStorageStructure() throws Exception {
		Connection inMemoryDbConnection = null;
		Statement stmt = null;
		try {
			inMemoryDbConnection = connectionPool.checkOut();
			stmt = inMemoryDbConnection.createStatement();
			// File fXmlFile = new File();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(DAOHandler.class.getClassLoader().getResourceAsStream("schema.xml"));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("inmemory");
			int tablescreated = 0;
			System.out.println("----------------------------");
			// TODO: Refactor these loops and decouple default from hardcoding
			for (int temp = 0; temp < nList.getLength(); temp++) {
				if (tablescreated==2)
					break;
				Element inmemory = (Element) nList.item(temp);
				NodeList dbObjects = inmemory.getElementsByTagName("dbobject");
				for (int i = 0; i < dbObjects.getLength(); ++i) {
					if (tablescreated==2)
						break;
					Element dbObject = (Element) dbObjects.item(i);
					System.out.println(dbObject.getAttribute("type") + " id : " + dbObject.getAttribute("id")
							+ " name: " + dbObject.getAttribute("name"));
					System.out.println("Stmt : " + dbObject.getTextContent());
					if (dbObject.getAttribute("name").toString().compareToIgnoreCase("adjacencylist") == 0) {
						stmt.executeUpdate(dbObject.getTextContent());
						tablescreated++;
					}
					if (dbObject.getAttribute("name").toString().compareToIgnoreCase("default") == 0) {
						stmt.executeUpdate(dbObject.getTextContent());
						tablescreated++;
					}
				}
			}
			if (!connectionPool.isAutoCommit()) {
				inMemoryDbConnection.commit();
			}
			System.out.println("----------TABLES CREATED------------------");
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionPool.checkIn(inMemoryDbConnection);
		}

	}

	private void loadInitialData(boolean avoidRoot) throws Exception {
		if(avoidRoot==false){
			CATEGORYTREE_ROOTID = mCategoriesTableHandler.createNode(new Category(0, "root", "root", "rootshouldneverbedeleted", -100, 1));
		}
		int electronicGoodsId = mCategoriesTableHandler.createNode(new Category(0, "Electronics", "Electronics", "All Electronic goods", CATEGORYTREE_ROOTID, 1));
			mCategoriesTableHandler.createNode(new Category(0, "Mobiles", "Mobiles", "All Mobiles Electronic goods", electronicGoodsId, 1));
			int phoneId = mCategoriesTableHandler.createNode(new Category(0, "Phones", "Phones", "Mobiles Phones", electronicGoodsId, 2));
				int accessoriesID = mCategoriesTableHandler.createNode(new Category(0, "Accessories", "Accessories", "All Mobile Phone accessories", phoneId, 1));
					mCategoriesTableHandler.createNode(new Category(0, "Chargers", "Chargers", "Chargers for mobiles", accessoriesID, 1));
					mCategoriesTableHandler.createNode(new Category(0, "Cases & Covers", "Cases and Covers", "Cases and Covers for mobiles", accessoriesID, 2));
					mCategoriesTableHandler.createNode(new Category(0, "Screen Protectors", "Screen Protectors", "Screen Protectors for mobiles", accessoriesID, 3));
					mCategoriesTableHandler.createNode(new Category(0, "Power Banks", "Power Banks", "Power Banks for mobiles", accessoriesID, 4));
					mCategoriesTableHandler.createNode(new Category(0, "Selfie Sticks", "Selfie Sticks", "Selfie Sticks for mobiles", accessoriesID, 5));
			mCategoriesTableHandler.createNode(new Category(0, "Cameras", "Cameras", "All types of Cameras", electronicGoodsId, 3));
			int computersID = mCategoriesTableHandler.createNode(new Category(0, "Computers", "Computers", "All Computers", electronicGoodsId, 4));
				mCategoriesTableHandler.createNode(new Category(0, "Business laptops", "Business laptops", "All Business laptops", computersID, 1));
				mCategoriesTableHandler.createNode(new Category(0, "Gaming laptops", "Gaming laptops", "All types of Gaming laptops", computersID, 2));
				mCategoriesTableHandler.createNode(new Category(0, "Desktops", "Desktops", "All types of Desktops electronic goods", computersID, 3));
			mCategoriesTableHandler.createNode(new Category(0, "Books", "Books", "All Bookss", CATEGORYTREE_ROOTID, 2));
			mCategoriesTableHandler.createNode(new Category(0, "Fashion", "Fashion", "All Fashion goods", CATEGORYTREE_ROOTID, 3));
			mCategoriesTableHandler.createNode(new Category(0, "Home & Furniture", "Home and Furniture", "All Home and Furniture goods", CATEGORYTREE_ROOTID, 4));
			mCategoriesTableHandler.createNode(new Category(0, "Appliances", "Appliances", "All Appliances", CATEGORYTREE_ROOTID, 5));
	}

	private void setAllTableManagers() throws Exception {
		mCategoriesTableHandler = new CategoriesDAOAdjacencyList(connectionPool);
	}
}
