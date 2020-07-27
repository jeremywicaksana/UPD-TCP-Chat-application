
import java.util.*;
public class JANSCII {

    private static final int MESSAGELENGTH = 16;
    private static final String[] LETTERS = new String[]
                                      {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
                                       "a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
                                       "0","1","2","3","4","5","6","7","8","9","."," "};

    public static byte[] encode(String message){
        //this only works for message length under or equal to 16
        //if message is longer, split it before encoding, and send it through different frames
        /*if (message.length() > MESSAGELENGTH) {
            throw new LengthException("message too long");
        }*/
        
        //encode using integers 0-63
        int[] temp = new int[MESSAGELENGTH]; //every message becomes 16 JANSCII
        for (int i = 0; i < message.length(); i ++) {
            for (int p = 0; p < 64; p ++) {
                if (message.substring(i,i+1).equals(LETTERS[p])) {
                    temp[i] = p;
                    break;
                } else if (p == 63) {
                    temp[i] = 63; //any unknown character becomes a space
                }
            }
        }
        
        //make it one large binary string
        String binary = "";
        for (int i = 0; i < MESSAGELENGTH; i ++) {
            String z = Integer.toBinaryString(temp[i]);
            if (i >= message.length()) {
                z = "111111"; //format is 16 janscii (6 bit), so if we only need 10 janscii, we pad with one's, resulting in a space
            }
            
            while (z.length() < 6) {
                z = "0" + z ; //janscii is exactly 6 bits
            }
            binary = binary + z;
        }
        //split into bytes (8 bits instead of 6) (12 bytes =^ 16 janscii)
        int[] frame = new int[12];
        byte[] result = new byte[12];
        for (int i = 0; i < 12; i ++) {
            String z = binary.substring(i*8, i*8+8);
            frame[i] = Integer.parseInt(z,2);
            result[i] = (byte) frame[i];
        }
        return result;
    }
    
    public static String decode(int[] message) {
        String temp = "";
        //create one large binary string
        for (int i = 0; i < 12; i ++) {
            String z = fill(message[i]);
            if (message[i] == 0) {
                z = "00000000";
            }
            temp = temp + z;
        }
        
        //split into janscii (6 bits instead of 8)
        int[] frame = new int[16];
        for (int i = 0; i < 16; i ++) {
            String z = temp.substring(i*6,i*6+6);
            frame[i] = Integer.parseInt(z,2);
        }
        
        //decode
        String result = "";
        for (int i = 0; i < 16; i ++) {
            result = result + LETTERS[frame[i]];
        }
        if (result.substring(15).equals(" ")) {
            for (int i = 15; i > 0; i --) {
                if (result.substring(i).equals(" /")) {
                    result = result.substring(0,i);
                } else {
                    break;
                }
            }
        }
        return result;
    }

    //transforms an integer to a full 8 bit string
    public static String fill(int x) {
        String bits = Integer.toBinaryString(x);
        if (bits.length() > 8) {
            bits = bits.substring(bits.length() - 8);
        }
        while (bits.length() < 8) {
            bits = "0" + bits;
        }
        return bits;
    }
    
    public static String fill(byte x) {
        return fill((int)x);
    }
}
