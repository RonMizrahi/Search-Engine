package DataBase;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Writer extends Thread {
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
	private Semaphore List_Hold;
	private LinkedHashMap<Integer, Node> UpdateList;		// list that hold the future update to DB
	private Set<Entry<Integer, Node>> mapValues;	// For iterator of the list to get the first element
	private DataBase db; // the DB
	private ArrayList<PrintWriter> SocketList;		//clients list to know how many client in server
	
	/**
	 * constructor 
	 * @param db DataBase
	 */
	public Writer(DataBase db,ArrayList<PrintWriter> SocketList)
	{
		List_Hold=new Semaphore(1);
		this.db=db;
		UpdateList=new LinkedHashMap<Integer, Node>();
		mapValues = UpdateList.entrySet();
		this.SocketList=SocketList;	
	}

	/**
	 * 
	 * @param x The index to search
	 * @return answer, or integer min value if not found
	 */
	public int SearchAtWriteList(int x)
	{
		Node answer = UpdateList.get(x);
		if(answer==null)		//no in update list , return min value
			return 0;
		else
		{
			answer.z++;		//update Z in list
			return answer.y;		//return answer
		}

	}

	/**
	 * get the values to write
	 * @param x query
	 * @param y reply
	 * @param z num of time that searched
	 */
	public void WhatToWrite(int x,int y, int z) throws InterruptedException
	{
		List_Hold.acquire();	//Only one can update the list every time
		if(SearchAtWriteList(x)==0)	//if already in update list only do z++
			UpdateList.put(x, new Node(x,y,z));	//put new node in the list
		List_Hold.release();
	}

	public void run()
	{
		while(true)
		{
			try{
				int wait=SocketList.size()*10;	//wait num of client * 10
				Thread.sleep(wait);	//less clients = update at more frequency
				Node entry=mapValues.iterator().next().getValue();
				db.write(entry.x,entry.y,entry.z);	//Write to DataBase
				UpdateList.remove(mapValues.iterator().next().getKey());
			}catch(Exception e){}
		}
	}
}
