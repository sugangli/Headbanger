package edu.rutgers.winlab.headbanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import edu.rutgers.winlab.headbanger.core.Authentication;
import edu.rutgers.winlab.headbanger.core.DataPreprocess;
import edu.rutgers.winlab.headbanger.util.InputArray;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class AuthenticationActivity extends Activity implements SensorEventListener {
	
	private SoundPool mSoundPool;
	private MediaPlayer mediaplayer;
	private Thread mThread;
	private Thread rThread;
	private Handler mHandler;
	private String logSessionDirectoryPath;
	private String ExemplarDirectoryPath;
	private TextView ShowTextView;
	
	
	private Sensor accSensor;
    private LogFileWriter accLogFileWriter;
    private boolean result;
    private boolean isRunning;
    
    private ArrayList<Long> timestamplist = new ArrayList<Long>();
    private ArrayList<Double> xval;
    private ArrayList<Double> yval;
    private ArrayList<Double> zval;
    
    private InputArray ia;
    
    private InputArray eia; //read exemplar file before authentication process start 
    
    private int exem_len = 500;
    
    private int sample_rate = 50;
    
    
    
    private SensorManager sensorManager;
    
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        View view = this.getLayoutInflater().inflate(
                R.layout.activity_authenticatition, null);
        addContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        view.setBackgroundColor(0xff009973);
        GregorianCalendar now = new GregorianCalendar();
        
        String logSessionIdentifier = now.get(GregorianCalendar.YEAR) + "-"
                + (now.get(GregorianCalendar.MONTH) + 1) + "-"
                + now.get(GregorianCalendar.DAY_OF_MONTH) + "_"
                + now.get(GregorianCalendar.HOUR_OF_DAY) + "-"
                + now.get(GregorianCalendar.MINUTE) + "-"
                + now.get(GregorianCalendar.SECOND) + "-"
                + now.get(GregorianCalendar.MILLISECOND);
        
        logSessionDirectoryPath = Environment.getExternalStorageDirectory()
                + "/HeadBanger/"+ logSessionIdentifier + "/";
        ExemplarDirectoryPath = Environment.getExternalStorageDirectory()
                + "/HeadBanger/exemplar/";
        
        ShowTextView = (TextView) findViewById(R.id.moveheadtext);
        
        sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(SENSOR_SERVICE);
        
        accSensor = (Sensor) sensorManager.getSensorList(
                Sensor.TYPE_ACCELEROMETER).get(0);
        sensorManager.registerListener(this, accSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        
        File logSessionDirectory = new File(logSessionDirectoryPath);
        
        
        xval = new ArrayList<Double>();
        yval = new ArrayList<Double>();
        zval = new ArrayList<Double>();
        
        mHandler = new Handler();

        try {
            logSessionDirectory.mkdirs();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error: " + e.getMessage());
        }
        
        accLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                + "acc.txt");
        
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        
        
        mediaplayer = MediaPlayer.create(this, R.raw.somebody_10s);
        
        
        
       
        
       
        
        rThread = new Thread(){
        	
        	@Override 
        	public void run(){
        	
        	try {
        		double[] buf1 = new double[exem_len];
        		double[] buf2 = new double[exem_len];
        		double[] buf3 = new double[exem_len];
        		
				BufferedReader br  = new BufferedReader(new FileReader(ExemplarDirectoryPath+"filtered_acc_li.csv"));
				String line;
				for (int i = 0; i < exem_len; i++){
				
					if ((line = br.readLine()) != null){
						
						String[] entry = line.split(",");
						buf1[i] = Double.parseDouble(entry[0]);
						buf2[i] = Double.parseDouble(entry[1]);
						buf3[i] = Double.parseDouble(entry[2]);
						
						//System.out.println(buf1[i] + " " + buf2[i] + " " + buf3[i]);
						
						
					}
					
					
				}
				//System.out.println("exemplar len: "+ );
				
				eia = new InputArray(buf1, buf2, buf3);
				
				br.close();
				
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
        	}
		
        };
        
        rThread.start();
        
        mediaplayer.start();
        
        mThread = new Thread(){//start once data co
        	@Override
        	 public void run(){
        		
        		isRunning = true;
        		
        		
        		
//        		DataPreprocess dp = new DataPreprocess();
//        		dp.OutputFilteredData(logSessionDirectoryPath+"acc.txt",logSessionDirectoryPath+"filtered_acc.csv");
        		
        		long[] time = new long[timestamplist.size()];
        		double[] buf1 = new double[xval.size()];
        		double[] buf2 = new double[yval.size()];
        		double[] buf3 = new double[zval.size()];
        		
        		for(int i = 0; i < timestamplist.size(); i++){
        			
        			time[i] = timestamplist.get(i);
        			buf1[i] = xval.get(i);
        			buf2[i] = yval.get(i);
        			buf3[i] = zval.get(i);
        			
        		}
        		
        		
        		Authentication auth = new Authentication();
        		
        		///result = auth.AuthenTest(logSessionDirectoryPath+"acc.txt", 
        		//		logSessionDirectoryPath+"filtered_acc.csv", ExemplarDirectoryPath+"filtered_acc.csv");
        		
        		//result = auth.AuthenTestInMem(logSessionDirectoryPath+"acc.txt", ExemplarDirectoryPath+"filtered_acc.csv");
        		
        		ia = new InputArray(time, buf1, buf2, buf3);
        		
        		//result = auth.AuthenTestInMem(ia, ExemplarDirectoryPath+"filtered_acc.csv", xval.size());
        		
        		//Log.i("mThread","timestamplist size:"+ timestamplist.size());
        		
        		//long sensingtime = timestamplist.get(timestamplist.size()-1) - timestamplist.get(0);
        
        		//Log.i("mThread","timestamplist :"+ sensingtime);
        		long starttime = System.currentTimeMillis();
        		result = auth.AuthenTestInMem(ia, eia, 500, sample_rate);
        	
        		long stoptime = System.currentTimeMillis();
        		
        		long latency = stoptime - starttime;
        		
//        		File orifile = new File(logSessionDirectoryPath+"acc.txt");
//        		File filfile = new File(logSessionDirectoryPath+"filtered_acc.csv");
//        		orifile.delete();
//        		filfile.delete();
        		
        		
        		
        		
        		Log.i("AuthenticationActivity", "algo latency: "+latency);
        		
        	    if(result){
        	    	
        	    	Log.i("AuthenticationActivity", "Succeed!");
        	    	
        	    }
        	    else
        	    	Log.i("AuthenticationActivity", "Fail!");
    	    	
        	    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        	if(result){
                        		ShowTextView.setText("Successful Login!");
                        		try {
									Thread.sleep(4000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                        	}else{
                        		ShowTextView.setText("Fail to Login!");
                        		try {
									Thread.sleep(4000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                        	}
                            isRunning = false;
                            startActivity(new Intent(AuthenticationActivity.this,
            	                    MainActivity.class));
                        }
                    });
        		
        	}
        	
        	
        	
        	
        };
        
        mediaplayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp) {
				ShowTextView.setText("Just a second...");
				sensorManager.unregisterListener(AuthenticationActivity.this);
				accLogFileWriter.closeWriter();
				mThread.start();
			
			}
    		
    	});
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        
	}
	
	@Override
    protected void onPause() {
		
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//mSoundPool.release();
        super.onPause();
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Long timestamp = System.currentTimeMillis();
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
            //accLogFileWriter.writeACCdata(timestamp, event.values[0],
            //        event.values[1], event.values[2]);
        
        	timestamplist.add(timestamp);
        	xval.add((double) event.values[0]);
        	yval.add((double) event.values[1]);
        	zval.add((double) event.values[2]);
        
            
            break;
            
         default :
        	break;
        }

		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
