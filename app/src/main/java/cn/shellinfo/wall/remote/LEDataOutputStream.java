package cn.shellinfo.wall.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 灏忕锛圠ittle-Endian锛夋暟鎹緭鍑哄埌瀛楄妭鏁扮粍
 * 
 * @author houj
 */
public final class LEDataOutputStream{

	private byte[] buf=new byte[1024];

	private int len;

	public LEDataOutputStream(int initLen){
		buf=new byte[initLen];
	}

	public LEDataOutputStream(){
		this(1024);
	}

	/**
	 * 鍙栧緱缂撳啿鍖哄ぇ灏�
	 */
	public int bufSize(){
		return buf.length;
	}

	/**
	 * @param n
	 */
	private void newSize(int n){
		int nlen=buf.length * 2;
		if(nlen < n){
			nlen=n;
		}
		byte[] pre=buf;
		buf=new byte[nlen];
		System.arraycopy(pre,0,buf,0,pre.length);
	}

	/**
	 * 閲嶇疆鏁版嵁涓洪暱搴�0
	 */
	public void reset(){
		len=0;
	}

	/**
	 * 杩斿洖鏁版嵁闀垮害.
	 * 
	 * @return
	 */
	public int size(){
		return len;
	}

	/**
	 * 宸叉湁鐨勬暟鎹浆鎹负瀛楄妭鏁扮粍.
	 * 
	 * @return
	 */
	public byte[] toByteArray(){
		byte[] r=new byte[len];
		System.arraycopy(buf,0,r,0,len);
		return r;
	}

	/**
	 * 鍐欏瓧鑺傛暟缁�.
	 * 
	 * @param b
	 *            瀛楄妭鏁扮粍
	 */
	public void write(byte[] b){
		if(len + b.length > buf.length){
			newSize(len + b.length);
		}
		System.arraycopy(b,0,buf,len,b.length);
		len+=b.length;
	}

	/**
	 * 鍐欏瓧鑺傛暟缁�.
	 * 
	 * @param b
	 *            瀛楄妭鏁扮粍
	 * @param pos
	 *            寮�濮嬩綅缃�
	 * @param blen
	 *            闀垮害
	 */
	public void write(byte[] b,int pos,int blen){
		if(len + blen > buf.length){
			newSize(len + blen);
		}
		System.arraycopy(b,pos,buf,len,blen);
		len+=blen;
	}

	/**
	 * 鍐檅oolean鍊�
	 * 
	 * @param b
	 */
	public void writeBoolean(boolean b){
		if(buf.length < len + 1){
			newSize(len + 1);
		}
		buf[len++ ]=b ? (byte) 1 : (byte) 0;
	}

	/**
	 * 鍐檅yte
	 * 
	 * @param b
	 */
	public void writeByte(int b){
		if(buf.length < len + 1){
			newSize(len + 1);
		}
		buf[len++ ]=(byte) (b);
	}

	/**
	 * 鍐檌nt
	 * 
	 * @param n
	 */
	public void writeInt(int n){
		int len=this.len;

		if(buf.length < len + 4){
			newSize(len + 4);
		}

		byte[] bs=this.buf;
		bs[len++ ]=(byte) (n >>> 0);
		bs[len++ ]=(byte) (n >>> 8);
		bs[len++ ]=(byte) (n >>> 16);
		bs[len++ ]=(byte) (n >>> 24);
		this.len=len;
	}

	public void setInt(int i,int n){
		byte[] bs=this.buf;
		bs[i++ ]=(byte) (n >>> 0);
		bs[i++ ]=(byte) (n >>> 8);
		bs[i++ ]=(byte) (n >>> 16);
		bs[i++ ]=(byte) (n >>> 24);
	}

	/**
	 * 鍐檒ong
	 * 
	 * @param n
	 */
	public void writeLong(long n){
		int len=this.len;
		if(buf.length < len + 8){
			newSize(len + 8);
		}
		byte[] bs=this.buf;
		bs[len]=(byte) (n >>> 0);
		bs[len + 1]=(byte) (n >>> 8);
		bs[len + 2]=(byte) (n >>> 16);
		bs[len + 3]=(byte) (n >>> 24);
		bs[len + 4]=(byte) (n >>> 32);
		bs[len + 5]=(byte) (n >>> 40);
		bs[len + 6]=(byte) (n >>> 48);
		bs[len + 7]=(byte) (n >>> 56);
		len+=8;
		this.len=len;
	}

