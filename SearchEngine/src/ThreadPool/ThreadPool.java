package ThreadPool;

import java.util.ArrayList;



public class ThreadPool {

	ArrayList<PoolThread> Threadslist;
	MyBlockingQueue TaskQueue;
	int Num_of_tasks,Num_of_threads;
	boolean isShutdown=false;
	public ThreadPool(int Num_of_threads,int Num_of_tasks)
	{
		Threadslist=new ArrayList<PoolThread>(Num_of_threads);
		this.Num_of_threads=Num_of_threads;
		this.Num_of_tasks=Num_of_tasks;
		TaskQueue = new MyBlockingQueue(Num_of_tasks);
		for(int i=0;i<Num_of_threads;i++)
		{
			Threadslist.add(new PoolThread(TaskQueue));
		}
		for(PoolThread thread:Threadslist)
			thread.start();
	}
	
	public void execute(Runnable task) {
		// TODO Auto-generated method stub
		if(this.isShutdown()) throw new IllegalStateException("threapool is stopped - can not execute new tasks");
		TaskQueue.put(task); //לשקול TRY CATCH
	}

	public boolean isShutdown() {
		// TODO Auto-generated method stub
		return isShutdown;
	}

	
	public void shutdown() {
		// TODO Auto-generated method stub
		isShutdown=true;
		for(PoolThread thread:Threadslist)
			thread.StopThread();
	}
	
}
