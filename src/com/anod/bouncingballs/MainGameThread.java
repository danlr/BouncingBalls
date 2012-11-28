package com.anod.bouncingballs;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class MainGameThread extends Thread {
	
	private static final String TAG = MainGameThread.class.getSimpleName();

	// desired fps
	private final static int 	MAX_FPS = 50;	
	// maximum number of frames to be skipped
	private final static int	MAX_FRAME_SKIPS = 20;
	// the frame period
	private final static int	FRAME_PERIOD = 1000 / MAX_FPS;
	
	// Surface holder that can access the physical surface
	private SurfaceHolder surfaceHolder;
	// The actual view that handles inputs
	// and draws to the surface
	private GameView gamePanel;

	// flag to hold game state 
	private boolean running;
	
	private boolean paused = false;
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean getRunning() {
		return this.running;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean getPaused() {
		return this.paused;
	}

	public MainGameThread(SurfaceHolder surfaceHolder, GameView gamePanel) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
	}

	@Override
	public void run() {
		Canvas canvas;
		Log.d(TAG, "Starting game loop");

		long beginTime;		// the time when the cycle begun
		long timeDiff;		// the time it took for the cycle to execute
		int sleepTime;		// ms to sleep (<0 if we're behind)
		int framesSkipped;	// number of frames being skipped 

		sleepTime = 0;
		
		while (running) {
			canvas = null;
			// try locking the canvas for exclusive pixel editing
			// in the surface
			try {
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					if(!paused) {
						beginTime = System.currentTimeMillis();
						framesSkipped = 0;	// resetting the frames skipped
						// update game state 
						this.gamePanel.update(beginTime);
						// render state to the screen
						// draws the canvas on the panel
						this.gamePanel.render(canvas);
						// calculate how long did the cycle take
						timeDiff = System.currentTimeMillis() - beginTime;
						// calculate sleep time
						sleepTime = (int)(FRAME_PERIOD - timeDiff);
						
						if (sleepTime > 0) {
							// if sleepTime > 0 we're OK
							try {
								// send the thread to sleep for a short period
								// very useful for battery saving
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {}
						}
						
						while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
							// we need to catch up
							this.gamePanel.update(System.currentTimeMillis()); // update without rendering
							sleepTime += FRAME_PERIOD;	// add frame period to check if in next frame
							framesSkipped++;
						}
					}else{
						this.gamePanel.render(canvas);
						try {
							// send the thread to sleep for a short period
							// very useful for battery saving
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}
				}
			} finally {
				// in case of an exception the surface is not left in 
				// an inconsistent state
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}	// end finally
		}
	}

}