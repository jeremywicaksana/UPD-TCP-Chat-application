import client.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;
//created by:
//-Christophorus Jeremy Wicaksana, s2096862

public class MyProtocol{

    // The host to connect to. Set this to localhost when using the audio interface tool.
    private static String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
    // The port to connect to. 8954 for the simulation server.
    private static int SERVER_PORT = 8954;
    // The frequency to use.
    private static int frequency = 20505;
    //global variables : name E{0-3}
    private int name;
    private Map<Integer, Integer> Routing; //maps destination to nexthop
    private Map<Integer, Integer> RoutHops;//maps destination to #Hops  (for optimization, only used while pinging)
    //chat input reader
    private BufferedReader reader;
    //locks for Ping thread
    public boolean lockIt = false;
    private PingThread pingthread;
    //TCP doesnt have its own thread (gets called by handlingThread), this is just an easy name
    public TCP tcpThread;
    //ack timeout, ACKTIME*50/1000 seconds
    public static final int ACKTIME = 150;
    public OurGui UI;

    private BlockingQueue<Message> receivedQueue;
    private BlockingQueue<Message> sendingQueue;
    
    private static MessageType lineStatus = MessageType.FREE;// to let other nodes know that a line is either BUSY or FREE;

    //initialization
    public MyProtocol(String server_ip, int server_port, int frequency){
        receivedQueue = new LinkedBlockingQueue<Message>();
        sendingQueue = new LinkedBlockingQueue<Message>();
        new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue); // Give the client the Queues to use
        new receiveThread(receivedQueue, this).start(); // Start thread to handle received messages!
        //start pingthread (timeout)
        pingthread = new PingThread(this);
        pingthread.start();
        reader = new BufferedReader(new InputStreamReader(System.in));
        
        tcpThread = new TCP(this);
        UI = new OurGui(this);
        UI.setVisible(true);
        
    }
       
    private void resetTable() {
        Routing = new HashMap<Integer,Integer>();
        Routing.put(this.name, this.name);

        RoutHops = new HashMap<Integer, Integer>();
        RoutHops.put(this.name, 0);
        
    }
    
    public Map<Integer, Integer> getRouting() {
        return Routing;
    }
    
    public int getName() {
        return name;
    }
    
    public void setName(int Vname) {
        name = Vname;
        Routing = new HashMap<Integer,Integer>();
        RoutHops = new HashMap<Integer,Integer>();
        Routing.put(Vname,Vname);
        RoutHops.put(Vname,0);
    }

//=======\\ MAC part //========\\ 
    public void mac(Message msg) {
    	while(true) {
    		try {
	        	if (new Random().nextInt(100) < 70 && lineStatus == MessageType.FREE) {
	        	    //check twice if line is free
	        		Thread.sleep(new Random().nextInt(8)*7);
	        		if (lineStatus == MessageType.FREE) {
	            		sendingQueue.put(msg);
	            		break;
	        		}	
	        	} 
	            Thread.sleep(50);
	    	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(2);
	    	} 
		}
    }
//=======\\ Forwarding //=======\\ 
    public int Forwarding(int dest) {
    	return Routing.get(dest);
	}

