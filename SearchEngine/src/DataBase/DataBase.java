package DataBase;

import java.util.concurrent.Semaphore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DataBase {
	private int readers = 0,writers = 0;
	private RandomAccessFile raf;
	private File file;
	private Semaphore Rmutex = new Semaphore(1);

	private Semaphore Wmutex = new Semaphore(1);

	private Semaphore db =  new Semaphore(1);

	private Semaphore readTry = new Semaphore(1);

	private Semaphore protect = new Semaphore(1);


	public void read(int x,int[] ans)
	{
		int numOfDB=x/1000;
		try {
			//			Read Write Algo 			///
			protect.acquire();
			readTry.acquire();
			Rmutex.acquire();
			readers++;
			if(readers==1)
				db.acquire();
			Rmutex.release();
			readTry.release();
			////// 			CS			////////////
			
			file=new File("DB "+numOfDB);
			raf=new RandomAccessFile(file, "r");
			
			raf.seek((x%1000)*12);
			raf.readInt(); //read x
			ans[0]=raf.readInt(); //read y
			ans[1]=raf.readInt(); //read z
			raf.close();
			
			////// 			CS			////////////

			Rmutex.acquire();
			readers--;
			if(readers==0)
				db.release();
			Rmutex.release();
			protect.release();
		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();

		}
	}

	public void write(int x,int y,int z)
	{
		int numOfDB=x/1000;
		try {
			//			Read Write Algo 			///
			Wmutex.acquire();
			writers++;
			if(writers==1)
				readTry.acquire();
			Wmutex.release();
			db.acquire();

			////// 			CS			////////////
			
			file=new File("DB "+numOfDB);
			raf=new RandomAccessFile(file, "rw");
			
			raf.seek((x%1000)*12); //point the location
			raf.writeInt(x); //write the x 
			raf.writeInt(y); //write the y
			raf.writeInt(z); //write the z
			raf.close();

			////// 			CS			////////////

			db.release();
			Wmutex.acquire();
			writers--;
			if(writers==0)
				readTry.release();
			Wmutex.release();

		} catch (InterruptedException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
