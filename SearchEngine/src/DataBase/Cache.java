package DataBase;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import ThreadPool.ThreadPool;

public class Cache {
	/**
	 * Node class holds X -Query, Y - answer , Z - how many time searched
	 */
	private class Node
	{
		public int x,y,z;
		public Node(int x,int y,int z)
		{
			this.x=x;
			this.y=y;
			this.z=z;
		}
	}
	private int M,C,minimum_Z=1,minimum_X=-1,NumOfUpdates=0;
	private LinkedHashMap<Integer, Node> CacheList;		// list that hold the future update to DB
	private LinkedHashMap<Integer, Node> FutureUpdateList;		// list that hold the future update to DB
	private Semaphore update,search;
	private Writer writerDB;
	
	public Cache(int M,int C,Writer writerDB)
	{
		this.M=M;	//Min times of obj to appear in cache
		this.C=C;	//Size of the cache
		CacheList=new LinkedHashMap<Integer, Node>();	//the cache list
		update=new Semaphore(1);	//hold the update CS
		search=new Semaphore(1);	//Hold the search CS
		this.writerDB=writerDB;		//writer thrad for writing cache removed nodes
	}
	
	/**
	 * 
	 * @param x what to search
	 * @return answer (y)
	 */
	public int search(int x)
	{
		try {
			search.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Node answer = CacheList.get(x);
		search.release();
		if(answer==null)		//no in CacheList , return min value
		{
			return 0;
		}
		else
		{
			answer.z++;		//update Z in list
			return answer.y;		//return answer
		}
	}
	
	/**
	 *  update the cache
	 * @param x query
	 * @param y answer
	 * @param z searched times
	 * @throws InterruptedException
	 */
	public void update(int x,int y, int z) throws InterruptedException
	{
		update.acquire();	//only 1 thread can update
		if(CacheList.size()==C)	//if cache is full
		{
			if(minimum_Z<z)
			{
				Node tempNode=FutureUpdateList.remove(minimum_X);	//remove the lowest searched in list
				writerDB.WhatToWrite(tempNode.x, tempNode.y, tempNode.z);	//update the DB with the removed from cache
				FutureUpdateList.put(x, new Node(x,y,z));	//put the new higher
				FindMinZ();	//find the new minimum Z in list
				NumOfUpdates++;
				if(NumOfUpdates>=(C/2))	//Update cache version
				{
					LinkedHashMap<Integer, Node> tempList=new LinkedHashMap<>(FutureUpdateList);
					search.acquire();	//shutdown cache O(1)
					CacheList=tempList;	//updated
					NumOfUpdates=0;
					search.release();	//Turn on the Cache
				}
			}
		}
		else	//Cache not full
		{
			CacheList.put(x, new Node(x,y,z));	//add a new node to list
			if(CacheList.size()==C)
			{
				FutureUpdateList=new LinkedHashMap<Integer, Node>(CacheList);	//if cache is full create new list for updates
				FindMinZ();
			}
		}
		update.release();
	}
	
	//get minimum searched number to get inside to cache
	public int GetM()
	{
		return M;
	}
	
	//find minimum Z in the list
	private void FindMinZ()
	{
		minimum_Z=Integer.MAX_VALUE;
		for(Entry<Integer,Node> entry : FutureUpdateList.entrySet())
		{
			Node temp=entry.getValue();
			if(temp.z<minimum_Z)
			{
				minimum_Z=temp.z;
				minimum_X=temp.x;
			}
		}
	}
}