//=======\\ Reading //=======\\ 
    private void read() {
        String z = "";
        while(true) {
            try {
                z = reader.readLine();
            } catch (IOException e) {
            
            }
            //lock thread: it will save the data, block the input, and wait till pinging is done
            synchronized(this) {
                while (lockIt) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("Thread error");
                    }
                }
            }
            //make sure message is no longer than 10 frames (16 chars per frame)
            if (z.length() > 160) {
                z = z.substring(0,160);
            }
            //check message for commands
            int dest; //0-3 for destination, 99 for broadcast
            if (z.split(" ")[0].length() == 2) {
                int p;
                try {
                    p = Integer.parseInt(z.substring(1,2));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid command: please insert valid destination");
                    continue;
                }
                if (p == name) {
                    System.out.println("you sent this to yourself: \r\n" + z.substring(3));
                    continue;
                } else if (p < 4 && p >= 0) {
                    if (!Routing.keySet().contains(p)) {
                        System.out.println("unknown chat partner");
                    } else if (z.length() > 3) {
                        //message is long enough
                        //remove command
                        sendToTCP(p, z.substring(3));
                    } else {
                        System.out.println("write a message please");
                    }
                    
                } else {
                    System.out.println("Invalid command: please insert valid destination");
                    continue;
                }
            } else if (z.split(" ")[0].equals("/ALL") && z.length() >= 5) {
                //destination is everyone except himself
                for (Integer key:Routing.keySet()) {
                    if (key != name) {
                        sendToTCP(key, z.substring(5));
                    }
                } 
            } else if (z.split(" ")[0].equals("/EXIT")) {
                mac(new Message(MessageType.END));
                System.out.println("END");
            } else {
                System.out.println("didnt specify command: /1-3, /ALL, /EXIT");
                continue;
            }

            
            //message is a stringarray with data in frame-size chunks, 
            //needs to be encoded, then send through TCP to MAC
            
        }
    }
//=======\\ Create TCP dataframe //=======\\ 
    public byte[] TCPHeader(int src, int dest, String flags, int syn, int ack, int checksum, boolean first) {
        byte[] temp = new byte[4];
        String[] a = new String[]{Integer.toBinaryString(src), Integer.toBinaryString(dest), Integer.toBinaryString(Routing.get(dest)), 
                                    Integer.toBinaryString(syn), Integer.toBinaryString(ack),Integer.toBinaryString(checksum)};
        //src,dest,nexthop,00
        for (int i = 0; i < 3; i ++) {
            if (a[i].length() == 1) {
                a[i] = "0" + a[i];
            }
        }
        for (int i = 0; i < 6; i ++) {
            if (a[i].length() > 8) {
                a[i] = a[i].substring(a[i].length()-8);
            }
        }
        while (a[5].length() < 8) {
            a[5] = "0"+a[5];
        }
        a[5] = a[5].substring(2);
        if (first) { //append first knowledge
            a[5] += "01";
        } else {
            a[5] += "00";
        }
        String result = a[0]+a[1]+a[2]+flags;
        temp[0] = (byte) Integer.parseInt(result,2);
        //syn
        int len = 8-a[3].length();
        for (int i = 0; i < len; i ++) {
            a[3] = "0" + a[3];
        }
        temp[1] = (byte) Integer.parseInt(a[3],2);
        //ack
        len = 8-a[4].length();
        for (int i = 0; i < len; i ++) {
            a[4] = "0" + a[4];
        }
        temp[2] = (byte) Integer.parseInt(a[4],2);
        //checksum (6 bits)
        temp[3] = (byte) Integer.parseInt(a[5],2);
        return temp;
    }
    
    //create body of tcp frame
    public void sendToTCP(int dest, String z) {
        String[] message = new String[(z.length()-1)/16+1]; //integer division, rest value needs one last packet
        
        int i = 0;
        while (z.length() > 16) {
            message[i] = z.substring(0,16);
            z = z.substring(16);
            i += 1;
        }
        //overwrites last packet with last packet, but if packet amount is 1, it skips above loop
        message[i] = z;
        
        byte[][] result = new byte[message.length][16];
        int seq = new Random().nextInt(255-message.length-1);
        for (i = 0; i < message.length; i ++) {
            byte[] body = JANSCII.encode(message[i]);
            int[] checksum = new int[12];
            seq += 1;
            //save message in integer form
            for (int y = 0; y < message.length; y ++) {
                checksum[y] = (int) body[y];
            }
            //save flag
            String flag;
            boolean first = false;
            if (i == 0) {
                //first message
                flag = "00";
                first = true;
            }
            if (i == message.length-1) {
                //Last message
                flag = "10";
            } else {
                flag = "00";
            }
            //create header
            byte[] header = TCPHeader(name,dest,flag,seq,0,Checksum.calcChecksum(checksum), first);
            for (int y = 0; y < 12; y ++) {
                result[i][4+y] = body[y];
            }
            for (int y = 0; y < 4; y ++) {
                result[i][y] = header[y];
            }
            //copied Header and Body into one byte array result[i]
        }
        Message[] msg = new Message[message.length];
        for (int y = 0; y < message.length; y ++) {
            ByteBuffer buf = ByteBuffer.allocate(16);
            buf.put(result[y]);
            msg[y] = new Message(MessageType.DATA, buf);
        }
        //send Message[] to tcp output port
        /*tcp(msg);*/
        tcpThread.sendPacket(msg);
    }
    
    public Message updateNexthop(Message message, int dest) {
        byte[] temp = message.getData().array();
        String bits = JANSCII.fill(temp[0]);
        String nexthop = Integer.toBinaryString(Routing.get(dest));
        if (nexthop.length() == 1) {
            nexthop = "0" + nexthop;
        }
        bits = bits.substring(0,4) + nexthop + bits.substring(6);
        temp[0] = (byte) Integer.parseInt(bits, 2);
        ByteBuffer b = ByteBuffer.allocate(16);
        b.put(temp);
        Message res = new Message(MessageType.DATA, b);
        return res;
    }

