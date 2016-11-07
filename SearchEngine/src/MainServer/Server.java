package MainServer;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import DataBase.*;
import DataBase.Writer;
import ThreadPool.ThreadPool;

public class Server {
	private final int serverPort = 5859; // The port that this server is listening on
	private ArrayList<PrintWriter> SocketList; // Client soceket list
	private int S,C,M,L,Y;
	private DataBase db;	//the DataBase
	private ThreadPool QueryPool; // Query pool
	private ThreadPool readersPool; //Readers pool
	private Cache cache;	//cache
	private ThreadPool threadC,cache_updaters;		//cache reader/updaters
	private Writer writeDB;		//Writer Thrad for writing in DB
	private int id=0; 	//client id

	/**
	 * constructor of a server
	 * @param S number of allowed Search-threads
	 * @param C size of the cache
	 * @param M Min times of obj to appear in cache
	 * @param L range [1,L] for query
	 * @param Y num of readers from DataBase
	 */
	public Server(int S,int C,int M,int L,int Y)
	{
		this.S=S; //  number of allowed Search-threads
		this.C=C; // size of the cache
		this.M=M; // Min times of obj to appear in cache
		this.L=L; // range [1,L] for query
		this.Y=Y; // num of readers from DataBase
		SocketList = new ArrayList<PrintWriter>(); // clients' socket list
		QueryPool=new ThreadPool(S,S*100);		//the S Threads
		db=new DataBase();		//the db
		this.readersPool=new ThreadPool(Y, Y*100);		//the readers threads
		writeDB=new Writer(db,SocketList);	//writer Thread (writer to db)
		threadC=new ThreadPool(1, 10);	// Cache thread C (1 thread Pool)
		cache=new Cache(M,C,writeDB);	//the cache
		cache_updaters=new ThreadPool(2,20);	// Cache updaters pool
		writeDB.start();
		start();
	}

	/**
	 * server manage class, get new clients connections.
	 *
	 */
	public void start()
	{
		Thread Run= new Thread("Server"){
			@Override
			public void run() {
				try {
					@SuppressWarnings("resource")
					ServerSocket serverSock = new ServerSocket(serverPort); //server default port

					while (true) {
						Socket clientSocket = serverSock.accept(); // waiting for a new client
						PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());	 // open writer buffer to accepted client
						SocketList.add(writer); 	// add new client socket to list
						Thread listener = new Thread(new ClientManage(clientSocket, writer));	 // listen to a client input
						listener.start();
						System.out.println("<System> " + "Got a connection. \n");
					}
				} catch (Exception ex) {
					System.out.println("<System> " + "Error making a connection. \n");
				}
			}
		};Run.start();
		System.out.println("Server started\n");
	}

	/**
	 * Class that manage the client's input/output messages
	 */
	private class ClientManage implements Runnable {

		private BufferedReader readClient; 	// open reader buffer to a client
		private Socket socket;	 // socket of accepted client
		private PrintWriter writeClient;	 //open writer buffer to accepted client
		public ClientManage(Socket clientSocket, PrintWriter writer) { 	//constractor that gets client's info
			writeClient = writer;
			id++;
			try {
				socket = clientSocket; // client socket
				InputStreamReader isReader = new InputStreamReader(socket.getInputStream()); // new inputStreamc for reading
				readClient = new BufferedReader(isReader);
			} catch (Exception ex) {
				System.out.println("<System> " + "Unexpected error... \n");
			}

		}

		@Override
		public void run() {
			String message = null;

			try {
				while ((message = readClient.readLine()) != null) { //read message from the client
					if (message.contains("<get name>")) {  // if new client add socket and name to lists
						tellUser(""+id);
					} 
					else{
						//get new query
						AddNewDBFile(Integer.parseInt(message));	//make a new db file
						QueryPool.execute(new QueryThread(db,writeClient,Integer.parseInt(message),readersPool,L,cache,writeDB,threadC,cache_updaters));
					}
				}
			} catch (Exception ex) { // close all connection and remove user from server if can not listen to the user.
				closeClientConnection();
				SocketList.remove(writeClient);
			}
		}

		/**
		 * close all client connections
		 */
		private void closeClientConnection() {
			try {
				tellUser("You are disconnected");
				writeClient.close();
				socket.close();
				readClient.close();
			} catch (IOException ex) {
				System.out.println("can not close the connection with the client");
			}
		}

		/**
		 * Server sends private messages to a client.
		 *
		 * @param message get a message
		 */
		private void tellUser(String message) {
			writeClient.println(message);
			writeClient.flush();
		}
		
		/**
		 * 	open new file for reading writing
		 * @param x query
		 */
		private void AddNewDBFile(int x)
		{
			int numOfDB=x/1000;
			File file=new File("DB "+numOfDB);
			if(!file.exists()){
				try {
					RandomAccessFile raf=new RandomAccessFile(file, "rw");
					raf.seek(1000*14);
					raf.writeByte(0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
