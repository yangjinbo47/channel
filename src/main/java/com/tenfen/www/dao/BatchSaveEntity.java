package com.tenfen.www.dao;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springside.modules.orm.hibernate.HibernateDao;

public class BatchSaveEntity<T> {
	
	private HibernateDao<T, Serializable> hibernateDao;
	private String batchThreadName = null;
	
	public BatchSaveEntity(HibernateDao<T, Serializable> hibernateDao, String batchThreadName) {
		this.hibernateDao = hibernateDao;
		this.batchThreadName = batchThreadName;
		thread = new Thread(new SaveTask(hibernateDao), batchThreadName);
        thread.start();
	}
	
	/** 缓存的更新数据 */
	private volatile LinkedList<T> entityList = new LinkedList<T>();

	/**
	 * 同步数据锁
	 */
	private ReentrantLock listLock = new ReentrantLock();

	/**
	 * 可执行锁，表示当前数据量可以批量更新到数据库
	 */
	private final Object empty = new Object();

	/**
	 * 满锁
	 */
	private final Object full = new Object();

	/**
	 * 所能处理的最大数量，超过这个数量需要等待
	 */
	private int cacheSize = 10000;

	/**
	 * 提交大小，每达到该大小则自动提交一次
	 */
	private int commitSize = 1000;

	/**
	 * 当检测到数据量不足以提交更新时，线程等待时间
	 */
	private long interval = 30 * 1000L;
	
	private Thread thread = null;
	
	public void initThread()
    {
        thread = new Thread(new SaveTask(hibernateDao), batchThreadName);
        thread.start();
    }

	//批量插入方法
	public void batchSaveEntity(T entity) {
		if (!thread.isAlive()) {
			initThread();
		}

		listLock.lock();
		int curSize = 0;
		try {
			curSize = entityList.size();
		} finally {
			listLock.unlock();
		}
		if (curSize >= cacheSize) {
			synchronized (full) {
				try {
					full.wait();
				} catch (InterruptedException e) {
					// 该异常无须处理
				}
			}
		}

		listLock.lock();
		try {
			entityList.add(entity);
			curSize = entityList.size();
		} finally {
			listLock.unlock();
		}

		if (curSize >= commitSize) {
			synchronized (empty) {
				empty.notifyAll();
			}
		}
	}

	/**
	 * 批量保存线程
	 * 
	 * @author BOBO
	 */
	protected class SaveTask implements Runnable {
		
		private HibernateDao<T, Serializable> hibernateDao;
		
		public SaveTask(HibernateDao<T, Serializable> hibernateDao) {
			this.hibernateDao = hibernateDao;
		}
		
		public void run() {
			while (true) {
				listLock.lock();
				int curSize = 0;
				try {
					curSize = entityList.size();
				} finally {
					listLock.unlock();
				}

				// 若缓冲列表中commitSize达到commitSize，或者间歇时间超过interval，则执行更新列表操作
				if (curSize < commitSize) {
					synchronized (empty) {
						try {
							empty.wait(interval);
						} catch (InterruptedException e) {
							// 该异常无须处理
						}
					}
				}

				// 将缓存中的数据放入更新list
				LinkedList<T> list = new LinkedList<T>();
				listLock.lock();
				try {
					for (int i = 0; i < commitSize; i++) {
						if (entityList.size() > 0) {
							list.add(entityList.removeFirst());
						} else {
							break;
						}
					}
				} finally {
					listLock.unlock();
				}

				if (list.size() > 0) {
					List<T> updateList = list;
					for (T entity : updateList) {
						hibernateDao.save(entity);
					}
				}

				// 唤醒满锁，使生产者可以继续
				synchronized (full) {
					full.notifyAll();
				}
			}
		}
	}
}
