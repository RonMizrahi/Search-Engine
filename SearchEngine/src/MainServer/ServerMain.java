package MainServer;


public class ServerMain {

	public static void main(String[] args) {
		int[] arr=new int[args.length];		//convert args to int
		for(int i=0;i<arr.length;i++)
		{
			arr[i]=Integer.parseInt(args[i]);
		}
		new Server(arr[0],arr[1],arr[2],arr[3],arr[4]);		//run server
	}

}
