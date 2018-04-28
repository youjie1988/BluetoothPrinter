package cn.shellinfo.wall.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.shellinfo.wall.local.ShareDataLocal;

public class RemoteAsyncTask extends AsyncTask<ParamMap, String, ParamMap> {
	private final ParamMap tempParamMap = new ParamMap();
	public static final String ERROR_KEY = "_____error";
	public static final String ERROR_KEY_IS_HTML = "_____error_is_html";
	private ConnClient c = null;
	private Context context = null;
	private String processorName = null;
	private boolean getHttp = true;

	/**
	 * @param processorName
	 *            the processorName to set
	 */
	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	private TaskFinishedListener listener = null;

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(TaskFinishedListener listener) {
		this.listener = listener;
	}

	public interface TaskFinishedListener {
		/**
		 * 
		 * @return true:缁х画鍙栫綉缁滆祫婧�;false:涓嶅仛鎿嶄綔
		 */
		public boolean onPre();

		public void onFinished(ParamMap param);

		public void onError(String err);
	}

	public RemoteAsyncTask(Context context, String processorName, TaskFinishedListener listener) {
		try {
			c = new ConnClient(getServerUrl(context));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(StringConfig.initialize_connection_error);
		}
		this.context = context;
		this.listener = listener;
		this.processorName = processorName;
	}

	public String getServerUrl(Context context) {
		return ShareDataLocal.getInstance(context).getServerUrl();
	}

	public ConnClient getConnClient() {
		return c;
	}

	@Override
	protected ParamMap doInBackground(ParamMap... arg0) {
		if (getHttp)
			return getResult(arg0[0]);
		return null;
	}

	public ParamMap wrapParam(ParamMap param) {
		return param;
	}

	/**
	 * 瑙ｆ瀽param
	 * 
	 * @param param
	 * @return
	 */
	public ParamMap decryptParam(ParamMap param) {

		return param;
	}

	/**
	 * 鑾峰緱缁撴灉
	 * 
	 * @param param
	 * @return
	 */
	public ParamMap getResult(ParamMap param) {
		if (c == null) {
			tempParamMap.put(ERROR_KEY,StringConfig.get_server_list_error);
			return tempParamMap;
		}

		if (!isConnect(context)) {
			String err = StringConfig.unable_network_need_check_set;
			tempParamMap.put(ERROR_KEY, err);
			return tempParamMap;
		}

		ParamMap send = wrapParam(param);
		try {
			fetchConnectionTimeout();
			CommResponse resp = c.sendData(processorName, send);
			if (resp.errorMessage == null || resp.errorMessage.length() == 0) {
				ParamMap map = (ParamMap) resp.data;
				map = decryptParam(map);
				return map;
			}
			return procBusinessException(resp.errorMessage);

		} catch (IOException ioex) {
			return procIOException(ioex);
		} catch (WallRemoteException e) {
			e.printStackTrace();
			return procWallRemoteException(e);
		}
	}

	public ParamMap procBusinessException(String err) {
		tempParamMap.put(ERROR_KEY, err);
		return tempParamMap;
	}

	public ParamMap procIOException(IOException ioex) {
		ioex.printStackTrace();
		String err = StringConfig.network_error;
		if (!isConnect(context)) {
			err = StringConfig.unable_network_need_check_set;
		}
		tempParamMap.put(ERROR_KEY, err);
		return tempParamMap;
	}

