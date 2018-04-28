package cn.shellinfo.wall.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * <p>
 * Title: HouJUtil
 * </p>
 * 
 * @author houj
 * @version 1.0
 */
public final class IoUtil {
	private IoUtil() {
	}

	private final static Pattern CHARSET = Pattern.compile("(?i)charset\\s*?=[\" ]*(.*?)[/\">]");
	private final static Pattern ENCODING = Pattern.compile("(?i)encoding.*?=[\"]?(.*?)[/\">]");

	private static String getEncoding(byte[] data) {
		String r = getEncoding(data, 2000);
		// System.out.println(r);
		return r;
	}

	private final static Pattern PFRE = Pattern
			.compile("(?s)(?i)http-equiv=\"refresh\"[^>]*?url=\\s*?[\"]?([^> \"]+)[> \"]");
	private final static Pattern IFRAME = Pattern.compile("(?s)(?i)<iframe[^>]*?src\\s*?=\\s*?[\'\"]?([^> \"]+)[> \"]");

	private static String getEncoding(byte[] data, int len) {
		int end = data.length < len ? data.length : len;
		StringBuilder sb = new StringBuilder(end);
		for (int i = 0; i < end; i++) {
			int b = data[i] & 0xff;
			if (b < 0x80) {
				sb.append((char) b);
			}
		}
		Matcher m = CHARSET.matcher(sb);
		if (m.find()) {
			return m.group(1).trim();
		} else {
			m = ENCODING.matcher(sb);
			if (m.find()) {
				return m.group(1).trim();
			}
		}
		return null;
	}

	private static String getHostStr(URL u) {
		String h = u.getHost();
		int port = u.getPort();
		if (port != 80 && port > 0) {
			return h + ":" + port;
		}
		return h;
	}

	private static String getCookie(HttpURLConnection h) {
		String s = h.getHeaderField("Set-Cookie");
		if (s == null) {
			return null;
		}

		int i = s.indexOf(';');
		String r = i >= 0 ? s.substring(0, i) : s;
//		System.out.println(r);
		return r;
	}

