import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
    final static int mSize = 10;
    final static int ServerPort = 1234;
    static int[][] M = new int[mSize][mSize];
    static int id;
    static int msgCounter = 0;
    static final Object lock = new Object();
    static ArrayList<OrderMessage> waitQueue = new ArrayList<OrderMessage>();

    // Server needs to tell client that a new client has been added so it can add a new arraylist
    // It also needs to know how many clients there are when it is instaniated

    public static void main(String[] args) throws UnknownHostException, IOException{
        Scanner sc = new Scanner(System.in);
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip, ServerPort);

        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());

        //Read process ID from server
        id = in.readInt();

        System.out.println("The format for sending messages is: px#message");
        System.out.println("Add d (i.e. d#px#message) for a delayed message");
        System.out.println("Type logout to exit");
        System.out.println("You are process: p" +id);

        Thread sendMesssage = new Thread(new Runnable() {
            @Override
            public void run(){
                String message = "";
                while(!message.equals("logout")){
                    message = sc.nextLine();

                    //M[i,j] = M[i,j] + 1;
                    StringTokenizer st = new StringTokenizer(message, "#");
                    String firstStr = st.nextToken();
                    String recipient = "";
                    if(firstStr.equals("d")){ // d#px#msg
                        recipient = st.nextToken();
                    }
                    else{
                        recipient = firstStr;
                    }

                    int j = -1;
                    if (!recipient.equals("logout")){
                        j = Integer.parseInt(recipient.substring(1,2));
                        synchronized (lock) {
                            M[j][id] = M[j][id] + 1;
                        }
                    }

                    try {
                        // Send M with the message using OrderMessage
                        int messageId = msgCounter; // message id
                        Object data = message; // could be anything
                        int[] timestamp = new int[]{-1}; // vector clock
                        int[][] knownMessages = new int[mSize][mSize];
                        for(int a = 0; a < mSize; a++){
                            for(int b = 0; b < mSize; b++){
                                knownMessages[a][b] = M[a][b];
                            }
                        }
                        int sender = id; // pid of sender
                        int receiver = j; //pid of receiver
                        int[] multicast = new int[]{};

                        OrderMessage om = new OrderMessage(messageId, data, timestamp,
                                            knownMessages, sender, receiver, multicast);

                        // Increment message counter                    
                        msgCounter++;

                        //out.writeUTF(message);
                        out.writeObject(om);
                        out.flush();
                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    if(firstStr.equals("logout")){
                        break;
                    }
                }

                try {
                    in.close();
                    out.close();
                    s.close();
                    sc.close();
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
                        //String message = in.readUTF();
                        Object rm = in.readObject();
                        OrderMessage receivedMessage = (OrderMessage) rm;

                        // Wait in loop until we can receive messages
                        // while(!canReceive(receivedMessage)){}
                        if(!canReceive(receivedMessage)){
                            //System.out.println("Placing it into wait queue");
                            waitQueue.add(receivedMessage);
                        }
                        else{
                            String message = (String) receivedMessage.getData();
                            StringTokenizer st = new StringTokenizer(message, "#");
                            String firstStr = st.nextToken();
                            String recipient = "";
                            String msgSent = "";
                            if(firstStr.equals("d")){ // d#px#msg
                                recipient = st.nextToken();
                            }
                            else{
                                recipient = firstStr;
                            }
                            msgSent = st.nextToken();

                            System.out.println("p" + receivedMessage.getSender() + ": " + msgSent);
                            int[][] newM = receivedMessage.getKnownMessages();

                            for(int i = 0; i < M.length; i++){
                                for(int j = 0; j < M[i].length; j++){
                                    M[i][j] = Math.max(newM[i][j], M[i][j]);
                                }
                            }
                            ArrayList <OrderMessage> waitQueueCopy = new ArrayList<OrderMessage>(waitQueue);
                            for(OrderMessage w: waitQueueCopy){
                                if(canReceive(w)){
                                    newM = w.getKnownMessages();
                                    for(int i = 0; i < M.length; i++){
                                        for(int j = 0; j < M[i].length; j++){
                                            M[i][j] = Math.max(newM[i][j], M[i][j]);
                                        }
                                    }
                                    waitQueue.remove(w);

                                    message = (String) w.getData();
                                    st = new StringTokenizer(message, "#");
                                    firstStr = st.nextToken();
                                    recipient = "";
                                    msgSent = "";

                                    if(firstStr.equals("d")){ // d#px#msg
                                        recipient = st.nextToken();
                                    }
                                    else{
                                        recipient = firstStr;
                                    }
                                    msgSent = st.nextToken();

                                    System.out.println("p" + w.getSender() + ": " + msgSent);
                                }
                            }
                            }
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }

            public boolean canReceive(OrderMessage om){
                int i = id;
                int j = om.getSender();
                int[][] W = om.getKnownMessages();

                //System.out.println(W[i][j]);
                //System.out.println(M[i][j]);
                // Check first condition
                if(W[i][j] > M[i][j] + 1){
                    return false;
                }
                
                // Check second condition
                for(int k = 0; k < mSize; k++){
                    if(k != j  && (M[k][i] < W[k][i])){
                        return false;
                    }
                }

                return true;
            }
        });

        sendMesssage.start();
        readMessage.start();
    }
}
