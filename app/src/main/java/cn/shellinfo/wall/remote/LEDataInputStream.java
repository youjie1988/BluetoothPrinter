package cn.shellinfo.wall.remote;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 灏忕锛圠ittle-Endian锛夋暟鎹緭鍏�
 */
public final class LEDataInputStream {

	private byte[] bbuf;

	private char[] cbuf;

	protected final InputStream in;

	private byte readBuffer[] = new byte[8];

	public LEDataInputStream(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public final int read(byte b[]) throws IOException {
		return in.read(b, 0, b.length);
	}

	public final int read(byte b[], int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public final boolean readBoolean() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		return (ch != 0);
	}

	public final byte readByte() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		return (byte) (ch);
	}

	public final char readChar() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (char) ((ch2 << 8) + (ch1 << 0));
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public final int readInt() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
	}

	public final long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return (((long) (readBuffer[7] & 255) << 56) + ((long) (readBuffer[6] & 255) << 48) + ((long) (readBuffer[5] & 255) << 40) + ((long) (readBuffer[4] & 255) << 32) + ((long) (readBuffer[3] & 255) << 24) + ((readBuffer[2] & 255) << 16) + ((readBuffer[1] & 255) << 8) + ((readBuffer[0] & 255) << 0));
	}

	public final Object readObject() throws IOException {
		int ID = in.read();
		switch (ID) {
		case 0: {// null
			return null;
		}
		case 1: {// Integer
			return new Integer(readInt());
		}
		case 2: {// Long
			return new Long(readLong());
		}
		case 3: {// String
			return readString();
		}
		case 4: {// byte[]
			int len = readInt();
			byte[] r = new byte[len];
			readFully(r);
			return r;
		}
		case 5: {// short[]
			int len = readInt();
			short[] r = new short[len];
			for (int i = 0; i < len; i++) {
				r[i] = readShort();
			}
			return r;
		}
		case 6: {// int[]
			int len = readInt();
			int[] r = new int[len];
			for (int i = 0; i < len; i++) {
				r[i] = readInt();
			}
			return r;
		}
		case 7: {// long[]
			int len = readInt();
			long[] r = new long[len];
			for (int i = 0; i < len; i++) {
				r[i] = readLong();
			}
			return r;
		}
		case 8: {// List
			int len = readInt();
			ArrayList<Object> r = new ArrayList<Object>(len);
			for (int i = 0; i < len; i++) {
				r.add(readObject());
			}
			return r;
		}
		case 9: {// Map
			int len = readInt();
			HashMap<String, Object> r = new HashMap<String, Object>(len);
			for (int i = 0; i < len; i++) {
				r.put(readString(), readObject());
			}
			return r;
		}
		case 10: {// Object[]
			int len = readInt();
			Object[] r = new Object[len];
			for (int i = 0; i < len; i++) {
				r[i] = readObject();
			}
			return r;
		}
		case 11: {
			return new Boolean(readBoolean());
		}
		case 12: {
			int len = readInt();
			boolean[] r = new boolean[len];
			for (int i = 0; i < len; i++) {
				r[i] = readBoolean();
			}
			return r;
		}
		case 14: {
			return readDouble();
		}
		case 15:{
			int len=readByte();
			String[] ks=new String[len];
			Object[] vs=new Object[len];
			for(int i=0;i<len;i++){
				ks[i]=readString();
				vs[i]=readObject();
			}
			return new ParamMap(ks,vs);
		}
		case 16:{
			int len=readInt();
			char[] cs=new char[len];
			for(int i=0;i<len;i++){
				cs[i] = readChar();
			}
			return cs;
		}
		case 17: {
			long n=readLong();
			return Double.longBitsToDouble(n);
		}
		case 18: {
			int len=readInt();
			double[] ds=new double[len];
			for(int i=0;i < len;i++){
				long n=readLong();
				ds[i]= Double.longBitsToDouble(n);
			}
			return ds;
		}
		default:
			throw new RuntimeException(StringConfig.unknown_data_type + ID);
		}
	}

	public final short readShort() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (short) ((ch2 << 8) + (ch1 << 0));
	}

	public final String readString() throws IOException {
		int len = in.read();
		if (len < 0) {
			throw new EOFException();
		}
		if (len == 255) {
			len = readInt();
		}
		if (cbuf == null || cbuf.length < len) {
			bbuf = new byte[len << 1];
			cbuf = new char[len];
		}
		byte[] buf = bbuf;
		readFully(buf, 0, len << 1);
		char[] cs = cbuf;
		for (int i = 0; i < len; i++) {
			cs[i] = (char) ((buf[i << 1] & 0xFF) | ((buf[(i << 1) + 1] & 0xFF) << 8));
		}
		return new String(cs, 0, len);
	}
	public final int readUnsignedByte() throws IOException {
		int ch = in.read();
		if (ch < 0) {
			throw new EOFException();
		}
		return ch;
	}

	public final int readUnsignedShort() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return (ch2 << 8) + (ch1 << 0);
	}

	public final int skipBytes(int n) throws IOException {
		int total = 0;
		int cur = 0;
		while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
			total += cur;
		}
		return total;
	}
}