	private static String getString_(String url, int n, String cookie, String defEncodeing) throws Exception {
		// System.out.println(url);
		if (n <= 0) {
			throw new RuntimeException(StringConfig.no_access_to_content);
		}
		if (!url.startsWith("http://")) {
			url = "http://".concat(url);
		}
		URL u = new URL(url);
		HttpURLConnection http = (HttpURLConnection) u.openConnection();
		http.setConnectTimeout(connectionTimeout);
		http.setReadTimeout(readTimeout);
		String host = getHostStr(u);
		// System.out.println(host);
		http.setRequestProperty("Host", host);

		http.setRequestProperty("User-Agent", "Opera9.50(windows 2000)");
		http.setRequestProperty("Referer", url);

		if (cookie != null) {
			http.setRequestProperty("Cookie", cookie);
		}

		int code = http.getResponseCode();
		if (code >= 300 && code < 400) {
			String newUrl = http.getHeaderField("Location");
			return getString_(newUrl, n - 1, getCookie(http), defEncodeing);
		}

		// for (int i = 0; i < 10; i++) {
		// System.out.println(http.getHeaderFieldKey(i) + ":" +
		// http.getHeaderField(i));
		// }

		String encoding = null;

		String s = http.getContentType();
		if (s != null) {
			s = s.toLowerCase();
			int i = s.indexOf("charset=");
			if (i >= 0) {
				encoding = s.substring(i + "charset=".length());
			}
		}

		InputStream ins = http.getInputStream();
		int len = http.getContentLength();
		byte[] data = null;
		try {
			data = read(ins, len);
		} finally {
			ins.close();
		}

		// FileUtil.writeFile(new File("d:/aa.zip"), data);
		if (data == null) {
			return null;
		}
		if ("deflate".equals(http.getContentEncoding())) {
			System.out.println(data.length);
			try {
				data = deflate(data);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println(data.length);
		}
		String tencoding = getEncoding(data);
		if (tencoding != null) {
			encoding = tencoding;
		}
		if (encoding == null || encoding.length() <= 0) {
			encoding = defEncodeing;
		}
		if (encoding == null || encoding.length() <= 0 || encoding.toUpperCase().startsWith("GB")) {
			encoding = "GBK";
		}
		String r = null;
		try {
			r = new String(data, encoding);
		} catch (UnsupportedEncodingException ue) {
			r = new String(data, defEncodeing);
		}
		// if (r.length() < 3000) {
		// Matcher m = PFRE.matcher(r);
		// if (m.find()) {
		// String tu = m.group(1);
		// if (tu.length() > 5) {
		// tu = tu.trim();
		// return getString_(getUrl(tu, url), n - 1, getCookie(http),
		// defEncodeing);
		// }
		// }
		// m = IFRAME.matcher(r);
		// if (m.find()) {
		// String tu = m.group(1);
		// if (tu.length() > 5) {
		// tu = tu.trim();
		// return getString_(getUrl(tu, url), n - 1, getCookie(http),
		// defEncodeing);
		// }
		// }
		// }
		return r;
	}

	private static byte[] deflate(byte[] data) throws IOException {
		ByteArrayInputStream ins = new ByteArrayInputStream(data);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InflaterInputStream di = new InflaterInputStream(ins);
		try {
			byte[] buf = new byte[1024];
			int n = 0;
			while ((n = di.read(buf)) > 0) {
				os.write(buf, 0, n);
			}
			return os.toByteArray();
		} finally {
			di.close();
		}
	}

	public static String getUrl(String u, String base) {
		if (u.startsWith("javascript:")) {
			return u;
		}
		u = u.trim();
		if (u.startsWith("http://")) {
			return u;
		}
		if (u.startsWith("/")) {
			int i = base.indexOf('/', 7);
			if (i < 0) {
				return base.concat(u);
			}
			return base.substring(0, i).concat(u);
		} else {
			int i = base.lastIndexOf('/');
			if (i < 7) {
				return base + "/" + u;
			} else {
				return base.substring(0, i + 1).concat(u);
			}
		}
	}

	public static byte[] getBytesByURL(URL u) {
		InputStream is = null;
		try {
			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setConnectTimeout(connectionTimeout);
			uc.setReadTimeout(readTimeout);
			uc.setRequestProperty("User-Agent", "Opera9.50(windows 2000)");
			uc.setRequestProperty("Referer", u.toString());
			// System.out.println(uc.getContentType());
			// System.out.println(uc.getContentLength());
			// uc.connect();
			is = uc.getInputStream();
			return read(is, uc.getContentLength());
		} catch (Exception ex) {
			// System.err.println(ex.getMessage());
		}
		return null;
	}

	public static byte[] getBytesByURL(URL u, byte[] data) throws IOException {
		final byte[] dataBytes = data;
		InputStream is = null;
		HttpClient httpClient = null;
		try {
			HttpPost post = new HttpPost(u.toURI());
			post.setEntity(new ByteArrayEntity(dataBytes));
			httpClient = new DefaultHttpClient();
			HttpParams params = post.getParams();
			if (proxyType != PROXY_TYPE_NONE) {
				String poxyStr=proxyList[proxyType].address().toString().substring(1);
				HttpHost httpHost = new HttpHost(poxyStr);
				params.setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
			}
			post.setHeader("Accept-Encoding", "identity");//璁剧疆涓嶅帇缂�,鍥犱负濡傛灉鍘嬬缉锛宑twap涓嬭繑鍥炵殑contentlength鍜屽疄闄呮暟鎹暱搴︿笉涓�鑷�
			params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			HttpConnectionParams.setSoTimeout(params, readTimeout);// 
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);// 杩炴帴瓒呮椂
			post.setParams(params);
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				return read(is, (int) entity.getContentLength());
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			throw ioex;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static final Proxy[] proxyList = new Proxy[] {
			new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.172", 80)),
			new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80)) };
	public static int readTimeout = 60*1000;//璇诲彇瓒呮椂鏃堕棿1鍒嗛挓
	public static int connectionTimeout = 30*1000;//杩炴帴瓒呮椂鏃堕棿30绉�
	public static final int PROXY_TYPE_NONE = -1;// 涓嶄娇鐢ㄤ唬鐞�
	public static final int PROXY_TYPE_CMWAP = 0;// 浣跨敤cmwap浠ｇ悊(绉诲姩)
	public static final int PROXY_TYPE_CTWAP = 1;// 浣跨敤ctwap浠ｇ悊(鐢典俊)
	public static int proxyType = PROXY_TYPE_NONE;// 浣跨敤浠ｇ悊绫诲瀷

