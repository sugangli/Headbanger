package edu.rutgers.winlab.headbanger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Random;

import edu.rutgers.winlab.headbanger.R;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class LoggerService extends Service implements SensorEventListener {

    public static boolean isLogging = false;
    private String logSessionDirectoryPath;
    private String musicDirectoryPath;

    
    
    private Thread tThread;
    private boolean tempoIsRunning;
    
    private Thread mThread;
    private boolean musicIsRunning;
    private MediaPlayer mediaplayer;
    private LogFileWriter musicLogfileWriter;
    
    private TextView thresholdTextView;
    private SoundPool mSoundPool;
    private int mSoundID1;
    private int mSoundID2;
    private SharedPreferences mSharedPreferences;
    private Handler mHandler;
    private TextView timingTextView;
    private String timingText;
    private int beatCount;
    private LogFileWriter beatCoutnfilewriter;
    
    
   
    private float xAccValue;
    private float yAccValue;
    private float zAccValue;
   
    private SensorManager sensorManager;

    private Sensor accSensor;
    private LogFileWriter accLogFileWriter;

    private Sensor rvSensor;
    private LogFileWriter rotationLogFileWriter;
    private LogFileWriter quaternionLogFileWriter;

    private Sensor gsSensor;
    private LogFileWriter gyroLogFileWriter;

    private Sensor mgSensor;
    private LogFileWriter mgLogFileWriter;

    private Sensor liSensor;
    private LogFileWriter lightSensorLogFileWriter;

    private Thread cameraThread;
    private Camera mCamera;
    private CameraSurfaceView mCameraSurfaceView;

    private SharedPreferences sharedPreferences;
    private WindowManager windowManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    	Log.i("onCreate", "Service Start");
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(SENSOR_SERVICE);
      
        
        // camera
        if (sharedPreferences.getBoolean("preference_camera", true)) {
            mCamera = Camera.open();
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    3, 2, WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mCameraSurfaceView = new CameraSurfaceView(this, mCamera);
            windowManager.addView(mCameraSurfaceView, params);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isLogging = true;
        foregroundProcessing();
        return START_STICKY;
    }

    private void foregroundProcessing() {
        GregorianCalendar now = new GregorianCalendar();

        String logSessionIdentifier = now.get(GregorianCalendar.YEAR) + "-"
                + (now.get(GregorianCalendar.MONTH) + 1) + "-"
                + now.get(GregorianCalendar.DAY_OF_MONTH) + "_"
                + now.get(GregorianCalendar.HOUR_OF_DAY) + "-"
                + now.get(GregorianCalendar.MINUTE) + "-"
                + now.get(GregorianCalendar.SECOND) + "-"
                + now.get(GregorianCalendar.MILLISECOND);

        logSessionDirectoryPath = Environment.getExternalStorageDirectory()
                + "/Headbanger/" + logSessionIdentifier + "/";
        musicDirectoryPath = Environment.getExternalStorageDirectory()
                + "/Headbanger/music/";

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundID1 = mSoundPool.load(getApplicationContext(), R.raw.timing1, 0);
        mSoundID2 = mSoundPool.load(getApplicationContext(), R.raw.timing2, 0);

        File logSessionDirectory = new File(logSessionDirectoryPath);

        try {
            logSessionDirectory.mkdirs();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error: " + e.getMessage());
        }

        // accelerometer
        if (sharedPreferences.getBoolean("preference_accelerometer", true)) {
            accLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "acc.txt");
            accSensor = (Sensor) sensorManager.getSensorList(
                    Sensor.TYPE_ACCELEROMETER).get(0);
            sensorManager.registerListener(this, accSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // rotation vector
        if (sharedPreferences.getBoolean("preference_rotation", true)) {
            rotationLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "rotation.txt");
            quaternionLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "quaternion.txt");
            rvSensor = (Sensor) sensorManager.getSensorList(
                    Sensor.TYPE_ROTATION_VECTOR).get(0);
            sensorManager.registerListener(this, rvSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // gyroscope
        if (sharedPreferences.getBoolean("preference_gyroscope", false)) {
            gyroLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "gyro.txt");
            gsSensor = (Sensor) sensorManager.getSensorList(
                    Sensor.TYPE_GYROSCOPE).get(0);
            sensorManager.registerListener(this, gsSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // magnetic sensor
        if (sharedPreferences.getBoolean("preference_magnetic_sensor", false)) {
            mgLogFileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "magnetic.txt");
            mgSensor = (Sensor) sensorManager.getSensorList(
                    Sensor.TYPE_MAGNETIC_FIELD).get(0);
            sensorManager.registerListener(this, mgSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // light sensor
        if (sharedPreferences.getBoolean("preference_light_sensor", false)) {
            lightSensorLogFileWriter = new LogFileWriter(
                    logSessionDirectoryPath + "light.txt");
            liSensor = (Sensor) sensorManager.getSensorList(Sensor.TYPE_LIGHT)
                    .get(0);
            sensorManager.registerListener(this, liSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

 
        
        //play the midi track
        if(!musicIsRunning){
        	musicLogfileWriter = new LogFileWriter(logSessionDirectoryPath
                    + "music.txt");
        	mediaplayer = MediaPlayer.create(this, R.raw.somebodyedit);
        	
        	mediaplayer.start();
        	Long timestamp = System.currentTimeMillis();
        	musicLogfileWriter.writeBeat(timestamp, 1);
        	
        	mediaplayer.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer mp) {
					LoggerService.this.stopSelf();
				}
        		
        	});
        
        	
        }
        //play the sound track
        if(!tempoIsRunning) {
        	beatCoutnfilewriter = new LogFileWriter(logSessionDirectoryPath
                    + "beatcount.txt");
        	tThread = new Thread() {
        		@Override
        		public void run(){
                    
                    while (tempoIsRunning){
                    	
                    	Long timestamp = System.currentTimeMillis();
                    	Random rnd = new Random ();
                    	rnd.setSeed(timestamp);
                    	int interval = rnd.nextInt(2000);
                    	try {
							Thread.sleep(interval+1000);// interval will be a random # between 1 to 3 secs
							mSoundPool.play(mSoundID2, 1.0f, 1.0f, 0, 0, 1.0f);
							beatCount++;
							beatCoutnfilewriter.writeBeat(timestamp, beatCount);
							
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	
                    	//mSoundPool.play(mSoundID1, 1.0f, 1.0f, 0, 0, 1.0f);
                    	
                    }
                    
        		}
        	};
        	//tThread.start();
        	tempoIsRunning = true;
        	
        }
        
 
        // camera
        if (sharedPreferences.getBoolean("preference_camera", false)) {
            mCamera.startPreview();
            cameraThread = new Thread() {
                public void run() {
                    while (isLogging) {
                        try {
                            Thread.sleep(5000);
                            mCamera.takePicture(null, null, pictureCallback);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            //cameraThread.start();
        }
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null || mCamera == null) {
                return;
            }

            String imageDirectoryPath = logSessionDirectoryPath + "camera/";
            File imageDirectory = new File(imageDirectoryPath);
            if (!imageDirectory.exists()) {
                try {
                    imageDirectory.mkdirs();
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(),
                            "Error: " + e.getMessage());
                }
            }

            String imagePath = imageDirectoryPath + System.currentTimeMillis()
                    + ".jpg";
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(imagePath, true);
                fileOutputStream.write(data);
                fileOutputStream.close();
            } catch (Exception e) {
            }
            fileOutputStream = null;
            Log.v("logger", "shoya:take a pucture");
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Long timestamp = System.currentTimeMillis();
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
            accLogFileWriter.writeACCdata(timestamp, event.values[0],
                    event.values[1], event.values[2]);
            xAccValue = event.values[0];
            yAccValue = event.values[1];
            zAccValue = event.values[2];
            
            break;

        case Sensor.TYPE_ROTATION_VECTOR:
            rotationLogFileWriter.writeRotationVectorData(timestamp,
                    event.values[0], event.values[1], event.values[2]);
            float[] quaternion = new float[4];
            SensorManager.getQuaternionFromVector(quaternion, event.values);
            quaternionLogFileWriter.writeQuaternionData(timestamp,
                    quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
            break;

        case Sensor.TYPE_GYROSCOPE:
            gyroLogFileWriter.writeGyroscopeData(timestamp, event.values[0],
                    event.values[1], event.values[2]);
            break;

        case Sensor.TYPE_MAGNETIC_FIELD:
            mgLogFileWriter.writeMagneticSensorData(timestamp, event.values[0],
                    event.values[1], event.values[2]);
            break;

        case Sensor.TYPE_LIGHT:
            lightSensorLogFileWriter.writeLightSensorData(timestamp,
                    event.values[0]);
            break;

        default:
            break;
        }
    }

    @Override
    public void onDestroy() {

        // Stop logging
        sensorManager.unregisterListener(this);
        isLogging = false;

        // save
        if (accSensor != null) {
            accLogFileWriter.closeWriter();
            accSensor = null;
        }
        if (rvSensor != null) {
            rotationLogFileWriter.closeWriter();
            quaternionLogFileWriter.closeWriter();
            rvSensor = null;
        }
        if (gsSensor != null) {
            gyroLogFileWriter.closeWriter();
            gsSensor = null;
        }
        if (mgSensor != null) {
            mgLogFileWriter.closeWriter();
            mgSensor = null;
        }
        if (liSensor != null) {
            lightSensorLogFileWriter.closeWriter();
            liSensor = null;
        }
        if (mediaplayer != null){
        	musicLogfileWriter.writeBeat(System.currentTimeMillis(), 0);
        	musicLogfileWriter.closeWriter();
        	mediaplayer.release();
        	mediaplayer = null;
        	
        }

        if (musicIsRunning) {
        	musicIsRunning = false;
        	beatCoutnfilewriter.closeWriter();
        	mThread.interrupt();
        } 

        if (mCamera != null) {
            
        }
    }

}