//=======\\ Ping Algorithm //=======\\     
    //multithread new method called startPinging(), locking main thread until done
    private static final int ITERATIONS = 10; //#iterations for establishing routing table;
    public void startPinging() {
        resetTable();
        try{Thread.sleep(new Random().nextInt(10)*10);}catch(InterruptedException e){}
        for (int i = 0; i < ITERATIONS; i ++) {
            ping();
            try{Thread.sleep(800);}catch(InterruptedException e){}
        }
        try{Thread.sleep(3000);}catch(InterruptedException e){}
        //PRINT the routing table: reach X-node = #-hops
        for (int key: Routing.keySet()) {
            System.out.print(" reach: " + key + " = " + RoutHops.get(key) + " via " + Routing.get(key));
        } 
        System.out.println("");
        String available = "";
        for (int key: Routing.keySet()) {
            available += "/" + key + "=available  ";
        } 
        UI.textArea_1.append(available + "\r\n");
        UI.textArea_1.append("Connected! \r\n");
    }
    
    public void pingSkip() {
        pingthread.skip();
    }
    
    //Send ping signals
    public void ping() {
        int i = 2;
        int[] maps = new int[Routing.size()*2];
        //take entire Routing table, make sure my name is first entry
        for (Integer key:Routing.keySet()) {
            if (key == name) {
                maps[0] = key;
                maps[1] = 0;
            } else {
                maps[i] = key;
                maps[i+1] = RoutHops.get(key);
                i += 2;
            }
        }
        //convert local copy of routing table to string
        String routes = "";
        for (i = 0; i < maps.length; i ++) {
            String temp = Integer.toBinaryString(maps[i]);
            //make sure every integer is 2 bits (also 0 and 1)
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            routes += temp;
        }
        //fill string with 0's untill length 16
        while (routes.length() < 16) {
            routes += "0";
        }
        
        //transform bit string to 2 integers
        int x = Integer.parseInt(routes.substring(0,8),2);
        int y = Integer.parseInt(routes.substring(8),2);
        //put 2 message integers into bytebuffer
        ByteBuffer toSend = ByteBuffer.allocate(2);
        toSend.put((byte)x);
        toSend.put((byte)y);
        Message msg = new Message(MessageType.DATA_SHORT, toSend);
        //MAC
        Random rand = new Random();
        try{Thread.sleep(rand.nextInt(4)*25);}catch(InterruptedException e){}
        mac(msg);
    }
    
    //recieve ping signals and update routing table
    public void readPing(ByteBuffer message) {
        //transform mesage to byte array
        byte[] bytes = message.array();
        String bits = JANSCII.fill(bytes[0]); 
        String t = JANSCII.fill(bytes[1]);
        
        bits += t;
        //save as 2 bit integers (even indexes are node names, odd indexes are #Hops)
        int[] maps = new int[8];
        for (int i = 0; i < 8; i ++) {
            maps[i] = Integer.parseInt(bits.substring(i*2,i*2+2),2);
        }
        //try for every node name if it is in routing table
        boolean found = false;
        for (int i = 0; i < 4; i ++) {
            if (i != 0 && maps[2*i] == 0 && maps[2*i+1] == 0) {
                //the only possible 4 bits with 0000 is from node A, and A sends 0000 at the start of the array
                // any other position means the zeroes are padding
            } else {
                for (Integer key: Routing.keySet()) {
                    if (key == maps[2*i]) {
                        if (RoutHops.get(key) > maps[2*i+1]+1) {
                            //new value is lower
                            Routing.put(key,maps[0]);
                            RoutHops.put(key,maps[2*i+1]+maps[1] + 1);
                            break;
                        }
                        found = true;
                    }
                }
                if (!found) {
                    //not yet in routing table
                    Routing.put(maps[2*i],maps[0]);
                    RoutHops.put(maps[2*i],maps[2*i+1]+1);
                }
                found = false;
            }
        }
    }

 //======\\ Main function //=========\\ 
