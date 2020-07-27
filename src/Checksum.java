public class Checksum {
	
	public static int calcChecksum(int[] m) {
		int checksum = 0;
		for (int i = 0; i < m.length; i++) {
			checksum += m[i];
			int temp = checksum / 256;
			checksum = checksum % 256;
			checksum += temp;
		}
		checksum ^= 255;
		return checksum;
	}
	
	public static boolean checkChecksum(int[] m, int val) {
		int check = calcChecksum(m);
		return (check == val);
	}
	
}
