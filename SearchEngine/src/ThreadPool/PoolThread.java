package ThreadPool;


public class PoolThread extends Thread {
	private MyBlockingQueue TaskQueue;
	private boolean isAlive=true;

	public PoolThread(MyBlockingQueue TaskQueue)
	{
		this.TaskQueue = TaskQueue;
	}

	public void run()
	{
		while(isAlive)
		{
			try{
				Runnable r=(Runnable)TaskQueue.get();
				r.run();
			}
			catch (Exception e){
				System.err.println(e);
			}
		}
	}
	public void StopThread()
	{
		isAlive=false;
		this.interrupt();
	}

}
