package ClientServer;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

import javax.swing.text.AbstractDocument.BranchElement;

public class Client {
	private PrintWriter out = null;    //socket output to server - for sending data through the socket to the server
	private BufferedReader in = null;  //socket input from server - for reading server's response
	private Socket socket = null; // socket to to conect the server
	private int R1,R2, queryX=0; // טווח המספרים
	private File file; //הקובץ שממנו קוראים את ההסתברויות
	private String filename,name; //file name , name of client (the ID)
	private int[] arr=new int[1000];
	private Semaphore WaitForServerReply;
	
	/**
	 * 
	 * @param R1 the integer numbers that specify the range [R1, R2]
	 * @param R2 the integer numbers that specify the range [R1, R2]
	 * @param filename file name containing
	 */
	public Client(int R1,int R2,String filename)
	{
		this.R1=R1;
		this.R2=R2;
		this.filename=filename;
		file=new File(this.filename);
		WaitForServerReply=new Semaphore(1);
		connect();
	}

	//Thread that listen to server's messages
	private void ListenThread() {
		Thread client_listener = new Thread("client_listener"){
			@Override
			public void run() {
				try {
					String server_reply = in.readLine();
					while (server_reply != null) {
						if (server_reply.equals("You are disconnected"))
						{
							closeClientConnection();
							break;
						}
						else
						{
							System.out.println("Client<"+name+"> : got reply "+server_reply+" for query "+queryX+"\n");
							WaitForServerReply.release();
						}
						server_reply = in.readLine();
					}
				} catch (IOException ex) {
					closeClientConnection();
					System.out.println("Connection is closed ==Errror==");
				}
			}
		};client_listener.start();
	}

	//Thread that sends info to server
	private void query_sender()
	{
		Thread client_sender= new Thread("client_sender"){
			@Override
			public void run() {
				int random;
				while(true)
				{
						try {
							WaitForServerReply.acquire();
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} //wait for reply from the server
					random=(int)(Math.random()*1000); //picks random number in arr range
					queryX=arr[random];
					System.out.println("Client<"+name+"> : sending "+ queryX);
					out.println(arr[random]);
					out.flush();
				}
			}
		};client_sender.start();
	}
	
	/**
	 * Close all clinet connction to the server
	 */
	private void closeClientConnection() {
		try {
			System.out.println("You are disconnected");
			socket.close();
			out.close();
			in.close();
			System.exit(0);
		} catch (IOException ex) {
			System.out.println("can not close the connection with the client");
		}
	}

	private void connect()
	{
		try {
			socket = new Socket("127.0.0.1", 5859);   //establish the socket connection between the client and the server
			out = new PrintWriter(socket.getOutputStream(), true);  //open a print stream on socket
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //open a BufferedReader on the socket
			out.println("<get name>");
			name=in.readLine();
			ListenThread(); // active listen tread for push messages
			query_sender();
			Read_X_toArr();
		} catch (Exception e) {
			System.out.println("Can not connect to server!\n");
			closeClientConnection();
		}
	}
	
	//make an array with the X to search
	private void Read_X_toArr() throws IOException
	{
		String str;
		int R1=0,R2=0,location=0; //location - next free cell in arr
		BufferedReader br =  new BufferedReader(new FileReader(file));
		while((str=br.readLine())!=null)
		{
			br.readLine();
			StringTokenizer st = new StringTokenizer(str,",");
			R1=Integer.parseInt(st.nextToken());
			R2=Integer.parseInt(st.nextToken());
			while (st.hasMoreTokens()) {
				int num=(int)(Double.parseDouble(st.nextToken())*1000);
				for(int i=0;i<num;i++)
				{
					arr[location]=R1;
					location++;
				}
				R1++;
			}  
		}
		br.close();
	}
}