//==========testing method============\\ 
    public static void main(String args[]) {
        MyProtocol j = new MyProtocol(SERVER_IP, SERVER_PORT, frequency); 
        if(args.length > 0) {
            j.setName(Integer.parseInt(args[0])%4);
        } else {
            j.setName(new Random().nextInt(4));
        }
        try{Thread.sleep(5000);}catch(InterruptedException e){} //wait 5 sec at start before pinging
        j.UI.textArea_1.append("Connecting..... \r\n");
        //multithreaded immidiate ping
        j.pingthread.skip();
        //multithreaded immediate read (will at first be actively blocked by pingthread)
        j.read();
    }
//=========\\ Recieving Thread //=======\\ 
    private class receiveThread extends Thread {
        protected BlockingQueue<Message> receivedQueue;
        protected MyProtocol mainThread;
        protected Map<Integer, List<Integer>> sM;//sequenceMap -- >creates a map of sequence numbers with Source as the key
        protected Map<Integer, List<Message>> rM;//receiveMap --> creates a map of message bytes with Source as the key
        protected Map<Integer, List<Integer>> prevSeq;

        public receiveThread(BlockingQueue<Message> receivedQueue, MyProtocol thread){
            super();
            this.receivedQueue = receivedQueue;
            this.mainThread = thread;
            rM = new HashMap<Integer, List<Message>>();
            sM = new HashMap<Integer, List<Integer>>();
            prevSeq = new HashMap<Integer, List<Integer>>();
        }

        public void printByteBuffer(ByteBuffer bytes, int bytesLength){
            for(int i=0; i<bytesLength; i++){
                System.out.print( Byte.toString( bytes.get(i) )+" " );
            }
            System.out.println();
        }
        
        
        public void constructMessage(Message msg) {
        	//look up the source of the packet and its sequence number
        	String bytes = JANSCII.fill(msg.getData().array()[0]);
        	int source = Integer.parseInt(bytes.substring(0,2),2);
        	int seq = msg.getData().array()[1];

        	if (!rM.keySet().contains(source)) { 
        	    rM.put(source, new ArrayList<Message>());  //list of all data packets
        	    sM.put(source, new ArrayList<Integer>()); //list is of all sequence numbers
        	}
        	
        	//retrieve the array associated with that source
        	List<Message> receiveMessage = rM.get(source);
        	List<Integer> sequenceNum = sM.get(source); 
    	        
        	//insert the sequence number to the map and sort it
        	if (!sequenceNum.contains(seq) && (prevSeq.get(source) == null || !prevSeq.get(source).contains(seq))) {
        	    //not in current window, not in previous window
        	    sequenceNum.add(seq);
        	    Collections.sort(sequenceNum);
        	    //save message
        	    receiveMessage.add(msg);
        	} //if seq was in the current or previous window, the window may still be printed, but message will not be appended
        	
        	//insert the new updated array to the map 
        	this.rM.put(source, receiveMessage);
        	this.sM.put(source, sequenceNum);
        	boolean start = false;
        	int seqStart = 0;
        	boolean fin = false;
        	int seqFin = 0;
        	for (int i = 0; i < receiveMessage.size(); i ++) {
                byte[] temp = receiveMessage.get(i).getData().array();
                String bits = JANSCII.fill(temp[0]);
                String bits2 = JANSCII.fill(temp[3]);

                if (bits2.substring(6).equals("01")) { //Start
                    start = true;
                    seqStart = (int) temp[1];
                }
                if (bits.substring(6).equals("10")) { //end
                    fin = true;
                    seqFin = (int) temp[1];
                }
            }
            if (start && fin) {
                if (receiveMessage.size() == seqFin-seqStart + 1) {
                    //all frames are recieved
                    //send to decode
                    //remove datastream
                    UI.writeChat(TCPCoding.decode(rM, source), source, false);
                    prevSeq.put(source, sM.get(source));
                    rM.remove(source);
                    sM.remove(source);
                } else if (seqFin-seqStart == 0) { //if start == fin, the message length is still 1
                    UI.writeChat(TCPCoding.decode(rM, source), source, false);
                    prevSeq.put(source, sM.get(source));
                    rM.remove(source);
                    sM.remove(source);
                }
            }
        }
        
        //create an ACK packet
        public Message createAck(int dest, int seq) { //dest --> destination of the ack packet, seq --> sequence num of Message packet
        	int src = mainThread.name;
        	String flags = "01";
        	int syn = 0;
        	int ack = seq;
        	int checksum = 0;
        	byte[] ackHeader = mainThread.TCPHeader(src, dest, flags, syn, ack, checksum, false);
        	
        	ByteBuffer ackBuffer = ByteBuffer.allocate(16);
        	ackBuffer.put(ackHeader, 0, 4);
        	
        	Message ackPacket = new Message(MessageType.DATA, ackBuffer);
        	return ackPacket;
        }

        public void run(){
            while(true) {
                try{
                    Message m = receivedQueue.take();
                    if (m.getType() == MessageType.BUSY){
                    	lineStatus = MessageType.BUSY;
                    } else if (m.getType() == MessageType.FREE){
                    	lineStatus = MessageType.FREE;
                    } else if (m.getType() == MessageType.DATA){
                    	handlingThread H = new handlingThread(m, this); //disappears after having handled the data
                    } else if (m.getType() == MessageType.DATA_SHORT){
                        readPing(m.getData());
                        //start pinging if not yet already doing so
                        mainThread.pingthread.skip();
                    } else if (m.getType() == MessageType.DONE_SENDING){
                        //System.out.println("DONE_SENDING");
                    } else if (m.getType() == MessageType.HELLO){
                        //System.out.println("HELLO");
                    } else if (m.getType() == MessageType.SENDING){
                        //System.out.println("SENDING");
                    } else if (m.getType() == MessageType.END){
                        System.out.println("END");
                        System.exit(0);
                    }
                } catch (InterruptedException e){
                    System.err.println("Failed to take from queue: "+e);
                }                
            }
        }
    }
