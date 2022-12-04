import java.io.Serializable;
 
public class OrderMessage implements Serializable {

    private int messageId; //message id
    private Object data; // could be anything
    private int[] timestamp; // vector clock
    private int[][] knownMessages; // number of messages sent between each process
    private int sender; // pid of sender
    private int receiver; // pid of receiver

    public OrderMessage(){
        messageId = -1;
        data = null;
        timestamp = new int[]{-1};
        knownMessages = new int[][]{{-1}};
        sender = -1;
        receiver = -1;
    }

    public OrderMessage(int mid, Object d, int[] ts, int[][] km, int s, int r){
        messageId = mid;
        data = d;
        timestamp = ts;
        knownMessages = km;
        sender = s;
        receiver = r;
    }

    public Object getData(){
        return data;
    }

    public int getReceiver() {
        return receiver;
    }

    public int getMessageId() {
        return messageId;
    }

    public int[] getTimestamp() {
        return timestamp;
    }

    public int[][] getKnownMessages() {
        return knownMessages;
    }

    public int getSender() {
        return sender;
    }
}
