package cn.shellinfo.wall.remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ConnClient {
	private final URL servURL;

	public boolean isDebug = true;

	public ConnClient(String servURL) throws MalformedURLException {
		// Log.d("servURL", servURL);
		this.servURL = new URL(servURL);
	}

	// public byte[] sendDataNoAna(String processorName, Object data) {
	// LEDataOutputStream dos = new LEDataOutputStream();
	// try {
	// if (isDebug) {
	// dos.writeShort(0);
	// dos.writeString(processorName);
	// } else {
	// dos.writeShort(1);
	// dos.writeInt(processorName.hashCode());
	// }
	// dos.writeObject(data);
	// } catch (Throwable e) {
	// e.printStackTrace();
	// return null;
	// }
	// byte[] bs = dos.toByteArray();
	// bs = IoUtil.getBytesByURL(servURL, bs);
	// return bs;
	// }

	public CommResponse sendData(String processorName, Object data, boolean isUrlConn) throws WallRemoteException,
			IOException {
		LEDataOutputStream dos = new LEDataOutputStream();
		try {
			if (isDebug) {
				dos.writeShort(1);
				dos.writeString(processorName);
			} else {
				dos.writeShort(0);
				dos.writeInt(processorName.hashCode());
			}
			dos.writeObject(data);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new WallRemoteException(StringConfig.network_data_write_error);
			// return CommResponse.makeErr(CommResponse.RC_WRITE_DATA_ERR);
		}
		byte[] bs = dos.toByteArray();
		String emsg = null;
		for (int i = 0; i < 3; i++) {
			if (isUrlConn) {
				bs = IoUtil.getBytesByURL(servURL, bs, null);
			} else {
				bs = IoUtil.getBytesByURL(servURL, bs);
			}
			if (bs == null || bs.length <= 2) {
				emsg=StringConfig.unable_connect_server;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
				// return CommResponse.makeErr(CommResponse.RC_NET_ERR);
			}
			LEDataInputStream dis = new LEDataInputStream(new ByteArrayInputStream(bs));
			try {
				int flag = dis.readShort();
				if ((flag & 1) == 0) {
					data = dis.readObject();
					CommResponse r = new CommResponse();
					r.data = data;
					return r;
				} else {//鏈嶅姟绔姏寮傚父锛岃蛋杩欓噷
					int code = dis.readInt();
					emsg = (String) dis.readObject();
					bs=null;
					break;
					// return CommResponse.makeErr(code, emsg);
				}
			} catch (Throwable t) {
				t.printStackTrace();
				emsg=StringConfig.data_format_error;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
				// return CommResponse.makeErr(CommResponse.RC_DATA_FORMAT_ERR);
			}
		}
		throw new WallRemoteException(emsg,bs);
	}

	public CommResponse sendData(String processorName, Object data) throws WallRemoteException, IOException {
		return sendData(processorName, data, true);
	}
}
