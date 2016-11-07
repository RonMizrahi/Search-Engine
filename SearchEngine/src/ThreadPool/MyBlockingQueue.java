package ThreadPool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class  MyBlockingQueue<T>{
	private Lock lock;
	private Condition isFullCondition,isEmptyCondition;
	private int size;
	private Queue<T> q = new LinkedList<T>();

	public MyBlockingQueue(int size) {
		this.size = size;
		lock = new ReentrantLock();
		isFullCondition = lock.newCondition();
		isEmptyCondition = lock.newCondition();
	}

	public void put (T t) {
		lock.lock();
		try {
			while (isFull()) {
				try {
					isFullCondition.await();
				} catch (InterruptedException ex) {}
			}
			q.add(t);
			isEmptyCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public T get() {
		T t = null;
		lock.lock();
		try {
			while (isEmpty()) {
				try {
					isEmptyCondition.await();
				} catch (InterruptedException ex) {}
			}
			t = q.poll();
			isFullCondition.signalAll();
		} finally { 
			lock.unlock();
		}
		return t;
	}
	
    private boolean isEmpty() {
        return q.size() == 0;
    }
    private boolean isFull() {
        return q.size() == size;
    }
}