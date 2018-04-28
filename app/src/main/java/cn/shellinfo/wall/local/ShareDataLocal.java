package cn.shellinfo.wall.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class ShareDataLocal {
	private static ShareDataLocal cm = null;

	public static ShareDataLocal getInstance(Context context) {
		if (cm == null) {
			cm = new ShareDataLocal(context);
		}
		return cm;
	}

	public ShareDataLocal() {

	}

	private Context context;
	private String name;
	private int mode;

	public ShareDataLocal(Context context) {
		String packageName = context.getApplicationInfo().packageName;
		name = String.format("sd_%s", packageName);
		mode = Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS;
		this.context = context;
	}

	public SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(name, mode);
	}

	public Editor getEditor() {
		return getSharedPreferences().edit();
	}

	public boolean setStringValue(String key, String value) {
		Editor editor=getEditor();
		if (value == null) {
			editor.remove(key);
		} else {
			editor.putString(key, value);
		}
		return editor.commit();
	}

	public String getStringValue(String key, String def) {
		return getSharedPreferences().getString(key, def);
	}

	public boolean setBytesValue(String key, byte[] value) {
		Editor editor=getEditor();
		if (value == null) {
			editor.remove(key);
		} else {
			String strList = new String(Base64.encode(value, Base64.DEFAULT));
			editor.putString(key, strList);
		}
		return editor.commit();
	}

	public byte[] getBytesValue(String key) {
		String value = getSharedPreferences().getString(key, null);
		if (value != null) {
			return Base64.decode(value.getBytes(), Base64.DEFAULT);
		}
		return null;
	}

	public void setIntValue(String key, int value) {
		Editor editor = getEditor();
		editor.putInt(key, value);
		editor.commit();
	}

	public int getIntValue(String key, int def) {
		return getSharedPreferences().getInt(key, def);
	}

	public void setLongValue(String key, long value) {
		Editor editor = getEditor();
		editor.putLong(key, value);
		editor.commit();
	}

	public long getLongValue(String key, long def) {
		return getSharedPreferences().getLong(key, def);
	}

	public boolean removeValue(String key) {
		Editor editor = getEditor();
		editor.remove(key);
		return editor.commit();
	}

	public void clear() {
		Editor editor = getEditor();
		editor.clear();
		editor.commit();
	}

	public void setBooleanValue(String key, boolean value) {
		Editor editor = getEditor();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key, defaultValue);
	}

	public void register(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	public void unregister(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	private String serverUrl = null;// 杩炴帴鏈嶅姟鍣ㄥ湴鍧�
	private String serverAddrStr = null;// 杩炴帴鏈嶅姟鍣╥d鍦板潃
	private int serverDestPort = 82;// 杩炴帴鏈嶅姟鍣ㄧ鍙ｅ湴鍧�
	private boolean testServer = false;

	public boolean isTestServer() {
		return testServer;
	}

	public void initServerURL() {
		if (serverUrl == null || serverAddrStr == null || serverDestPort == 0) {
			testServer = getBooleanValue(KEY_TEST);
		} else {
			testServer = serverUrl.contains(testTag);
			setBooleanValue(KEY_TEST, testServer);
		}

		if (this.testServer) {
			serverUrl = String.format(format_server, defaultTestServer);
			serverAddrStr = defaultTestServerAddress;
			serverDestPort = defaultTestServerPort;
		} else {
			serverUrl = String.format(format_server, defaultOfficialServer);
			serverAddrStr = defaultOfficialServerAddress;
			serverDestPort = defaultOfficialServerPort;
		}
	}

	public String getServerUrl() {
		if (this.serverUrl == null) {
			defaultOfficialServer = getStringValue("defaultOfficialServer", defaultOfficialServer);
			defaultTestServer = getStringValue("defaultTestServer", defaultTestServer);

			initServerURL();
		}
		return this.serverUrl;
	}

	public String getServerIpAddress() {
		if (this.serverAddrStr == null) {
			defaultOfficialServerAddress = getStringValue("defaultOfficialServerAddress", defaultOfficialServerAddress);
			defaultTestServerAddress = getStringValue("defaultTestServerAddress", defaultTestServerAddress);

			initServerURL();
		}
		return this.serverAddrStr;
	}

	public int getServerDestPort() {
		if (this.serverDestPort == 0) {
			defaultOfficialServerPort = getIntValue("defaultOfficialServerPort", defaultOfficialServerPort);

			defaultTestServerPort = getIntValue("defaultTestServerPort", defaultTestServerPort);

			initServerURL();
		}
		return this.serverDestPort;
	}

	public void changeServer() {
		changeServer(!testServer);
	}

	/**
	 * 寮哄埗璁剧疆鏈嶅姟绔湴鍧�绫诲瀷
	 * 
	 * @param tag
	 *            true:涓烘祴璇曠幆澧� false锛氫负姝ｅ紡鐜
	 */
	public void changeServer(boolean tag) {
		this.testServer = tag;
		if (this.testServer) {
			serverUrl = String.format(format_server, defaultTestServer);
			serverAddrStr = defaultTestServerAddress;
			serverDestPort = defaultTestServerPort;
		} else {
			serverUrl = String.format(format_server, defaultOfficialServer);
			serverAddrStr = defaultOfficialServerAddress;
			serverDestPort = defaultOfficialServerPort;
		}
		setBooleanValue(KEY_TEST, testServer);
	}

	private static final String format_server = "%s/comm";
	public static final String KEY_TEST = "testServer";
	private static final String CONFIG_FILENAME = "serverconfig.txt";

	private String defaultOfficialServer = "http://115.28.15.130:8080/PayCenter";

	private String defaultOfficialServerAddress = "comm.weipass.cn";

	private int defaultOfficialServerPort = 82;

	private String defaultTestServer = "http://115.28.15.130:8080/PayCenter";

	private String defaultTestServerAddress = "wkf.oboard.net";

	private int defaultTestServerPort = 82;

	private String testTag = "weipasstest.net";

	/**
	 * 榛樿姝ｅ紡鐜鍦板潃
	 */
	public void setDefaultOfficialServer(String url) {
		this.defaultOfficialServer = url;
		setStringValue("defaultOfficialServer", defaultOfficialServer);
	}

	public String getDefaultOfficialServer() {
		return this.defaultOfficialServer;
	}

	/**
	 * 榛樿姝ｅ紡鐜蹇冭烦ip鎴栬�呭煙鍚嶅湴鍧�
	 */
	public void setDefaultOfficialServerAddress(String ipAddress) {
		this.defaultOfficialServerAddress = ipAddress;
		setStringValue("defaultOfficialServerAddress", defaultOfficialServerAddress);
	}

	public String getDefaultOfficialServerAddress() {
		return this.defaultOfficialServerAddress;
	}

	/**
	 * 榛樿姝ｅ紡鐜蹇冭烦杩炴帴绔彛鍦板潃
	 */
	public void setDefaultOfficialServerPort(int port) {
		this.defaultOfficialServerPort = port;
		setIntValue("defaultOfficialServerPort", defaultOfficialServerPort);
	}

	public int getDefaultOfficialServerPort() {
		return this.defaultOfficialServerPort;
	}

	/**
	 * 榛樿娴嬭瘯鐜鍦板潃
	 */
	public void setDefaultTestServer(String url) {
		defaultTestServer = url;
		setStringValue("defaultTestServer", defaultTestServer);
	}

	public String getDefaultTestServer() {
		return this.defaultTestServer;
	}

	/**
	 * 榛樿娴嬭瘯鐜蹇冭烦ip鎴栬�呭煙鍚嶅湴鍧�
	 */
	public void setDefaultTestServerAddress(String ipAddress) {
		this.defaultTestServerAddress = ipAddress;
		setStringValue("defaultTestServerAddress", defaultTestServerAddress);
	}

	public String getDefaultTestServerAddress() {
		return this.defaultTestServerAddress;
	}

	/**
	 * 榛樿娴嬭瘯鐜蹇冭烦杩炴帴绔彛鍦板潃
	 */
	public void setDefaultTestServerPort(int port) {
		this.defaultTestServerPort = port;
		setIntValue("defaultTestServerPort", defaultTestServerPort);
	}

	public int getDefaultTestServerPort() {
		return this.defaultTestServerPort;
	}

	/**
	 * 娴嬭瘯鐜鍦板潃鏍囪
	 */
	public void setTestTag(String tag) {
		this.testTag = tag;
	}
}
