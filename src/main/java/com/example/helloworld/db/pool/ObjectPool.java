package com.example.helloworld.db.pool;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.Hashtable;
import com.example.helloworld.db.sql.GenericJDBCInvoker;

/**
 * Generic ObjectPool which should be implemented for respective type of object
 * pool like DBConnection pool
 * 
 * @author vthippan
 *
 * @param <T>
 */
public abstract class ObjectPool<T> {
	public static enum STORAGETYPE{
		POSTGRES, SQLITE, ORACLE, MSSQL, SAPHANA
	};
	
	private long expirationTime;

	private int maxPoolSize;

	private Hashtable<T, Long> locked, unlocked;
	
	public ObjectPool() {
		expirationTime = 30000; // 30 seconds: Make it Configurable
		locked = new Hashtable<T, Long>();
		unlocked = new Hashtable<T, Long>();
		maxPoolSize = 100; // 100 connections max: Make it Configurable
	}

	protected abstract T create();

	public abstract void handleExceptionOnObjectInPool(T o, Exception e);

	public abstract boolean validate(T o);

	public abstract void expire(T o);

	public abstract boolean isAutoCommit();
	
	public abstract GenericJDBCInvoker getCoreInvoker();
	
	public abstract STORAGETYPE getSTORAGEType();
	
	public synchronized T checkOut() {
		System.out.println("ObjectPool: before checkout(): unlocked.size():" + unlocked.size() + " ;locked.size():" + locked.size());
		long now = System.currentTimeMillis();
		T t;
		if (unlocked.size() > 0) {
			Enumeration<T> e = unlocked.keys();
			while (e.hasMoreElements()) {
				t = e.nextElement();
				if ((now - unlocked.get(t)) > expirationTime) {
					// object has expired
					unlocked.remove(t);
					expire(t);
					t = null;
				} else {
					if (validate(t)) {
						unlocked.remove(t);
						locked.put(t, now);
						return (t);
					} else {
						// object failed validation
						unlocked.remove(t);
						expire(t);
						t = null;
					}
				}
			}
		}
		// no objects available, create a new one
		t = create();
		locked.put(t, now);
		System.out.println("ObjectPool: after checkout(): unlocked.size():" + unlocked.size() + " ;locked.size():" + locked.size());
		return (t);
	}

	public synchronized void checkIn(T t) {
		System.out.println("ObjectPool: before checkIn(): unlocked.size():" + unlocked.size() + " ;locked.size():" + locked.size());
		locked.remove(t);
		unlocked.put(t, System.currentTimeMillis());
		System.out.println("ObjectPool: after checkIn(): unlocked.size():" + unlocked.size() + " ;locked.size():" + locked.size());
	}

	protected int getAvailablePool() {
		return maxPoolSize - locked.size();
	}

	protected int getMaxPoolSize() {
		return maxPoolSize;
	}
	
	protected int getCurrentPoolSize() {
		return locked.size() + unlocked.size();
	}
}