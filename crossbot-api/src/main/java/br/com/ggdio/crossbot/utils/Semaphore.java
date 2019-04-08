package br.com.ggdio.crossbot.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent semaphore thread locker
 * 
 * @author Guilherme Dio
 *
 */
public class Semaphore {

	private final ReentrantLock lock = new ReentrantLock();
	
	private CountDownLatch latch;
	
	public Semaphore() {
		this(true);
	}
	
	public Semaphore(boolean green) {
		if(green) {
			setGreenSignal();
		} else {
			setRedSignal();
		}
	}
	
	private CountDownLatch getLatch() {
		try {
			lock.lock();
			return latch;
			
		} finally {
			lock.unlock();
			
		}
	}
	
	/**
	 * Proceed the signal.
	 * <p>If it's red, then the current thread is locked until it turns green.</p>
	 * <p>If it's green, then the current thread is allowed to proceed and continue it's execution.</p>
	 * 
	 */
	public void proceed() {
		try {
			getLatch().await();
			
		} catch (InterruptedException e) { }
	}
	
	/**
	 * Releases the semaphore
	 */
	public void setGreenSignal() {
		lock.lock();
		if(latch != null) {
			latch.countDown();
		}
		latch = new CountDownLatch(0);
		lock.unlock();
	}
	
	/**
	 * Blocks the semaphore
	 */
	public void setRedSignal() {
		lock.lock();
		latch = new CountDownLatch(1);
		lock.unlock();
	}
	
}
