package cn.shellinfo.wall.remote;

public class WallRemoteException extends RuntimeException {
	public byte[] errdata;
	public WallRemoteException(String message){
		super(message);
	}
	
	public WallRemoteException(String msg,byte[] errData){
		super(msg);
		this.errdata=errData;
	}
}
