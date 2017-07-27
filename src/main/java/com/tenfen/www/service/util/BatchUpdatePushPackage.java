package com.tenfen.www.service.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.tenfen.entity.operation.pack.TOrder;
import com.tenfen.www.dao.operation.pack.OrderDao;

public class BatchUpdatePushPackage<T> {
	
	private OrderDao orderDao;
	private String batchThreadName;
	
	public BatchUpdatePushPackage(OrderDao orderDao, String batchThreadName) {
		this.orderDao = orderDao;
		this.batchThreadName = batchThreadName;
		thread = new Thread(new SaveTask(orderDao), batchThreadName);
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
	private int cacheSize = 1000;

	/**
	 * 提交大小，每达到该大小则自动提交一次
	 */
	private int commitSize = 300;

	/**
	 * 当检测到数据量不足以提交更新时，线程等待时间
	 */
	private long interval = 30 * 1000L;
	
	private Thread thread = null;
	
	public void initThread()
    {
        thread = new Thread(new SaveTask(orderDao), batchThreadName);
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
		
		private OrderDao orderDao;
		private Map<String, TOrder> updateMap = new HashMap<String, TOrder>();
		
		public SaveTask(OrderDao orderDao) {
			this.orderDao = orderDao;
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
					updateMap.clear();//清空map
					for (T entity : list) {
						TOrder tOrder = (TOrder)entity;
						updateMap.put(tOrder.getPhoneNum()+"_"+tOrder.getTradeId(), tOrder);
					}
					for (String key : updateMap.keySet()) {
						TOrder tOrder = updateMap.get(key);
						orderDao.save(tOrder);
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("pushPackageId", tOrder.getPushId());
						orderDao.batchExecute("update PushPackage set packageToday=packageToday+1 where id=:pushPackageId", map);
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
