package cn.shellinfo.wall.remote;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public final class ParamMap implements Parcelable{
	private int size;
	private String[] ks;
	private Object[] vs;

	public ParamMap() {
		ks = new String[8];
		vs = new Object[8];
		size=8;
	}

	public String[] keyBuf() {
		return ks;
	}

	public Object[] valueBuf() {
		return vs;
	}

	public ParamMap(Object... os) {
		this();
		for (int i = 0; i < os.length; i += 2) {
			put((String) os[i], os[i + 1]);
		}
	}

	public ParamMap(String[] ks, Object[] vs) {
		size = ks.length;
		if (size <= 0) {
			ks = new String[8];
			vs = new Object[8];
		}
		this.ks = ks;
		this.vs = vs;
	}

	public boolean containsKey(String key) {
		if (key == null) {
			return false;
		}
		for (int i = size; --i >= 0;) {
			if (key.equals(ks[i])) {
				return true;
			}
		}
		return false;
	}

	public Object get(String key) {
		if (key == null) {
			return null;
		}
		for (int i = size; --i >= 0;) {
			if (key.equals(ks[i])) {
				return vs[i];
			}
		}
		return null;
	}

	public <T> T get(String key, Class<? extends T> c) {
		Object o = get(key);
		if (c.isInstance(o)) {
			return (T) o;
		}
		throw new RuntimeException("not found key:" + key);
	}

	public int[] getIntArray(String key) {
		Object o = get(key);
		if (o instanceof int[]) {
			return (int[]) o;
		}
		throw new RuntimeException("not found key:" + key);
	}

	public String[] keys() {
		String[] ss = new String[size];
		System.arraycopy(ks, 0, ss, 0, ss.length);
		return ss;
	}

	public Object put(String key,Object value){
		if(key == null){
			return null;
		}
		for(int i=size; --i >= 0;){
			if(key.equals(ks[i])){
				Object r=vs[i];
				vs[i]=value;
				return r;
			}
		}
		if(value == null){
			return null;
		}
		if(size >= ks.length){
			{
				String[] pks=ks;
				ks=new String[pks.length * 2];
				System.arraycopy(pks,0,ks,0,pks.length);
			}
			{
				Object[] pvs=vs;
				vs=new Object[pvs.length * 2];
				System.arraycopy(pvs,0,vs,0,pvs.length);
			}
		}
		ks[size]=key;
		vs[size++ ]=value;
		return null;
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		for (String s : m.keySet()) {
			put(s, m.get(s));
		}
	}

	public void putAll(ParamMap m) {
		int size = m.size;
		String[] ks = m.ks;
		Object[] vs = m.vs;
		for (int i = size; --i >= 0;) {
			put(ks[i], vs[i]);
		}
	}

	public int size() {
		return size;
	}

	// 闄勫姞鐨勮幏鍙栨暟鍊肩殑鏂规硶=================================================

	public String getString(String key, String def) {
		Object r = get(key);
		if (r == null) {
			return def;
		}
		return r.toString();
	}

	public String getString(String key) {
		Object r = get(key);
		if (r == null) {
			throw new RuntimeException("not found key:" + key);
		}
		return r.toString();
	}

	public int getInt(String key) {
		Object r = get(key);
		if (r == null) {
			throw new RuntimeException("not found key:" + key);
		}
		if (r instanceof Number) {
			return ((Number) r).intValue();
		}
		return Integer.parseInt(r.toString());
	}

	public int getInt(String key, int def) {
		Object r = get(key);
		if (r instanceof Number) {
			return ((Number) r).intValue();
		}
		return def;
	}

	public long getLong(String key, long def) {
		Object r = get(key);
		if (r instanceof Number) {
			return ((Number) r).longValue();
		}
		return def;
	}

	public long getLong(String key) {
		Object r = get(key);
		if (r == null) {
			throw new RuntimeException("not found key:" + key);
		}
		if (r instanceof Number) {
			return ((Number) r).longValue();
		}
		return Long.parseLong(r.toString());
	}

	public String[] getStringArray(String key) {
		Object o = get(key);
		if (o == null) {
			throw new ParamNotFoundException(key);
		}
		if (!(o instanceof Object[])) {
			throw new ParamNotFoundException(key, "String[]");
		}
		Object[] os = (Object[]) o;
		String[] rs = new String[os.length];
		for (int i = os.length; --i >= 0;) {
			if (os[i] instanceof String) {
				rs[i] = (String) os[i];
			} else {
				throw new ParamNotFoundException(key, "String[]");
			}
		}
		return rs;
	}

	public String[] getStringArray(String key, String[] def) {
		Object o = get(key);
		if (!(o instanceof Object[])) {
			return def;
		}
		Object[] os = (Object[]) o;
		String[] rs = new String[os.length];
		for (int i = os.length; --i >= 0;) {
			if (os[i] instanceof String) {
				rs[i] = (String) os[i];
			} else {
				return def;
			}
		}
		return rs;
	}
	
	public static final Parcelable.Creator<ParamMap> CREATOR = new Parcelable.Creator<ParamMap>() {
		public ParamMap createFromParcel(Parcel in) {
			return new ParamMap(in);
		}

		public ParamMap[] newArray(int size) {
			return new ParamMap[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeInt(size);
//		dest.writeStringArray(ks);
//		dest.writeArray(vs);
		dest.writeInt(size);
		if (size > 0) {
			String[] ksCopyStrings = new String[size];
			Object[] vsCopyObjects = new Object[size];
			for(int i = 0;i < size;i++ ) {
				ksCopyStrings[i] = ks[i];
				vsCopyObjects[i] = vs[i];
			}
			dest.writeStringArray(ksCopyStrings);
			dest.writeArray(vsCopyObjects);
		}
	}

	private ParamMap(Parcel in) {
		size = in.readInt();
		if (ks==null) {
			ks =  new String[size];
		}
		if (vs==null) {
			vs =  new Object[size];
		}
		in.readStringArray(ks);
		vs = in.readArray(this.getClass().getClassLoader());
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
