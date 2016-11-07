package ClientServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class ClientMain {

	public static void main(String[] args){
		//read R1 and R2 from the file
		String str=null;
		File file=new File(args[0]);
		int R1=0,R2=0;	//read R1 and R2
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			str=br.readLine();
			br.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		StringTokenizer st = new StringTokenizer(str,",");
		R1=Integer.parseInt(st.nextToken());
		R2=Integer.parseInt(st.nextToken());
		new Client(R1,R2,args[0]);
	}
}
