package DataBase;

import java.util.concurrent.Semaphore;

import ThreadPool.ThreadPool;

public class CacheReader implements Runnable {

	private Cache cache;	//the cache
	private int[] answer;		//answer array to return
	private Semaphore pushed;	//answer semaphore to sign new answer pushed
	private int x,y,z;
	private ThreadPool updaters;	//helper threads to update the cache
	private boolean state;	//true= update, false= search
	
	public CacheReader(Cache cache,int[] answer,Semaphore pushed,int x,int y,int z,boolean state,ThreadPool updaters)
	{
		this.cache=cache;
		this.answer=answer;
		this.pushed=pushed;
		this.x=x;
		this.y=y;
		this.z=z;
		this.updaters=updaters;
		this.state=state;
	}
	
	//update the cache
	public void update(int x,int y,int z)
	{
		updaters.execute(new Runnable() { public void run() { 
			  try {
				cache.update(x, y, z);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}});
	}
	public void run()
	{
		if(!state){	//if false =search
			answer[0]=cache.search(x);
			pushed.release(); //release query pool's thread
		}
		else
		{
			update(x, y, z);
		}
	}
}
