public class Timer implements Runnable {
	
	private int length;
	private int counter;
	private MyProtocol m;
	private int seq;
	private int dest;
	private boolean receiveAck;
	
	public Timer(MyProtocol m, int length, int count, int dest) {
		this.length = length;
		this.m = m;
		this.seq = count;
		this.receiveAck = false;
		this.dest = dest;
		counter = 0;
		
	}
	
	public int getCount() {
		return seq;
	}
	
	public void receiveAck() {
		this.receiveAck = true;
	}

	public boolean[] getCounter() {
		return new boolean[]{counter < length, receiveAck, length == 300, counter == 0};
	}

	public int getDest() {
		return dest;
	}
	
	public void run() {
		counter = 0;
		while(counter < length) {
			try {
				Thread.sleep(50);
				if(receiveAck) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;
		}
		if(!receiveAck) {
			//m.addResendPackets(dest, seq);
			//m.tcp(null);
			m.tcpThread.reSend(seq);
			try{Thread.sleep(100);}catch(InterruptedException e) {}
			m.tcpThread.sendPacket(null);
			System.out.println("--TIMEOUT--");
			System.out.println("Did not receive ack for packet No: " + this.getCount());
		}
	}
	
	
}
