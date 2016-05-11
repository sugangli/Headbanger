package edu.rutgers.winlab.headbanger;



import edu.rutgers.winlab.headbanger.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Context mContext;
    LoggerService serviceBinder;

    private boolean mJustSelected;
    private View backgroundView;

    private WakeLock wakeLock;
    private PowerManager powerManager;
    private SoundPool mSoundPool;
    private int mSoundID;
    private Handler mHandler;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        setContentView(layout);
        backgroundView = new View(this);
        layout.addView(backgroundView);
        backgroundView.setBackgroundColor(0xff009973);
        mContext = getBaseContext();
    }
    
    private boolean isAttached = false;

    @Override
    public void onAttachedToWindow() {
      super.onAttachedToWindow();
      this.isAttached = true;
      openOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundID = mSoundPool.load(getApplicationContext(), R.raw.finished, 0);
        if (this.isAttached)
        	openOptionsMenu();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "GlassLogger");
        wakeLock.acquire();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem startMenuItem = (MenuItem) menu.findItem(R.id.welcome);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.welcome:
        	Log.i("onOptionsItemSelected", "Welcome");
        	startActivity(new Intent(MainActivity.this,
                    AuthenticationActivity.class));
            mJustSelected = true;
            return false;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (mJustSelected) {
            // FIXME: need to wait a bit
            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    openOptionsMenu();
                }
            });
            mJustSelected = false;
        } else {
            // User dismissed so back out completely
            // FIXME: right now, calling finish here doesn't seem to let us
            // re-enable the receiver
            // finish();
        }
    }

    private void startRecording() {
        if (isInstallationFinished()) {
        	Log.i("startRecording", "Isinstalled");
            if (android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)) {
                Intent bindIndent = new Intent(MainActivity.this,
                        LoggerService.class);
                Log.i("startRecording", "startservice");
                mContext.startService(bindIndent);
                if (sharedPreferences.getBoolean("timer", false)) {
                    mHandler = new Handler();
                    mHandler.postDelayed(finihsedrecordingTimer, 310000);
                }
            } else {
                Toast.makeText(getBaseContext(),
                        "Error: Storage does not have enough space.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
        	Log.i("startRecording", "not installed");
            Toast.makeText(getBaseContext(), "Error: permission error.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (isServiceRunning()) {
            Intent bindIndent = new Intent(MainActivity.this,
                    LoggerService.class);
            mContext.stopService(bindIndent);
        }
    }

    private boolean isServiceRunning() {
        return LoggerService.isLogging;
    }

    @Override
    protected void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSoundPool.release();
        super.onPause();
        wakeLock.release();
    }

    private void clearCalibration() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("threshold", 4.0f);
        editor.commit();
    }

    private boolean isInstallationFinished() {
        // TODO Check whether App has root permission.
        return true;
    }

    private final Runnable finihsedrecordingTimer = new Runnable() {
        @Override
        public void run() {
            mSoundPool.play(mSoundID, 1.0f, 1.0f, 0, 0, 1.0f);
            try {
                Thread.sleep(1000);
                stopRecording();
            } catch (InterruptedException e) {
            }
        }
    };

}