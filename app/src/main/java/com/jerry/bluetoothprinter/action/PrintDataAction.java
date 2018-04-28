package com.jerry.bluetoothprinter.action;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.jerry.bluetoothprinter.service.PrintDataService;
import com.jerry.bluetoothprinter.view.R;

public class PrintDataAction implements OnClickListener {    
    private Context context = null;    
    private TextView deviceName = null;    
    private TextView connectState = null;    
    private EditText printData = null;    
    private String deviceAddress = null;    
    private PrintDataService printDataService = null;   
    
    public PrintDataAction(Context context, String deviceAddress,    
            TextView deviceName, TextView connectState) {    
        super();    
        this.context = context;    
        this.deviceAddress = deviceAddress;    
        this.deviceName = deviceName;    
        this.connectState = connectState;    
        this.printDataService = new PrintDataService(this.context,    
                this.deviceAddress);   
        this.initView();    
    }    
    
    Handler myHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                 
             }   
             super.handleMessage(msg);   
        }   
   };  
    
    private void initView() {    
        // 锟斤拷锟矫碉拷前锟借备锟斤拷锟�   
        this.deviceName.setText(this.printDataService.getDeviceName());    
        // 一锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟借备    
        boolean flag = this.printDataService.connect();    
        if (flag == false) {    
            // 锟斤拷锟斤拷失锟斤拷    
            this.connectState.setText("disconnect");    
        } else {    
            // 锟斤拷锟接成癸拷    
            this.connectState.setText("connected");    
    
        }    
    }    
    
    public void setPrintData(EditText printData) {    
        this.printData = printData;    
    }    
    
    @Override    
    public void onClick(View v) { 
        if (v.getId() == R.id.send) {    
            String sendData = this.printData.getText().toString();    
            this.printDataService.send(sendData + "\n");    
        }
    }    
}