//========\\ Handling thread //===========\\ 
    private class handlingThread extends Thread {
        //this thread safeguards receiveThread from waiting too long to read linestatus
        // if receiveThread had to do all calculations, it would take a long time while not reading
        //  line status, giving room for collisions, since all sending is in seperate threads
        private Message m;
        private receiveThread r;
        
        public handlingThread(Message m, receiveThread r) {
            super();
            this.r = r;
            this.m = m;
            this.start();
        }
        
        public void run() {
            byte[] bytes = m.getData().array();
            String bits = JANSCII.fill(bytes[0]); 
            int dest = Integer.parseInt(bits.substring(2, 4), 2);
            int nextHop = Integer.parseInt(bits.substring(4, 6), 2);
            int src = Integer.parseInt(bits.substring(0, 2), 2);
            int seq = (int) bytes[1];
            if (!r.mainThread.Routing.keySet().contains(src)) {
                //routing error, start pinging without saving any data (src will resend if it has to)
                r.mainThread.pingthread.skip();
            } else if(dest == r.mainThread.name) {
            	//for tcp
            	//check for flags
            	String flag = bits.substring(6, 8);
            	if(flag.equals("01")) { // Flag == ACK
            		System.out.println("you got an ack" + m.getData().array()[2]);
            		r.mainThread.tcpThread.receiveAck(m);
            	} else {
            	    //create ACK
            	    r.constructMessage(m);
            	    //mainThread.mac(this.createAck(src, seq));
            	    new ackThread(r.mainThread, src, seq);
            	}
            	if (nextHop != r.mainThread.name) {
            	    //you got the message while the sender thought that there was a node inbetween: wrong routing table
            	    //start pinging again
            	    r.mainThread.pingthread.skip();
            	}
            }else if(nextHop == r.mainThread.name) {
            	int temp = Forwarding(dest);
             	String hop = Integer.toBinaryString(temp);
            	if(hop.length() == 1) {
            		hop = "0" + hop;
            	}
            	else if(hop.length() > 2) {
            		hop = hop.substring(hop.length() - 2);
            	}
            	bits = bits.substring(0,4) + hop + bits.substring(6,8);
            	System.out.println("Forwarding to " + dest + " via " + temp);
            	temp = Integer.parseInt(bits, 2);
            	ByteBuffer toSend = ByteBuffer.allocate(16);
            	
            	toSend.put((byte) temp);
            	for(int i = 1; i< 16; i++) {
            		toSend.put(bytes[i]);
            	}
            	
            	Message msg = new Message(MessageType.DATA, toSend);
            	r.mainThread.mac(msg);
            }
        }
    }
    