	/**
	 * http://developer.android.com/reference/java/net/HttpURLConnection.html
	 */
	public static byte[] getBytesByURL(URL u, byte[] data, String referer) {
		InputStream is = null;
		HttpURLConnection uc = null;
		try {
			if (proxyType != PROXY_TYPE_NONE) {
				uc = (HttpURLConnection) u.openConnection(proxyList[proxyType]);
			} else {
				uc = (HttpURLConnection) u.openConnection();
			}
			uc.setRequestProperty("Accept-Encoding", "identity");//璁剧疆涓嶅帇缂�,鍥犱负濡傛灉鍘嬬缉锛宑twap涓嬭繑鍥炵殑contentlength鍜屽疄闄呮暟鎹暱搴︿笉涓�鑷�
			uc.setRequestMethod("POST");
			uc.setConnectTimeout(connectionTimeout);
			uc.setReadTimeout(readTimeout);
			uc.setDoInput(true);
			uc.setRequestProperty("accept", "*/*");
			uc.setUseCaches(false);// post璇锋眰涓嶈兘浣跨敤缂撳瓨.
			uc.setInstanceFollowRedirects(true);// 鏄惁鑷姩閲嶅畾鍚�.
			// Properties prop = System.getProperties();
			// prop.setProperty("http.proxyHost", "192.168.1.134");
			// 璁剧疆http璁块棶瑕佷娇鐢ㄧ殑浠ｇ悊鏈嶅姟鍣ㄧ殑绔彛
			// prop.setProperty("http.proxyPort", "8888");

			if (referer != null)
				uc.setRequestProperty("Referer", referer);

			if (data != null) {
				uc.setDoOutput(true);
				OutputStream os = uc.getOutputStream();
				os.write(data);
			}
			is = uc.getInputStream();
			return read(is, uc.getContentLength());
		} catch (Exception ex) {
			// li.add("getBytesByURL:>"+ex.getClass()+">>"+ex.getMessage());
			// ex.printStackTrace();
		} finally {
			if (uc != null)
				uc.disconnect();
		}
		return null;
	}

