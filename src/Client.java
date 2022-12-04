import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
    final static int ServerPort = 1234;
    static List<List<Integer>> M = new ArrayList<List<Integer>>();

    // Server needs to tell client that a new client has been added so it can add a new arraylist
    // It also needs to know how many clients there are currently at the beginning

    public static void main(String[] args) throws UnknownHostException, IOException{
        Scanner sc = new Scanner(System.in);
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip, ServerPort);

        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());

        Thread sendMesssage = new Thread(new Runnable() {
            @Override
            public void run(){
                String message = "";
                while(!message.equals("logout")){
                    message = sc.nextLine();
                    try {
                        out.writeUTF(message);
                        out.flush();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                try {
                    in.close();
                    out.close();
                    s.close();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String message = in.readUTF();
                        System.out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendMesssage.start();
        readMessage.start();
    }
}
