package DataBase;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;
import ThreadPool.ThreadPool;

public class QueryThread implements Runnable{
	private Semaphore pushed; //make the thread to wait for pushed answer
	private DataBase db; // DataBase Source
	private PrintWriter writeClient; // client socket
	private int x,L; // The query to search , L - answer range [1,L]
	private Writer writeDB; // writer to db
	private ThreadPool ReadersPool; //Readers pool
	private int[] answer;//Stack for returned answers
	private Cache cache;	//the cache
	private ThreadPool threadC, cache_updaters;		//C Thread (1 thread pool) (cache manager) , cache updaters pool

	public QueryThread(DataBase db,PrintWriter writeClient, int searchX,ThreadPool ReadersPool,int L,Cache cache,Writer writedb,ThreadPool cReader,ThreadPool cache_updaters) {
		this.db=db;	
		this.writeClient=writeClient;
		this.x=searchX;
		this.ReadersPool=ReadersPool;
		answer = new int[2]; //arr[0]= y , arr[1] = z
		pushed  = new Semaphore(0);
		this.L=L; 
		this.cache=cache;
		this.writeDB=writedb;
		this.threadC=cReader;	//Cache manager
		this.cache_updaters=cache_updaters;
	}

	public void run() {
		int ans=0; //the answer to return

		try {

			/**------------- checks in cache (Case 1) -------------*/

			threadC.execute(new CacheReader(cache,answer,pushed,x,0,0,false,cache_updaters));	//search in cache (C Thread)
			pushed.acquire(); //standby till answer will return
			ans=answer[0]; //get answer

			/**-------------  checks in writer's update list (Case 2) -------------*/
			
			if(ans==0)	//if did not found in cache
				ans=writeDB.SearchAtWriteList(x);

			if(ans!=0)	//if answer was found in Case 1 or case 2
			{
				writeClient.println(ans);
				writeClient.flush();
			}

			/**------------- checks in DB (Case 3) -------------*/
			else	//Check in DB
			{
				ReadersPool.execute(new PoolReader(db,x,answer,pushed));
				pushed.acquire(); //standby till answer will return
				ans=answer[0]; //get answer

				if(ans!=0) // if answer was found (answer >=1 , see instructions)
				{
					answer[1]++;
					writeDB.WhatToWrite(x, answer[0], answer[1]);	// Update Z++
					if(answer[1]>=cache.GetM())	//notify when any z over M and can be candidate to cache
						threadC.execute(new CacheReader(cache,answer,pushed,x, answer[0], answer[1],true,cache_updaters));	//update cache
					writeClient.println(ans);	//write to client
					writeClient.flush();
				}
				else
				{
					int y=(int)(Math.random()*L)+1;	//random new Y for query
					writeDB.WhatToWrite(x, y, 1); // writer's updateList add new update node
					writeClient.println(y);
					writeClient.flush();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