	public static byte[] getBytesByURL(String url, byte[] data, int tryCount) {
		for (int i = 0; i < tryCount; i++) {
			byte[] r = getBytesByURL(url, data);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	public static String getConInput() {
		byte[] buf = new byte[1024];
		int n;
		try {
			n = System.in.read(buf, 0, buf.length);
			return new String(buf, 0, n);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] read(InputStream ins) {
		return read(ins, -1);
	}

	public static byte[] read(InputStream ins, int len) {
		if (ins == null) {
			return null;
		}
		if (len == 0) {
			// li.add("getBytesByURL:>read len==0");
			return new byte[0];
		}
		if (len < 0) {
			byte[] buf = new byte[16384];
			int n = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
			try {
				while ((n = ins.read(buf)) > 0) {
					baos.write(buf, 0, n);
				}
				return baos.toByteArray();
			} catch (Exception ex) {
				// li.add("getBytesByURL:>"+ex.getClass()+">>"+ex.getMessage());
				// ex.printStackTrace();
			} finally {
				try {
					ins.close();
				} catch (IOException ex1) {
				}
			}
		} else {
			byte[] buf = new byte[len];
			int n = 0;
			int all = 0;
			try {
				while ((n = ins.read(buf, all, len - all)) > 0) {
					all += n;
				}
				return buf;
			} catch (Exception ex) {
				// li.add("getBytesByURL:>"+ex.getClass()+">>"+ex.getMessage());
				// ex.printStackTrace();
			} finally {
				try {
					ins.close();
				} catch (IOException ex1) {
				}
			}
		}
		return null;
	}

	public static byte[] getBytesByURL(String url, int tryCount) {
		for (int i = 0; i < tryCount; i++) {
			byte[] r = getBytesByURL(url);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	public static byte[] getBytesByURL(String url) {
		if (url == null || url.length() < 8) {
			return null;
		}
		try {
			URL u = new URL(url);
			return getBytesByURL(u);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return null;
	}

	// public static LastInfo li = new LastInfo(100);
	public static byte[] getBytesByURL(String url, byte[] data) {
		// li.add("getBytesByURL:>"+url);
		if (url == null || url.length() < 8) {
			return null;
		}
		try {
			URL u = new URL(url);
			return getBytesByURL(u, data);
		} catch (Exception ex) {
			// li.add("getBytesByURL:>"+ex.getClass()+">>"+ex.getMessage());
			// ex.printStackTrace();
		}
		return null;
	}

	public static byte[] getDataByClass(Class<?> c, String name) {
		InputStream ins = c.getResourceAsStream(name);
		return read(ins);
	}

	public static String getStringByURL(String url, String defCharset) {
		return getStringByURL(url, defCharset, 1);
	}

	private final static char[] CS = "0123456789ABCDEF".toCharArray();

	private static String preUrl(String src) {
		StringBuilder sb = new StringBuilder();
		try {
			byte[] data = src.getBytes("GBK");
			for (int n : data) {
				if (n <= ' ' || n >= 0x80) {
					sb.append('%');
					sb.append(CS[(n >> 4) & 0xF]);
					sb.append(CS[n & 0xF]);
				} else {
					sb.append((char) n);
				}
			}
			return sb.toString();
		} catch (Exception e) {
		}
		return src;
	}

	public static String getStringByURL(String url, String defCharset, int tryCount) {
		url = preUrl(url);
		for (int i = 0; i < tryCount; i++) {
			try {
				String r = getString_(url, 3, null, defCharset);
				if (r != null) {
					return r;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public static String readString(InputStream ins, String code) {
		byte[] data = read(ins, -1);
		if (data == null) {
			return null;
		}
		if (code == null) {
			return new String(data);
		}
		try {
			return new String(data, 0, data.length, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int pipe(InputStream ins, OutputStream os, int bufSize) {
		int n = 0;
		byte[] buf = new byte[bufSize];
		int r = 0;
		try {
			while ((n = ins.read(buf)) > 0) {
				os.write(buf, 0, n);
				r += n;
			}
		} catch (IOException ex) {
			
		}
		return r;
	}

	public static int pipe(Reader ins, Writer os, int bufSize) {
		int n = 0;
		char[] buf = new char[bufSize];
		int r = 0;
		try {
			while ((n = ins.read(buf)) > 0) {
				os.write(buf, 0, n);
				r += n;
			}
		} catch (IOException ex) {
		}
		return r;
	}

	public static String getInput() {
		byte[] buf = new byte[16384];
		try {
			int n = System.in.read(buf);
			return new String(buf, 0, n);
		} catch (IOException ex) {
			return "";
		}
	}

	public static String getClassRootDir(Class<?> c) {
		String name = c.getName();
		int i = name.lastIndexOf('.');
		if (i >= 0) {
			name = name.substring(i + 1);
		}

		URL u = c.getResource(name + ".class");
		String r = u.toString();
		r = decodeUTF8(r);
		if (r.startsWith("jar:file:")) {
			int end = r.indexOf('!');
			if (end < 0) {
				end = r.length();
			}
			return r.substring("jar:file:".length(), end);
		} else if (r.startsWith("file:")) {
			String tn = c.getName().replace('.', '/') + ".class";
			r = r.replace('\\', '/');
			if (r.endsWith(tn)) {
				return r.substring("file:".length(), r.length() - tn.length() - 1);
			}
			return r.substring("file:".length());
		}
		return r;
	}

	public final static String decodeUTF8(String s) {
		int len = s.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '%') {
				baos.write(Integer.parseInt(s.substring(i + 1, i + 3), 16));
				i += 2;
			} else {
				baos.write(c);
			}
		}
		try {
			return new String(baos.toByteArray(), "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean isFileStyleProtocol(final URL url) {
		String s = url.getProtocol();
		if ("http".equals(s) || "https".equals(s) || "ftp".equals(s) || "file".equals(s) || "jar".equals(s)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		// byte[] data = null;
		// data =
		// IoUtil.getBytesByURL("http://news.workercn.cn/onlinepaper/2009_12/08/GR0101.htm",
		// 3);
		// System.out.println(data.length);
		System.out.println(IoUtil.getStringByURL("http://wap.s1979.com/bbs/", "UTF-8"));
	}

}