	/**
	 * 鍐欏璞�
	 * 
	 * @param o
	 */
	public final void writeObject(Object o){
		if(buf.length < len + 8){
			newSize(len + 8);
		}
		if(o == null){
			buf[len++ ]=0;
		}
		else if(o instanceof String){
			buf[len++ ]=3;
			writeString((String) o);
		}
		else if(o instanceof Integer){
			buf[len++ ]=1;
			writeInt(((Integer) o).intValue());
		}
		else if(o instanceof Long){
			buf[len++ ]=2;
			writeLong(((Long) o).longValue());
		}
		else if(o instanceof byte[]){
			buf[len++ ]=4;
			byte[] bs=(byte[]) o;
			writeInt(bs.length);
			write(bs);
		}
		else if(o instanceof short[]){
			buf[len++ ]=5;
			short data[]=(short[]) o;
			int len=data.length;
			writeInt(len);
			for(int i=0;i < len;i++ ){
				writeShort(data[i]);
			}
		}
		else if(o instanceof int[]){
			buf[len++ ]=6;
			int data[]=(int[]) o;
			int len=data.length;
			writeInt(len);
			for(int i=0;i < len;i++ ){
				writeInt(data[i]);
			}
		}
		else if(o instanceof long[]){
			buf[len++ ]=7;
			long data[]=(long[]) o;
			int len=data.length;
			writeInt(len);
			for(int i=0;i < len;i++ ){
				writeLong(data[i]);
			}
		}
		else if(o instanceof List){
			buf[len++ ]=8;
			List<Object> data=(List<Object>) o;
			int len=data.size();
			writeInt(len);
			for(int i=0;i < len;i++ ){
				writeObject(data.get(i));
			}
		}
		else if(o instanceof Map){
			buf[len++ ]=9;
			Map<String,Object> data=(Map) o;
			writeInt(data.size());
			for(String k:data.keySet()){
				Object value=data.get(k);
				writeString(k);
				writeObject(value);
			}
		}
		else if(o instanceof Object[]){
			buf[len++ ]=10;
			Object data[]=(Object[]) o;
			int len=data.length;
			writeInt(len);
			for(int i=0;i < len;i++ ){
				writeObject(data[i]);
			}
		}
		else if(o instanceof Boolean){
			buf[len++ ]=11;
			buf[len++ ]=((Boolean) o).booleanValue() ? (byte) 1 : (byte) 0;
		}
		else if(o instanceof boolean[]){
			buf[len++ ]=12;
			boolean[] bs=(boolean[]) o;
			int len=bs.length;

			for(int i=0;i < len;i++ ){
				this.buf[len++ ]=bs[i] ? (byte) 1 : (byte) 0;
			}
		}
		else if(o instanceof ParamMap){
			buf[len++ ]=15;
			ParamMap map=(ParamMap) o;
			int len=map.size();
			String[] ks=map.keyBuf();
			Object[] vs=map.valueBuf();
			writeByte(len);
			for(int i=0;i < len;i++ ){
				writeString(ks[i]);
				writeObject(vs[i]);
			}
		}
		else if(o instanceof char[]){
			buf[len++ ]=16;
			char[] cs=(char[]) o;
			int slen=cs.length;
			if(buf.length < len + slen * 2 + 4){
				newSize(len + slen * 2 + 4);
			}
			int len=this.len;
			byte[] bs=buf;
			bs[len++ ]=(byte) (slen >>> 0);
			bs[len++ ]=(byte) (slen >>> 8);
			bs[len++ ]=(byte) (slen >>> 16);
			bs[len++ ]=(byte) (slen >>> 24);
			for(int i=0;i < slen;i++ ){
				char c=cs[i];
				bs[len++ ]=(byte) (c >>> 0);
				bs[len++ ]=(byte) (c >>> 8);
			}
			this.len=len;
		}
		else if(o instanceof Double){
			buf[len++ ]=17;
			writeLong(Double.doubleToLongBits((Double) o));
		}
		else if(o instanceof double[]){
			double ds[]=(double[]) o;
			buf[len++ ]=18;
			writeInt(ds.length);
			for(double d:ds){
				writeLong(Double.doubleToLongBits(d));
			}
		}
		// else if (o instanceof Double) {
		// bs[len++] = 14;
		// double bs = (Double) o;
		// writeDouble(bs);
		// }
		else{
			throw new RuntimeException(StringConfig.unknown_data_type + o.getClass().getName());
		}
	}
	
	public void writeDouble(double n){
		writeLong(Double.doubleToLongBits(n));
	}
	
	public void writeFloat(float n){
		writeInt(Float.floatToIntBits(n));
	}

	/**
	 * 鍐檚hort
	 * 
	 * @param n
	 */
	public void writeShort(int n){
		if(buf.length < len + 2){
			newSize(len + 2);
		}
		buf[len++ ]=(byte) (n >>> 0);
		buf[len++ ]=(byte) (n >>> 8);
	}

	/**
	 * 鍐欏瓧绗︿覆.杩欓噷瀛楃涓蹭笉鑳戒负绌�,闀垮害鏃犻檺鍒�
	 * 
	 * @param s
	 */
	public void writeString(String s){
		if(s == null){
			if(buf.length < len + 1){
				newSize(len + 1);
			}
			buf[len++ ]=0;
			return;
		}
		int slen=s.length();
		int len=this.len;
		if(buf.length < len + 5 + (slen << 1)){
			newSize(len + 5 + (slen << 1));
		}
		byte[] bs=this.buf;

		if(slen >= 255){
			bs[len++ ]=(byte) (255);
			bs[len++ ]=(byte) (slen >>> 0);
			bs[len++ ]=(byte) (slen >>> 8);
			bs[len++ ]=(byte) (slen >>> 16);
			bs[len++ ]=(byte) (slen >>> 24);
		}
		else{
			bs[len++ ]=(byte) slen;
		}
		for(int i=0;i < slen;i++ ){
			char c=s.charAt(i);
			bs[len++ ]=(byte) (c >>> 0);
			bs[len++ ]=(byte) (c >>> 8);
		}
		this.len=len;
	}

	/**
	 * 鎶婂凡鏈夋暟鎹繚瀛樺埌娴�
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void writeTo(OutputStream os) throws IOException{
		os.write(buf,0,len);
	}

}
