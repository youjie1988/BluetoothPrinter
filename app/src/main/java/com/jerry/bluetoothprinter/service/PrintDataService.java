package com.jerry.bluetoothprinter.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;


import cn.shellinfo.wall.remote.LEDataOutputStream;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.impl.WeiposImpl;

import com.google.zxing.BarcodeFormat;
import com.jerry.bluetoothprinter.action.HEX;
import com.jerry.bluetoothprinter.service.IPrint.Gravity;
import com.jerry.bluetoothprinter.service.lattice.DotFont;
import com.jerry.bluetoothprinter.service.lattice.DotFontGroup;
import com.jerry.bluetoothprinter.service.lattice.IDotFont;
import com.jerry.bluetoothprinter.service.lattice.PrinterCanvas;
import com.jerry.bluetoothprinter.view.R;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class PrintDataService extends PrintUtils{
	private final static String TAG = "PrintDataService";
	private Context context = null;
	private String deviceAddress = null;
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private BluetoothDevice device = null;
	private static BluetoothSocket bluetoothSocket = null;
	private static OutputStream outputStream = null;
	private static InputStream inputStream = null;
    private static final String NAME = "BluetoothChat";
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private boolean isConnection = false;
	private int bufferSize = 10240;
	private boolean isInit = true;
	private boolean sendPrinter = true;
	private ReadThread mReadThread;
	private static AcceptThread mAcceptThread;
	
	public PrintDataService(Context context, String deviceAddress) {
		super();
		this.context = context;
		this.deviceAddress = deviceAddress;
		this.device = this.bluetoothAdapter.getRemoteDevice(this.deviceAddress);
		startReadThread();
	}
	
	private void startReadThread() {
		isInit = true;
		if (mReadThread == null) {
			mReadThread = new ReadThread();
			mReadThread.start();
		}
		
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        } else if (!mAcceptThread.isrun) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
	}

	/**
	 * 获取设备名称
	 * 
	 * @return String
	 */
	public String getDeviceName() {
		return this.device.getName();
	}
	
	private class ReadThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			int len = 0;
			byte[] buffer = new byte[bufferSize];
			while (isInit) {
//				try {
//					if (inputStream != null)
//						len = inputStream.read(buffer);
//					byte[] alldata = new byte[len];
//					System.arraycopy(buffer, 0, alldata, 0, len);
//					if(len != 0) {
//						Log.i(TAG, "ReadThread len:" + HEX.bytesToHex(buffer));
//					}
//					if (alldata != null) {
//						Log.i(TAG, "ReadThread@@@ len:" + HEX.bytesToHex(buffer));
//						String alldatastr = HEX.bytesToHex(alldata);
//						if ((Integer.parseInt(alldatastr) & 0x20) > 0
//								|| (Integer.parseInt(alldatastr) & 0x04) > 0
//								|| Integer.parseInt(alldatastr) == 72) {
//							sendPrinter = false;
//							eventCallback(
//									EVENT_NO_PAPER,
//									mContext.getResources().getString(
//											R.string.print_faild_no_paper),
//									curInvoker);
//							Message msg = Message.obtain(mHandler, 0);
//							msg.obj = mContext.getResources().getString(
//									R.string.print_faild_no_paper);
//							mHandler.sendMessage(msg);
//						} else {
//							eventCallback(EVENT_OK, mContext.getResources()
//									.getString(R.string.print_success),
//									curInvoker);
							sendPrinter = true;
//						}
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}

		protected void cancel() {
			try {
				if (isInit) {
					interrupt();
					isInit = false;
				}
				if (inputStream != null)
					inputStream.close();
				if (outputStream != null)
					outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 连接蓝牙设备
	 */
	public boolean connect() {
		Log.i(TAG, "connect @@");
		if (!this.isConnection) {
			try {
				bluetoothSocket = this.device
						.createRfcommSocketToServiceRecord(uuid);
				bluetoothSocket.connect();
				outputStream = bluetoothSocket.getOutputStream();
				inputStream = bluetoothSocket.getInputStream();

				this.isConnection = true;
				if (this.bluetoothAdapter.isDiscovering()) {
					System.out.println("关闭适配器！");
					this.bluetoothAdapter.isDiscovering();
				}			
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(TAG, "connect e:" + e.getMessage());
				Toast.makeText(this.context, "连接失败！", Toast.LENGTH_LONG).show();
				return false;
			}
			Toast.makeText(this.context, this.device.getName() + "连接成功！",
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return true;
		}
	}

	/**
	 * 断开蓝牙设备连接
	 */
	public static void disconnect() {
		System.out.println("断开蓝牙设备连接");
		try {
			bluetoothSocket.close();
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	  private class AcceptThread extends Thread {
	        private final BluetoothServerSocket mmServerSocket;
	        BluetoothSocket socket = null;
	        private boolean isrun = true;

	        public AcceptThread() {
	            BluetoothServerSocket tmp = null;
	            // 鍒涘缓涓�釜鏂扮殑socket鏈嶅姟鐩戝惉
	            try {
	                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, uuid);
	            } catch (IOException e) {
	            	Log.i(TAG, "AcceptThread  error");
	                e.printStackTrace();
	            }
	            mmServerSocket = tmp;
	        }

	        public void run() {
	            Log.d(TAG, "BEGIN mAcceptThread" + mmServerSocket);
	            setName("AcceptThread");
	            // 濡傛灉褰撳墠娌℃湁閾炬帴鍒欎竴鐩寸洃鍚瑂ocket鏈嶅姟
	            while (isrun) {
		                try {     
		                    if (mmServerSocket == null) continue;
		                    Log.d(TAG, "BEGIN mAcceptThread!!!" + this);
		                    socket = mmServerSocket.accept();
		                    Log.d(TAG, "BEGIN mAcceptThread@@@" + this);
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }	  
	                }
	            }
	        }
	 
	 public static Bitmap compressPic(Bitmap bitmap) {
	        // 获取这个图片的宽和高
	        int width = bitmap.getWidth();
	        int height = bitmap.getHeight();
	        // 指定调整后的宽度和高度
	        int newWidth = 388;
	        int newHeight = height;
	        if(width > newWidth) {
	        	float multiple = (float)newWidth/(float)width;        	
	        	newHeight = (int)(height * multiple);
	        } else {
	        	return bitmap;
	        }
	        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
	        Canvas targetCanvas = new Canvas(targetBmp);
	        targetCanvas.drawColor(0xffffffff);
	        targetCanvas.drawBitmap(bitmap, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
	        return targetBmp;
	  }
	 
	 public static byte px2Byte(int x, int y, Bitmap bit) {
	        if (x < bit.getWidth() && y < bit.getHeight()) {
	            byte b;
	            int pixel = bit.getPixel(x, y);
	            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
	            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
	            int blue = pixel & 0x000000ff; // 取低两位
	            int gray = RGB2Gray(red, green, blue);
	            if (gray < 128) {
	                b = 1;
	            } else {
	                b = 0;
	            }
	            return b;
	        }
	        return 0;
	 }

	 private static int RGB2Gray(int r, int g, int b) {
	        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
	        return gray;
	 }
	 
	 public static byte[] draw2PxPoint(Bitmap bmp) {
	        //用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法      
	        //整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
	        //但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
	        //所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
	        int size = bmp.getWidth() * bmp.getHeight() / 8 + 3000;
	        byte[] data = new byte[size];
	        int k = 0;
	        //设置行距为0的指令
	        data[k++] = 0x1B;
	        data[k++] = 0x33;
	        data[k++] = 0x00;
//	        
	        // 逐行打印
	        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
	            //打印图片的指令
	            data[k++] = 0x1B;
	            data[k++] = 0x2A;
	            data[k++] = 0x21; 
	            data[k++] = (byte) (bmp.getWidth() % 256); //nL
	            data[k++] = (byte) (bmp.getWidth() / 256); //nH
	            //对于每一行，逐列打印
	            for (int i = 0; i < bmp.getWidth(); i++) {
	                //每一列24个像素点，分为3个字节存储
	                for (int m = 0; m < 3; m++) {
	                    //每个字节表示8个像素点，0表示白色，1表示黑色
	                    for (int n = 0; n < 8; n++) {
	                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
	                        data[k] += data[k] + b;
	                    }
	                    k++;
	                }
	            }
	            data[k++] = 0x10;//换行
	        }
	        return data;
	    }
	 
	
	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}
	 
	
	 
	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	/**
	 * 发送数据
	 */
	public void send(String sendData) {
		Log.i(TAG, "开始打印 isConnection:！！" + isConnection);
		if (this.isConnection) {
			try {
				setOutputStream(outputStream);
				initPrinter();
				setAlign(0x00);  //设置居左
				setLineSpace(0);
				printText("今晚打老虎\n");
				setAlign(0x01);  //设置居中
				printText("今晚打老虎\n");
				setAlign(0x02);   //设置居右
				printText("今晚打老虎\n");
				setAlign(0x00);   //设置居左
				setBold(0x01);  //设置加粗
				setTextSize(0x01);  //设置字体高加倍
				printText("今晚打老虎\n");
				setTextSize(0x10);   //设置字体宽加倍
				printText("今晚打老虎\n");
				setTextSize(0x11);  //设置字体高宽加倍
				printText("今晚打老虎\n");
				setBold(0x00);  //取消加粗
				printText("今晚打老虎\n");
				setTextSize(0x00);  //设置字体正常
				Drawable drawable = context.getResources().getDrawable(R.drawable.print_logo);
				Bitmap bitmap = drawableToBitmap(drawable);
				printImage(bitmap);
				printLine(1);
				printBarCode("www.baidu.com", 73, 600, 80);
				printLine(1);
				setAlign(0x01);  //设置居中
				printQRCode("https://www.baidu.com/", 300, 300);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
					.show();

		}
	}

	
}