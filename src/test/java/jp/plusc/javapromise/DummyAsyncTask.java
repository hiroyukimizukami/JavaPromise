package jp.plusc.javapromise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DummyAsyncTask {
	private static final Logger logger = LoggerFactory.getLogger(PromiseTest.class);
	public static final String URL = "url";
	public static final String PARAMS = "params";
	public static final String DELAYED = "delayed";
	private static final List<Integer> DELAY;

	private final ScheduledExecutorService pool;
	private final int proccessCount;
	private final ArrayList<ScheduledFuture<?>> schedule;
	private final Vector<Throwable> throwable;

	static {
		DELAY = new ArrayList<Integer>();
		DELAY.add(10);
		DELAY.add(20);
		DELAY.add(30);
		DELAY.add(40);
		DELAY.add(50);
	}

	public DummyAsyncTask(final int proccessCount) {
		pool = Executors.newScheduledThreadPool(3);
		schedule = new ArrayList<ScheduledFuture<?>>();
		throwable = new Vector<Throwable>();
		this.proccessCount = proccessCount;
	}

	public synchronized void connectAsync(
			String url, Map<String, Object> params, final Callbackable c) {

		Random r = new Random();
		int index = r.nextInt(DELAY.size() -1);
		final int delay = DELAY.get(index);

		final Map<String, String> result = new HashMap<String, String>();
		result.put(URL, url);
		result.put(PARAMS, String.valueOf(params));
		result.put(DELAYED, String.valueOf(delay));

		final Runnable task = new Runnable() {

			public void run() {
				try {
					c.call(result);
				} catch (Throwable t) {
					throwable.add(t);
				}
			}
		};
		schedule.add(pool.schedule(task, delay, TimeUnit.MILLISECONDS));
		if (schedule.size() >= proccessCount) {
			pool.shutdown();
		}
	}

	public synchronized List<Throwable> getThrowables() {
		if (throwable.isEmpty()) {
			return new ArrayList<Throwable>();
		}
		return throwable.subList(0, throwable.size());
	}

	public void waitTask() {
		while(true) {
			if (isDoneAllTask()) {
				break;
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.info(String.valueOf(e));
				}
			}
		}
	}

	private synchronized boolean isDoneAllTask() {
		if (schedule.size() < proccessCount) {
			return false;
		}

		for (ScheduledFuture<?> f : schedule) {
			if (!(f.isDone() || f.isCancelled())) {
				return false;
			}
		}
		return true;
	}

	public static abstract class Callbackable {
		public void call(Map<String, String> result) {
			impl(result);
		}

		protected abstract void impl(Map<String, String> result);
	}

}
