package com.jerry.bluetoothprinter.service;


public class CodeUtil {
	// public static byte[] hexStringToBytes(String hex) {
	// int len = (hex.length() / 2);
	// byte[] result = new byte[len];
	// char[] achar = hex.toCharArray();
	// for (int i = 0; i < len; i++) {
	// int pos = i * 2;
	// result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
	// }
	// return result;
	// }
	//
	// private static byte toByte(char c) {
	// byte b = (byte) "0123456789ABCDEF".indexOf(c);
	// return b;
	// }
	private final static char[] CS = "0123456789ABCDEF".toCharArray();

	public static void printdatas(String name, byte[] bs) {
		if (name == null) {
			System.out.println(name + ":null");
			return;
		}
		if (bs == null) {
			System.out.println("bs:null");
			return;
		}
		StringBuilder sb = new StringBuilder(bs.length * 3 + 50);
		sb.append(name).append(':');
		for (int n : bs) {
			sb.append(CS[(n >> 4) & 0xF]);
			sb.append(CS[(n >> 0) & 0xF]);
			sb.append(' ');
		}
		System.out.println(sb);
	}

	public static byte[] hexToBytes(String s) {
		s = s.toUpperCase();
		int len = s.length() / 2;
		int ii = 0;
		byte[] bs = new byte[len];

		for(int i = 0; i < len; ++i) {
			char c = s.charAt(ii++);
			int h;
			if(c <= 57) {
				h = c - 48;
			} else {
				h = c - 65 + 10;
			}

			h <<= 4;
			c = s.charAt(ii++);
			if(c <= 57) {
				h |= c - 48;
			} else {
				h |= c - 65 + 10;
			}

			bs[i] = (byte)h;
		}

		return bs;
	}

	/**
	 * 灏嗗瓧绗﹁浆鎹负瀛楄妭鏁扮粍
	 * 
	 * @param in
	 * @return
	 */
	public static byte[] hexStringToBytes(String in) {
		byte[] arrB = in.getBytes();
		int iLen = arrB.length;
		// 涓や釜瀛楃琛ㄧず涓?釜瀛楄妭锛屾墍浠ュ瓧鑺傛暟缁勯暱搴︽槸瀛楃涓查暱搴﹂櫎浠?
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 灏嗗瓧鑺傝浆鍖栦负瀛楃
	 * 
	 * @param b
	 * @return
	 */
	public static char byteToChar(byte[] b) {
		int s = 0;
		if (b[0] > 0) {
			s += b[0];
		}
		if (b[0] < 0) {
			s += 256 + b[0];
		}
		s *= 256;
		if (b[1] > 0) {
			s += b[1];
		}
		if (b[1] < 0) {
			s += 256 + b[1];
		}
		char ch = (char) s;
		return ch;
	}

	/**
	 * 灏嗗瓧鑺傝浆鎹负瀛楃
	 * 
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte b) {
		String hex = "";
		hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		return hex;
	}

	/**
	 * 灏嗗瓧绗﹁浆鍖栦负瀛楄妭
	 * 
	 * @param ch
	 * @return
	 */
	public static byte[] charToByte(char ch) {
		int temp = (int) ch;
		byte[] b = new byte[2];
		// 灏嗛珮8浣嶆斁鍦╞[0],灏嗕綆8浣嶆斁鍦╞[1]
		for (int i = 1; i > -1; i--) {
			b[i] = (byte) (temp & 0xFF);// 浣?浣?
			// 鍚戝彸绉?浣?
			temp >>= 8;
		}
		return b;
	}

	/**
	 * 灏嗗瓧鑺傝浆鍖栦负姣旂壒鏁扮粍
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] byteToBitArray(byte b) {
		// 寮哄埗杞崲鎴恑nt?
		int temp = (int) b;
		byte[] result = new byte[8];
		for (int i = 7; i > -1; i--) {
			result[i] = (byte) (temp & 0x01);
			temp >>= 1;
		}
		return result;
	}

	/**
	 * 灏嗕簩缁存瘮鐗规暟缁勮浆鍖栦负瀛楄妭
	 * 
	 * @param b
	 * @return
	 */
	public static byte bitToByteArray(byte[] b) {
		byte result;
		result = (byte) (b[7] | b[6] << 1 | b[5] << 2 | b[4] << 3 | b[3] << 4 | b[2] << 5 | b[1] << 6 | b[0] << 7);
		return result;
	}

	/**
	 * 灏嗗瓧鑺傛暟缁勮浆鎹负瀛楃
	 * 
	 * @param bs
	 * @return
	 */
	public static String bytesToHexString(byte[] bs) {
		if (bs == null)
			return "";
		StringBuffer sb = new StringBuffer();
		String hex = "";
		for (int i = 0; i < bs.length; i++) {
			hex = Integer.toHexString(bs[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(' ');
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 鎶婂瓧鑺傛暟缁勮浆鎹负鏃犵鍙穒nt绫诲瀷
	 * 
	 * @param ss
	 * @param len
	 * @return
	 */
	public static int util_defHash(byte[] ss, int len) {
		int pre = 122356567;
		byte[] us = ss;
		for (int i = 0; i < len; i++) {
			pre *= 1313131;
			pre += us[i] & 0xFF;// 杞崲涓烘棤绗﹀彿byte
		}
		return pre;
	}

	public static int byteArrayToInt(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;
		for (int i = 0; i < 4; i++) {
			bLoop = bRefArr[i];
			iOutcome = iOutcome | (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	public static int defHash(byte[] bs) {
		int pre = 122356567;
		for (int i = 0; i < bs.length; i++) {
			pre *= 1313131;
			pre += (bs[i] & 0xFF);
		}
		return pre;
	}

	private final static byte[] bpre = 
			hexToBytes("30820122300D06092A864886F70D01010105000382010F003082010A02820101");
	private final static byte[] bend = hexToBytes("0203010001");
	// 鐢熸垚256闀垮害鍏挜
	public static byte[] makePublicKey256(byte[] bs) {
		byte[] rs = new byte[bs.length + bpre.length + bend.length];
		int io = 0;
		System.arraycopy(bpre, 0, rs, io, bpre.length);
		io += bpre.length;
		System.arraycopy(bs, 0, rs, io, bs.length);
		io += bs.length;
		System.arraycopy(bend, 0, rs, io, bend.length);
		return rs;
	}
	
	public static int util_defHash(byte[] us) {
		int pre = 122356567;
		int len = us.length;
		for (int i = 0; i < len; i++) {
			pre *= 1313131;
			pre += (us[i] & 0xFF);
		}
		return pre;
	}

	/**
	 * 灏嗗瓧绗﹁浆鎹负瀛楄妭鏁扮粍
	 * 
	 * @param in
	 * @return
	 */
	public static byte[] hexStrToBytes(String in) {
		byte[] arrB = in.getBytes();
		int iLen = arrB.length;
		// 涓や釜瀛楃琛ㄧず涓?釜瀛楄妭锛屾墍浠ュ瓧鑺傛暟缁勯暱搴︽槸瀛楃涓查暱搴﹂櫎浠?
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 灏嗗瓧鑺傛暟缁勮浆鎹负瀛楃
	 * 
	 * @param bs
	 * @return
	 */
	public static String bytesToHex(byte[] bs) {
		if (bs==null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String hex = "";
		for (int i = 0; i < bs.length; i++) {
			hex = Integer.toHexString(bs[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
