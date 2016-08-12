package pentos.sim;

import java.util.concurrent.*;
import java.lang.management.*;

class Timer extends Thread {

    private boolean start = false;
    private boolean finished = false;
    private Callable <?> task = null;
    private Exception exception = null;
    private Object result = null;

    private static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public long time()
    {
	return isAlive() ? bean.getThreadCpuTime(getId()) : 0;
    }

    public <T> T call(Callable <T> task, long timeout_ms) throws Exception
    {
	if (!isAlive()) throw new IllegalStateException();
	if (task == null) throw new NullPointerException();
	this.task = task;
	synchronized (this) {
	    start = true;
	    notify();
	}
	if (timeout_ms > 0) {
	    long epoch_ns = time();
	    long running_ms = 0;
	    do {
		synchronized (this) {
		    if (finished) break;
		    try {
			wait(timeout_ms - running_ms);
		    } catch (InterruptedException e) {}
		}
		running_ms = (time() - epoch_ns) / 1000000;
	    } while (running_ms < timeout_ms);
	} else synchronized (this) {
		while (finished == false) try {
			wait();
		    } catch (InterruptedException e) {}
	    }
	if (finished == false)
	    throw new TimeoutException();
	finished = false;
	if (exception != null) throw exception;
	@SuppressWarnings("unchecked")
	    T result_T = (T) result;
	return result_T;
    }

    public void run()
    {
	for (;;) {
	    synchronized (this) {
		while (start == false) try {
			wait();
		    } catch (InterruptedException e) {}
	    }
	    start = false;
	    exception = null;
	    try {
		result = task.call();
	    } catch (Exception e) {
		exception = e;
	    }
	    synchronized (this) {
		finished = true;
		notify();
	    }
	}
    }
}
