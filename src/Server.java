import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*;


public class Server {
    // Stores current clients
    public static ArrayList<ClientHandler> clientList = new ArrayList<>();
    // Sets id for processes
    static int counter = 0;

    public static void main(String[] args){
        try{
            // Listens on port 1234
            ServerSocket listener = new ServerSocket(1234);
            Socket s;

            while((s = listener.accept()) != null){
                System.out.println("New process has started: " + s);

                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                System.out.println("Creating a new handler for this process...");

                // Create a new client handler
                ClientHandler client = new ClientHandler(s,"p" + counter, in, out);
                Thread t = new Thread(client);
                clientList.add(client);
                t.start();
                counter++;
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }
}

class ClientHandler implements Runnable {

    Scanner sc = new Scanner(System.in);
    public String name;
    final ObjectInputStream in;
    final ObjectOutputStream out;
    Socket s;
    boolean isLoggedIn;

    public ClientHandler(Socket s, String name, ObjectInputStream in, ObjectOutputStream out){
        this.s = s;
        this.name = name;
        this.in = in;
        this.out = out;
        this.isLoggedIn = true;
    }

    @Override
	public void run() {
        String messageReceived;
        while(true){
            try {
                messageReceived = in.readUTF();
                System.out.println("Received: " + messageReceived + " from: " + name);

                if(messageReceived.equals("logout")){
                    this.isLoggedIn = false;
                    this.s.close();
                    break;
                }

                StringTokenizer st = new StringTokenizer(messageReceived, "#");
                String firstStr = st.nextToken();
                String recipient = "";
                String msgToSend = "";
                if(firstStr.equals("d")){ // d#px#msg
                    recipient = st.nextToken();
                    msgToSend = st.nextToken();
                    DelayHandler d = new DelayHandler(recipient, msgToSend);
                    Thread t = new Thread(d);
                    t.start();
                }
                else{
                    recipient = firstStr;
                    msgToSend = st.nextToken();
                    Boolean fin = false;

                    //OrderMessage om = new OrderMessage();

                    for (ClientHandler ch: Server.clientList){
                        if (ch.name.equals(recipient) && ch.isLoggedIn){
                            ch.out.writeUTF(this.name + ": " + msgToSend);
                            //ch.out.writeObject(om);
                            ch.out.flush();
                            fin = true;
                            break;
                        }
                    }

                    if(!fin){
                        out.writeUTF("Recipient not found.");
                        out.flush();
                    }
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        try{
            // Close streams
            this.in.close();
            this.out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

class DelayHandler implements Runnable {
    private String recepient;
    private String msgToSend;

    public DelayHandler(String recepient, String mr){
        this.recepient = recepient;
        msgToSend = mr;
    }

    @Override
	public void run() {

        try{
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        try {
            String recipient = this.recepient;
            String msgToSend = this.msgToSend;
            Boolean fin = false;

            for (ClientHandler ch: Server.clientList){
                if (ch.name.equals(recipient) && ch.isLoggedIn){
                    ch.out.writeUTF(this.recepient + ": " + msgToSend);
                    ch.out.flush();
                    fin = true;
                    break;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
