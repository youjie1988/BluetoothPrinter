package cn.shellinfo.wall.remote;

import java.util.ArrayList;

/**
 * 鎵嬫満杞欢寮�鍙戝钩鍙� - 缃戠粶閫氳妯″潡 閫氳杩斿洖淇℃伅鍖呰９绫伙紝鎵�浠ュ鎴风鍙戣捣鐨勯�氳璇锋眰閮藉皢杩斿洖璇ュ璞�
 */
public class CommResponse {
	/**
	 * 姝ｅ父
	 */
	public static final int RC_OK = 0;
	/**
	 * 鏈煡閿欒
	 */
	public static final int RC_ERR = 1;
	/**
	 * 缃戠粶閿欒
	 */
	public static final int RC_NET_ERR = 2;
	/**
	 * 鏁版嵁鏍煎紡閿欒
	 */
	public static final int RC_DATA_FORMAT_ERR = 3;
	/**
	 * 鍐欐暟鎹嚭閿�
	 */
	public static final int RC_WRITE_DATA_ERR = 4;
	/**
	 * 鍙栨秷
	 */
	public static final int RC_CANCEL = 5;

	/**
	 * 鍔犲瘑閿欒
	 */
	public static final int RC_ENCRYPTING = 6;

	/**
	 * 瑙ｅ帇閿欒
	 */
	public static final int RC_UNZIP = 7;
	
	public static final int RC_PROCESSOR_NOT_FOUND = 8;
	
	private final static String[] ERR_INFO={"=unknow error=","=net error=","=data format error=","=write error=","=cancel=","鍔犲瘑閿欒","瑙ｅ帇閿欒","=processor not found="};

	CommResponse(int resultCode) {
		this.resultCode = resultCode;
		if(resultCode>=0 && resultCode<ERR_INFO.length){
			errorMessage=ERR_INFO[resultCode+1];
		}
	}

	/**
	 * 杩斿洖鐮侊紝1000浠ュ唴鐨勮繑鍥炵爜鐢辩郴缁熷畾涔夛紝鐢ㄦ埛瀹氫箟鐨勮繑鍥炵爜蹇呴』澶т簬1000
	 */
	public int resultCode;

	/**
	 * 寮傚父娑堟伅鎻忚堪.鍙湁褰搑esultCode!=0鏃舵墠鏄湁鏁堝湴
	 */
	public String errorMessage;

	/**
	 * 杩斿洖缁撴灉鏁版嵁瀵硅薄,鍙湁褰搑esultCode==0鏄椂,鎵嶆槸鏈夋晥鍦�
	 */
	public Object data;

	public Object getData() {
		return data;
	}

	/**
	 * 鍙栧緱缁撴灉浠ｇ爜,0琛ㄧず姝ｅ父, 鍏跺畠鍊艰〃绀洪敊璇�
	 * 
	 * @return
	 */
	public int getResultCode() {
		return resultCode;
	}

	/**
	 * 鍙栧緱閿欒淇℃伅锛孯esultCode闈�0鏃舵湁鏁堛��
	 * 
	 * @return
	 */
	public String getErrMsg() {
		return errorMessage;
	}

	public void writeTo(LEDataOutputStream dos) throws Exception {
		if (resultCode != 0) {// 鍑洪敊浜�
			dos.writeShort(1);
			dos.writeInt(resultCode);
			dos.writeObject(errorMessage);
		}
		else {// 涓�鍒囨甯�
			dos.writeShort(0);
			dos.writeObject(data);
		}
		if (methods != null) {
			dos.writeShort(methods.size());
			for (CallBackMethod mi : methods) {
				dos.writeInt(mi.name.hashCode());
				dos.writeObject(mi.param);
			}
		}
		else {
			dos.writeShort(0);
		}
	}

	public CommResponse() {
	}

	/**
	 * 鏋勫缓涓�涓嚭閿欐椂鐨勮繑鍥炵粨鏋�
	 * 
	 * @param errid
	 *            閿欒ID,蹇呴』>=1000(灏忎簬1000鐨処D鐢辩郴缁熶娇鐢�)
	 * @param errMsg
	 *            閿欒娑堟伅
	 * @return
	 */
	public static CommResponse makeErr(int errid, String errMsg) {
		CommResponse r = new CommResponse();
		r.resultCode = errid;
		r.errorMessage = errMsg;
		return r;
	}
	
	public static CommResponse makeErr(int errid) {
		CommResponse r = new CommResponse(errid);
		return r;
	}

	/**
	 * 鏋勫缓涓�涓甯稿洖搴�
	 * 
	 * @param data
	 *            杩斿洖鐨勬暟鎹�,娉ㄦ剰:杩欓噷鍙敮鎸佺畝鍗曠殑鏁版嵁绫诲瀷: 鍖呮嫭鍩烘湰鏁版嵁绫诲瀷鐨勬暟缁�,鍩烘湰鏁版嵁绫诲瀷鐨勫寘瑁呯被
	 * @return
	 */
	public static CommResponse makeOK(Object data) {
		CommResponse r = new CommResponse();
		r.data = data;
		return r;
	}

	private ArrayList<CallBackMethod> methods;
	
	public void setClientMethodList(ArrayList<CallBackMethod> list){
		methods=list;
	}

	public void callClientMethod(String name, Object[] param) {
		if (methods == null) {
			methods = new ArrayList<CallBackMethod>();
		}
		CallBackMethod info = new CallBackMethod(name, param);
		methods.add(info);
	}
	
	
	public String toString(){
		return "code:"+resultCode+" errMsg"+errorMessage+" data:"+data;
	}

}
