import java.util.*;
import client.*;

public class TCP {
    private MyProtocol mainThread;
    private Map<Integer, Map<Integer, Message>> destToSeqToMes;
    private Map<Integer, Map<Integer, Timer>> destToSeqToTimer;
    private Map<Integer, Integer> seqToDest;
    private Map<Integer, Integer> destFails;
    private List<Integer> reSend;
    public boolean pinging = false;
    
    public TCP(MyProtocol m) {
        //create basic maps to fill, avoids nullpointer exceptions
        mainThread = m;
        destToSeqToMes = new HashMap<Integer, Map<Integer, Message>>();
        destToSeqToMes.put(0, new HashMap<Integer, Message>());
        destToSeqToMes.put(1, new HashMap<Integer, Message>());
        destToSeqToMes.put(2, new HashMap<Integer, Message>());
        destToSeqToMes.put(3, new HashMap<Integer, Message>());
        destToSeqToTimer = new HashMap<Integer, Map<Integer, Timer>>();
        destToSeqToTimer.put(0, new HashMap<Integer, Timer>());
        destToSeqToTimer.put(1, new HashMap<Integer, Timer>());
        destToSeqToTimer.put(2, new HashMap<Integer, Timer>());
        destToSeqToTimer.put(3, new HashMap<Integer, Timer>());
        seqToDest = new HashMap<Integer, Integer>();
        reSend = new ArrayList<Integer>();
        destFails = new HashMap<Integer, Integer>();
    }
    
    public synchronized void sendPacket(Message[] message) {
        while(true) {
            if (reSend.size() == 0) {
                if (message != null) {
                    for (int i = 0; i < message.length; i ++) {
                        int seq = (int) message[i].getData().array()[1];
                        String bits = JANSCII.fill(message[i].getData().array()[0]);
                        int dest = Integer.parseInt(bits.substring(2,4), 2);
                        //Save in outstanding array
                        destToSeqToMes.get(dest).put(seq, message[i]);
                        Timer t = new Timer(mainThread, mainThread.ACKTIME, seq, dest);
                        Thread timer = new Thread(t);
                        //save appropriate timer
                        destToSeqToTimer.get(dest).put(seq, t);
                        mainThread.mac(message[i]);
                        seqToDest.put(seq, dest);
                        timer.start();
                    }
                    break;
                } else {
                    System.out.println("error, nothing to resend");
                    break;
                }
            } else {
                //resending has priority
                int seq = reSend.get(0);
                System.out.println(seqToDest);
                int dest;
                if (seqToDest != null) {
			        dest = seqToDest.get(seq);
			    } else {
			        try{Thread.sleep(75);}catch(InterruptedException e) {}
			        dest = seqToDest.get(seq);
			    }
                Timer t = new Timer(mainThread, mainThread.ACKTIME, seq, dest);
                Thread timer = new Thread(t);
                //remove old timer
                if (destToSeqToTimer.get(dest)!= null) {
                    if ( destToSeqToTimer.get(dest).get(seq) != null) {
                        destToSeqToTimer.get(dest).get(seq).receiveAck();
                        destToSeqToTimer.get(dest).remove(seq);
                    } else {
                        System.out.println("Error");
                    }
                }
                
                //add new timer
                if (destFails.get(dest) == null) {
                    destFails.put(dest, 1);
                } else {
                    destFails.put(dest, destFails.get(dest)+1);
                }
                if (destFails.get(dest) > 7) {
                    //7 faulty acks, recreate routing table
                    pinging = true;
                    destFails.put(dest, 0);
                    mainThread.pingSkip();
                    System.out.println("Rerouting");
                    while(pinging) {
                        try{Thread.sleep(100);}catch(InterruptedException e) {}
                        System.out.print("/");
                    }
                    System.out.println("");
                    System.out.println("done rerouting");
                }
                System.out.println(pinging);
                if (mainThread.getRouting().keySet().contains(dest)) {
                    //still in range, just needed forwarding
                    destToSeqToTimer.get(dest).put(seq, t);
                    Message msg = mainThread.updateNexthop(destToSeqToMes.get(dest).get(seq), dest);
                    mainThread.mac(msg);
                    reSend.remove(0);
                    timer.start();
                    System.out.println("still in routingtable");
                } else {
                    //remove from all lists
                    //remove message
                    System.out.println("no longer in routingtable");
                    destToSeqToMes.get(dest).remove(seq);
                    reSend.remove(0);
                }
            }
        }   
    }
    
    public void receiveAck(Message message) {
        try {
            int ack = (int) message.getData().array()[2];
            String bits = JANSCII.fill(message.getData().array()[0]);

            int dest = Integer.parseInt(bits.substring(0,2), 2);
            //remove from all lists
            //stop and remove timer
            destToSeqToTimer.get(dest).get(ack).receiveAck();
            destToSeqToTimer.get(dest).remove(ack);
            //remove message
            destToSeqToMes.get(dest).remove(ack);
            //remove from reSend
            destFails.put(dest, 0);
            for (int i = 0; i < reSend.size(); i ++) {
                if (reSend.get(i) == ack) {
                    reSend.remove(i);
                    System.out.println("Ack received after timeout");
                    break;
                }
            }
            //remove from seqToDest
            seqToDest.remove(ack);
        } catch (NullPointerException e) {
            System.out.println("Acknowledgment array error");
        }
       
    }
    
    public synchronized void reSend(int seq) {
        reSend.add(seq);
    }
}
