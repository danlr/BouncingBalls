package com.anod.bouncingballs;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
    
	private static final String TAG = MainActivity.class.getSimpleName();
		
	private static final String GAME_PREFNAME = "AnodPreferences";
	private static final String HIGH_SCORE_PREFNAME = "HighScore";
	
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    GoogleAnalyticsTracker tracker;
	
	private static GameView gameView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-36639377-2", this);
    	
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    	
    	SharedPreferences settings = getSharedPreferences(GAME_PREFNAME, 0);
        int prevHighScore = settings.getInt(HIGH_SCORE_PREFNAME, 0);

    	
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        gameView = new GameView(this,prevHighScore);
        
        setContentView(gameView);
        registerForContextMenu(gameView);
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.dispatch();
		tracker.stopSession();
	}

    @Override
    public void onStart() {
      super.onStart();
      
      tracker.trackPageView("/main");
    }

    
	@Override
	protected void onStop() {
		super.onStop();
		gameView.QuitGame();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	@Override
	protected void onPause(){
		super.onPause();
		mSensorManager.unregisterListener(this);
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean focus){
		if(focus){
			if(gameView.surfaceCreated){
				gameView.ResumeGame();
			}
		}else{
			gameView.PauseGame();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	 	//Toast  toast;
        switch(item.getItemId()) {
        case R.id.quit:
        	gameView.QuitGame();
        	this.finish();
        	
            return true;
            
        case R.id.new_game:
        	gameView.NewGame();
        	return true;
        case R.id.menu_help:
        	Toast toast = Toast.makeText(getApplicationContext(), R.string.help_message, Toast.LENGTH_LONG);
        	toast.show();
        	return true;
        
        case R.id.gravity_onoff:
        	if (item.getTitle() == getString(R.string.gravity_on)) {
        		item.setChecked(false);
        		item.setTitle(R.string.gravity_off);
        		mSensorManager.unregisterListener(this);
        		gameView.SetNewGravity(0, 0, 0);
        	}
            else {
            	item.setChecked(true);
            	item.setTitle(R.string.gravity_on);
            	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            }

        	return true;
        	
        case R.id.fillCircles_onoff:
        	if (item.getTitle() == getString(R.string.fillCircles_on)) {
        		item.setChecked(false);
        		item.setTitle(R.string.fillCircles_off);
        		
        		gameView.SetCirclesFilling(false);
        	}
            else {
            	item.setChecked(true);
            	item.setTitle(R.string.fillCircles_on);
            	gameView.SetCirclesFilling(true);
            }

        	return true;
	    }
        return super.onMenuItemSelected(featureId, item);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */

		/*
		 final float alpha = 0.8f;
        
		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float[] linear_acceleration = new float[3];
		linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
		 * */
		
		float gx = event.values[0];
		float gy = event.values[1];
		float gz = event.values[2];
		
		float g = (float) Math.sqrt(gx*gx + gy*gy + gz*gz);
		
		double anglex = Math.atan2(gx,g);
		double angley = Math.atan2(gy,g);
		double anglez = Math.atan2(gz,g);
		
		gameView.SetNewGravity(anglex, angley, anglez);
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}