package DataBase;

import java.util.concurrent.Semaphore;

public class PoolReader implements Runnable {

	private DataBase db; // Data Base for reading from
	private int x; // what to search
	private int[] answer; //answer to return (shared array)
	private Semaphore pushed; //make query thread to wait
	
	public PoolReader(DataBase db,int x,int[] answer,Semaphore pushed)
	{
		this.db=db;
		this.x=x;
		this.answer=answer;
		this.pushed=pushed;
	}
	
	@Override
	public void run() {
		db.read(x, answer);//read answer from DB
		pushed.release(); //release query pool's thread
	}



}