//=======\\ Ping thread (timeout counter) //=======\\     
    private class PingThread extends Thread {
        private MyProtocol mainThread;
        //update Routing once every TIME seconds
        private static final int TIME = 300;
        private int i = 0;
        public PingThread(MyProtocol thread) {
            mainThread = thread;
        }
        
        public void run() {
            while(true) {
                while (i < TIME-10) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        
                    }
                    i += 1;
                }
                //activate the loop in which a wait() command is
                mainThread.lockIt = true;
                mainThread.tcpThread.pinging = true;
                mainThread.startPinging();
                //deactivate loop
                mainThread.lockIt = false;
                //notify wait()-command
                synchronized(mainThread) {
                    mainThread.notify();
                }
                mainThread.tcpThread.pinging = false;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    
                }
                i = 0; //reset timer, but never stop ping thread
            }
        }
        
        //will be called from a lot of different threads to activate pinging
        public void skip() {
            i = TIME + 1;
        }
    }
//=======\\ Acknowledgement thread //======\\     
    private class ackThread extends Thread {
    //this thread makes sure that receiveThread doesnt have to waste time with acknowledgements
        private MyProtocol mainThread;
        private int dest, seq;
                
        public ackThread(MyProtocol mainThread, int dest, int seq) {
            super();
            this.mainThread = mainThread;
            this.dest = dest;
            this.seq = seq;
            this.start();
            
        }
        
        public void run() {
            mainThread.mac(createAck(dest,seq));
        }
        
        //create an ACK packet
        public Message createAck(int dest, int seq) { //dest --> destination of the ack packet, seq --> sequence num of Message packet
        	int src = mainThread.name;
        	String flags = "01";
        	int syn = 0;
        	int ack = seq;
        	int checksum = 0;
        	byte[] ackHeader = mainThread.TCPHeader(src, dest, flags, syn, ack, checksum, false);
        	
        	ByteBuffer ackBuffer = ByteBuffer.allocate(16);
        	ackBuffer.put(ackHeader, 0, 4);
        	
        	Message ackPacket = new Message(MessageType.DATA, ackBuffer);
        	try{Thread.sleep(new Random().nextInt(2)*50);}catch(InterruptedException e){} //so that it wont collide with a possible second packet, 
        	//since it wont see the line is busy if it sends too quickly
        	return ackPacket;
        }
    }
}

