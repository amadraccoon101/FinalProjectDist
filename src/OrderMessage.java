import java.io.Serializable;
 
public class OrderMessage implements Serializable {

    private int messageId; //message id
    private Object data; // could be anything
    private int[] timestamp; // vector clock
    private int[][] knownMessages; // number of messages sent between each process
    private int sender; // pid of sender
    private int receiver; // pid of receiver
    private int[] multicast; // pids of all messages that need to be multicasted to 
    private boolean deliverable; // marks a message as deliverable

    public OrderMessage(){
        messageId = -1;
        data = null;
        timestamp = new int[]{-1};
        knownMessages = new int[][]{{-1}};
        sender = -1;
        receiver = -1;
        multicast = new int[]{};
        deliverable = false;
    }

    public OrderMessage(int mid, Object d, int[] ts, int[][] km, int s, int r, int[] mc, boolean de){
        messageId = mid;
        data = d;
        timestamp = ts;
        knownMessages = km;
        sender = s;
        receiver = r;
        multicast = mc;
        deliverable = de;
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

    public int getMulticast() {
        return sender;
    }

    public boolean getDeliverable() {
        return deliverable;
    }
}
