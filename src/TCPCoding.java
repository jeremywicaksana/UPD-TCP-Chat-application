import java.util.*;
import client.*;

public class TCPCoding {
    //Transform window of messages into one string (message)
    public static String decode(Map<Integer,List<Message>> messages, int source) {
        byte[][] frames = new byte[messages.get(source).size()][12];
        int firstSeq = 0;
        //save first frame and decide following seq nums
        for (int i = 0; i < frames.length; i ++) {
            byte[] temp = messages.get(source).get(i).getData().array();
            int z = (int) temp[0];
            int y = (int) temp[3];
            String bits = JANSCII.fill(z);
            String bits2 = JANSCII.fill(y);
            if (bits2.substring(6).equals("01")) {
                firstSeq = (int) temp[1];
                for (int p = 4; p < 16; p ++) {
                    //save first 
                    frames[0][p-4] = temp[p];
                }
                break;
            }
        }
        //order frames
        for (int i = 0; i < frames.length; i ++) {
            byte[] temp = messages.get(source).get(i).getData().array();
            String bits = JANSCII.fill(temp[0]);
            String bits2 = JANSCII.fill(temp[3]);
            if (!bits2.substring(6).equals("01")) {
                int thisSeq = (int) temp[1];
                for (int p = 4; p < 16; p ++) {
                    //save in respective index 
                    frames[Math.abs(thisSeq-firstSeq)][p-4] = temp[p];
                }
            }
        }
        for (int i = 0; i < frames.length; i ++) {
            for (int j = 0; j < 12; j ++) {
            }
        }
        //decode using JANSCII
        String result = "";
        for (int i = 0; i < frames.length; i ++) {
            int[] temp = new int[12];
            for (int p = 0; p < 12; p ++) {
                temp[p] = (int) frames[i][p];
            }
            result += JANSCII.decode(temp);
        }
        return result;
    }
    
    
}
