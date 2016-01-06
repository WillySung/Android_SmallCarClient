package com.example.shouchougen.smallcarclient_taichung;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class CameraTest extends Activity {
    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    int screenWidth, screenHeight;
    Camera camera;                    //define the camera of phone
    boolean isPreview = false;        //check if previewing
    private String ipname;
    //public String str_recv;
    //public int flag = 0;
    ImageView iconView;
    public static int imageResolution = 10;

    // variables for bluetooth
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private String address = null;   //"98:D3:31:B4:4D:76";

    public BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothSocket btSocket = null;
    public static OutputStream outStream = null;

    public static String message = " ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set to full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //got the ip information
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        ipname = data.getString("ipname");
        address = data.getString("btname");
        imageResolution = data.getInt("resolution");

        screenWidth = 640;//640
        screenHeight = 480;//480
        sView = (SurfaceView) findViewById(R.id.sView);                  // got the surfaceview
        surfaceHolder = sView.getHolder();                               // got surface holder
        iconView = (ImageView)findViewById(R.id.imageView);

        // add a callback
        surfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();                                            // initialize the camera
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // if camera != null release the camera
                if (camera != null) {
                    if (isPreview)
                        camera.stopPreview();
                    camera.release();
                    camera = null;
                }
                System.exit(0);
            }
        });
		    	
    	/*------------------BT Socket initial----------------*/

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(this.address);

        while(mBluetoothAdapter == null);
        mBluetoothAdapter.cancelDiscovery();

        try {
            btSocket = device.createRfcommSocketToServiceRecord(this.MY_UUID);
            btSocket.connect();
        } catch (IOException e) {}
		
		
    	/*------------------------ Create the ChatThread -------------------------*/
        Thread chat = new ChatThread(ipname);
        chat.start();

        // This thread is used to change the UI

        new Thread(new Runnable() {
            public void run(){

                while(true){
                    if(message.trim().equals("A")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.pee);
                            }
                        });
                    }
                    else if(message.trim().equals("B")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.tv);
                            }
                        });
                    }
                    else if(message.trim().equals("C")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.hi);
                            }
                        });
                    }
                    else if(message.trim().equals("D")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.rest);
                            }
                        });
                    }
                    else if(message.trim().equals("E")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.happy);
                            }
                        });
                    }
                    else if(message.trim().equals("F")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.angry);
                            }
                        });
                    }
                    else if(message.trim().equals("G")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.help);
                            }
                        });
                    }
                    else if(message.trim().equals("H")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                iconView.setImageResource(R.drawable.sad);
                            }
                        });
                    }
                    else{

                    }
                }
            }
        }).start();

    }

    private void initCamera() {
        if (!isPreview) {
            //front carmera = 1  default = back camera
            camera = Camera.open(1);
        }
        if (camera != null && !isPreview) {
            try{
                Camera.Parameters parameters = camera.getParameters();

                List<Size> sizes = parameters.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {
                    if (sizes.get(i).width < size.width){
                        size = sizes.get(i);
                    }
                }

                parameters.setPreviewSize(screenWidth, screenHeight);    // set the size of preview image
                parameters.setPreviewFpsRange(10,15);                    // set the preview fps
                parameters.setPictureFormat(ImageFormat.NV21);           // set image format to yuv

                parameters.setPictureSize(size.width, size.width);
                //parameters.setPictureSize(screenWidth, screenHeight);    // set the picture size
                camera.setPreviewDisplay(surfaceHolder);                 // set the surface to display
                camera.setDisplayOrientation(90);
                camera.setPreviewCallback(new StreamIt(ipname));         // set the callback function
                camera.startPreview();                                   // start to preview
                camera.autoFocus(null);                                  // set to auto focus
            } catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }
}

class StreamIt implements Camera.PreviewCallback {
    private String ipname;
    public StreamIt(String ipname){
        this.ipname = ipname;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        try{
            //Transfer the image data to JPG
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if(image!=null){
                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), CameraTest.imageResolution, outstream);
                outstream.flush();

                //use a new thread to send the jpg
                Thread th = new ImageThread(outstream,ipname);
                th.start();
            }
        }catch(Exception ex){
            Log.e("Sys","Error:"+ex.getMessage());
        }
    }
}

class ImageThread extends Thread{
    private byte byteBuffer[] = new byte[1024];
    private OutputStream outsocket;
    private ByteArrayOutputStream myoutputstream;
    private String ipname;

    public ImageThread(ByteArrayOutputStream myoutputstream, String ipname){
        this.myoutputstream = myoutputstream;
        this.ipname = ipname;
        try {
            myoutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try{
            //send the img through socket
            Socket tempSocket = new Socket(ipname, 6000);
            outsocket = tempSocket.getOutputStream();
            ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
            int amount;
            while ((amount = inputstream.read(byteBuffer)) != -1) {
                outsocket.write(byteBuffer, 0, amount);
            }
            myoutputstream.flush();
            myoutputstream.close();
            tempSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ChatThread extends Thread implements Runnable{
    // variables for internet
    private String ipname;

    public ChatThread(String ipname){
        this.ipname = ipname;
    }

    public void run(){
        int servPort = 6001;

    	/*------------------Receive the message from internet and sent it to blue-tooth device ----------*/
        while (true) {
            try {
                Socket socket = new Socket(this.ipname, servPort);
                while (true) {
                    InputStream in = socket.getInputStream();
                    byte[] rebyte = new byte[2];
                    byte[] msgBuffer;
                    String message;
                    in.read(rebyte);
                    message = new String(new String(rebyte));
                    CameraTest.outStream = CameraTest.btSocket.getOutputStream();

                    // pass this message to the UI changing thread
                    CameraTest.message = message;

                    // sent the message to the car
                    msgBuffer = message.getBytes();
                    try {
                        CameraTest.outStream.write(msgBuffer);
                    } catch (IOException e) { }
                }

            } catch (IOException e) { }

        }

    }
}