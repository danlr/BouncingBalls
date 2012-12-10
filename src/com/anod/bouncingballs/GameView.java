package com.anod.bouncingballs;

import com.anod.bouncingballs.model.Ball;
import com.anod.bouncingballs.model.Box;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import java.util.Random;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView  extends SurfaceView implements
								SurfaceHolder.Callback {
	
	
	private static final String TAG = GameView.class.getSimpleName();
	
	public static final int RENDER_GAME = 1;
	public static final int RENDER_FINISH_SCREEN = 2;
	public static final int RENDER_PAUSE = 3;
	public static final int RENDER_START_SCREEN = 4;
	
	public static final int RENDER_BALLS_FILL = 1;
	public static final int RENDER_BALLS_NO_FILL = 0;
	
	private int renderOption = RENDER_GAME;
	
	private int renderBallsOption = RENDER_BALLS_NO_FILL;
	
	//private int previousRenderOption = RENDER_START_SCREEN;
	
	private MainGameThread thread;
	
	private int time = 0;
	
	private long prevTimeStamp = 0;
	
	public boolean surfaceCreated = false;
	
	private int moveFingerStartX = 0;
	private int moveFingerStartY = 0;
	
	private int moveFingerPosX = 0;
	private int moveFingerPosY = 0;
	
	private int newBallX = 0;
	private int newBallY = 0;
	private float newBallVx = 0;
	private float newBallVy = 0;
	
	
	private boolean isFingerDown = false;
	private boolean changeNewBallSize = false;
	private boolean changeNewBallSizeGrowth = true;
	private long changeNewBallTimeStamp = 0;
	
	private int timeOfFinishScreenIsShown = 0;
	
	private Box box;
	private Ball newBall = new Ball();
	
	double anglex = 0;
	double angley = 0;
	double anglez = 0;
	
	//private MediaPlayer mplayer;
	
	public GameView(Context context, int prevHighScore) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		// make the GamePanel focusable so it can handle events
		setFocusable(true);
		
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		//Log.d(TAG, "surface changed! h=" + height + ", w=" + width);
		if(height<width){
			//isLandscapeFormat = true;
		}
		InitGameState(width, height);
		if(!surfaceCreated){
			
			// create the game loop thread
			thread.setRunning(true);
			thread.start();
			surfaceCreated = true;
		}//else{}
			
		
	}
	
	private void InitGameState(int width, int height) {
		prevTimeStamp = 0;
		time = 0;
		if(surfaceCreated){
			Box newbox = new Box(width, height);
			for (Ball b : box.getBalls()) {
				newbox.AddBall(b);
			}
			box = newbox;
		}else{
			box = new Box(width, height);
			box.AddBall(
					new Ball(width/2,height/2, 20, 5,5)
					);
		}
		
				
		
		renderOption = RENDER_GAME;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		thread = new MainGameThread(getHolder(), this);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		
		
		QuitGame();
		
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
		
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(renderOption==RENDER_FINISH_SCREEN){
			if(timeOfFinishScreenIsShown>5){
				timeOfFinishScreenIsShown = 0;
				ResetGame();
			}
			return false;
		}
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
			changeNewBallSize = true;
			moveFingerStartX = (int)event.getX();
			moveFingerStartY = (int)event.getY();
			
			moveFingerPosX = (int)event.getX();
			moveFingerPosY = (int)event.getY();
			
			newBallX = (int)event.getX();
			newBallY = (int)event.getY();
			
			Random rnd = new Random();
			
			newBall = new Ball(newBallX, newBallY,
									10 + rnd.nextInt(15), //radius 
									0,0);
			
			isFingerDown = true;

		} if (event.getAction() == MotionEvent.ACTION_MOVE) {
			
			newBallVx = moveFingerStartX - (int)event.getX();
			newBallVy = moveFingerStartY - (int)event.getY();
			
			//if(newBallVx>50 || newBallVy>50){}
			
			newBallVx =  (Box.MAX_SPEED * ((float)newBallVx/box.getW()));
			newBallVy =  (Box.MAX_SPEED * ((float)newBallVy/box.getH()));
			
			moveFingerPosX = (int)event.getX();
			moveFingerPosY = (int)event.getY();
			
			
		} if (event.getAction() == MotionEvent.ACTION_UP) {
			isFingerDown = false;
			changeNewBallSize = false;
			newBall.setVx(newBallVx);
			newBall.setVy(newBallVy);
			
			newBallVx = 0;
			newBallVy = 0;
			
			changeNewBallSize = false;
			
			box.AddBall(
					//new Ball(newBallX,newBallY, 20, newBallVx,newBallVy)
					newBall
					);
		}
		return true;
	}
	
	
	public void render(Canvas canvas) {
		
		switch(renderOption){
			case RENDER_GAME:
				RenderGame(canvas);
				return;
			case RENDER_FINISH_SCREEN:
				//RenderFinish(canvas);
				return;
			case RENDER_PAUSE:
				//RenderPause(canvas);
				return;
			case RENDER_START_SCREEN:
				//RenderStartScreen(canvas);
				return;
		}
	}

	private void RenderGame(Canvas canvas) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		
			canvas.drawColor(Color.BLACK);
							
			if(isFingerDown){
				newBall.Render(canvas, renderBallsOption);
				//if(!changeNewBallSize){
					p.setColor(newBall.getColor());
					p.setStrokeWidth(2);
					canvas.drawLine(newBallX, newBallY, moveFingerPosX, moveFingerPosY, p);
					
					double angle = Math.atan2(newBallX - moveFingerPosX, newBallY - moveFingerPosY);
					double arrowAngle = Math.PI * 160 /180;
					int arrowLine = 20;
					
					canvas.drawLine(newBallX, newBallY, 
							(int)(newBallX + arrowLine * Math.sin(angle + arrowAngle)) , 
							(int)(newBallY + arrowLine * Math.cos(angle + arrowAngle)),
							p);
					canvas.drawLine(newBallX, newBallY, 
							(int)(newBallX + arrowLine * Math.sin(angle - arrowAngle)), 
							(int)(newBallY + arrowLine * Math.cos(angle - arrowAngle)),
							p);
				//}
			}
			
			box.Render(canvas, renderBallsOption);
	}
	
	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update(long ticks) {
		
		if(prevTimeStamp==0){
			prevTimeStamp = ticks;
		}
		
		if(changeNewBallSize && ticks-changeNewBallTimeStamp >= 100){
			ChangeNewBallSize();
			changeNewBallTimeStamp = ticks;
		}
		
		if(ticks-prevTimeStamp>=10){  
			if(renderOption==RENDER_GAME){
				time++;
				box.UpdateState();
			}else{
				timeOfFinishScreenIsShown++;
			}
			prevTimeStamp = ticks;
		}
		
	}
	
	private void ChangeNewBallSize(){
		if(newBall.getR()<=box.MIN_BALL_RADIUS && !changeNewBallSizeGrowth){
			changeNewBallSizeGrowth = true;
		}
		if(newBall.getR()>=box.MAX_BALL_RADIUS && changeNewBallSizeGrowth){
			changeNewBallSizeGrowth = false;
		}
		
		if(changeNewBallSizeGrowth) {
			newBall.setR(newBall.getR()+1);
		}else{
			newBall.setR(newBall.getR()-1);
		}
	}

	public void QuitGame(){
		thread.setRunning(false);
		((Activity)getContext()).finish();
	}
	
	public void ResetGame(){
			thread.setRunning(true);
			thread.setPaused(true);
			surfaceCreated = false;
			InitGameState(box.getW(),box.getH());
			surfaceCreated = true;
			thread.setPaused(false);
	}
	
	public void NewGame(){
		thread.setRunning(true);
		thread.setPaused(true);
		box = new Box(box.getW(),box.getH());
		thread.setPaused(false);
	}
	
	public void PauseGame(){
		thread.setPaused(true);
	}
	
	public void ResumeGame(){
		thread.setPaused(false);
	}
	
	public void SetNewGravity(double anglex, double angley, double anglez){
		this.anglex = anglex;
		this.angley = angley;
		this.anglez = anglez;
		
		float dx = -(float)Math.sin(anglex);
		float dy = (float)Math.sin(angley);
		if(surfaceCreated){
			box.setAx(dx);
			box.setAy(dy);
		}
	}

	public void SetFilling(boolean fillCircles) {
		renderBallsOption = fillCircles ? RENDER_BALLS_FILL : RENDER_BALLS_NO_FILL;
	}

}