	public ParamMap procWallRemoteException(WallRemoteException e) {
		String msg = e.getMessage();
		if (msg == null) {
			msg = "message is null";
		}
		tempParamMap.put(ERROR_KEY, msg);
		if (e.errdata != null) {
			try {
				String content = new String(e.errdata);
				if (content.length() > 0) {
					content = content.toLowerCase();
					boolean isHtml = content.contains("<!DOCTYPE html>") || content.contains("<html>");
					if (isHtml)
						tempParamMap.put(ERROR_KEY_IS_HTML, "true");
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			saveErrData(e.errdata);
		}
		return tempParamMap;
	}

	/**
	 * 鎶婃棩鏈熻浆鎹负string绫诲瀷
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String getDateStr(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		if (date == null) {
			return "";
		} else {
			return dateFormat.format(date);
		}
	}

	/**
	 * 灏嗘湭澶勭悊鎴愬姛鐨勪粠鏈嶅姟绔繑鍥炵殑鏁版嵁淇濆瓨鍦╯dcard涓�
	 * 
	 * @param data
	 */
	private void saveErrData(byte[] data) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log/";
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		if (dirFile.exists()) {
			String fileName = dir + "remote_" + getDateStr(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".log";
			File logFile = new File(fileName);
			FileOutputStream fos = null;
			try {
				logFile.createNewFile();
				fos = new FileOutputStream(logFile);
				fos.write(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	protected void onPreExecute() {
		if (listener != null) {
			getHttp = listener.onPre();
		}
	}

	@Override
	protected void onPostExecute(ParamMap result) {
		procResult(result);
	}

	public void procResult(ParamMap result) {
		if (result != null) {
			String errorMsg = null;
			try {
				errorMsg = result.containsKey(ERROR_KEY) ? result.getString(ERROR_KEY) : null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errorMsg = StringConfig.unknown_error;
			}
			if (errorMsg == null) {
				// 鎴愬姛
				if (listener != null) {
					listener.onFinished(result);
				}
			} else {
				boolean isHtml = result.containsKey(ERROR_KEY_IS_HTML);
				Log.d("CommAsyncTask:" + processorName, errorMsg+"|isHtml:"+isHtml);
				if (isHtml) {
					isHtml=onHtmlError();
				}
				if (!isHtml&&listener != null) {
					listener.onError(errorMsg);
				}
			}
		}
	}

	/**
	 * 褰撹繑鍥炵殑鏁版嵁鏄竴涓猦tml椤甸潰鏃剁殑澶勭悊锛岃繑鍥瀟rue琛ㄧず涓嶅笇鏈涚户缁皟鐢╨istener.onError鍑芥暟锛屽弽涔嬩細缁х画璋冪敤listener.
	 * onError
	 * 
	 * @return
	 */
	public boolean onHtmlError() {
		return false;
	}

	@Override
	protected void onCancelled() {
		if (listener != null) {
			listener.onError("Cancelled Task");
		}
	}

	public boolean isConnect(Context context) {

		// 鑾峰彇鎵嬫満鎵�鏈夎繛鎺ョ鐞嗗璞★紙鍖呮嫭瀵箇i-fi,net绛夎繛鎺ョ殑绠＄悊锛�
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				// 鑾峰彇缃戠粶杩炴帴绠＄悊鐨勫璞�
				NetworkInfo info = connectivity.getActiveNetworkInfo();

				if (info != null && info.isConnected()) {
					// 鍒ゆ柇褰撳墠缃戠粶鏄惁宸茬粡杩炴帴
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						// boolean isok = checkNetworkSucess();
						// return isok;
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("mcn", e.toString());
		}
		return false;
	}

	public void fetchConnectionTimeout() {
		int time = 15000;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		IoUtil.proxyType = IoUtil.PROXY_TYPE_NONE;
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				time = 15000;
			} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				String currentAPN = info.getExtraInfo();
				if (currentAPN != null) {
					currentAPN = currentAPN.trim().toLowerCase();
					if (currentAPN.equals("cmwap") || currentAPN.equals("uinwap")) {// 绉诲姩銆佽仈閫歸ap涓婄綉
						IoUtil.proxyType = IoUtil.PROXY_TYPE_CMWAP;
					} else if (currentAPN.equals("ctwap")) {// 鐢典俊wap涓婄綉
						IoUtil.proxyType = IoUtil.PROXY_TYPE_CTWAP;
					}
				}
				if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS
						|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA
						|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE) {// 2g
					time = 30000;
				} else {// 3g
					time = 20000;
				}
			}
		}
		IoUtil.connectionTimeout = time;
	}
}
