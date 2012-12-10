package com.anod.bouncingballs.model;

import android.graphics.Canvas;

public class Box {
	private static final float G = 10;
	private static final float dT = 0.05f;
	
	private int width;
	private int height;
	
	private float ax = 0;
	private float ay = G;
	
	public void setAx(float ax){
		this.ax = ax * G;
	}
	public void setAy(float ay){
		this.ay = ay * G;
	}
	
	public int getH(){
		return height;
	}
	public int getW(){
		return width;
	}
	
	public static final int MAX_SPEED = 20;
	public static final int MAX_BALLS = 20;
	public int MAX_BALL_RADIUS = 20;
	public int MIN_BALL_RADIUS = 8;
	
	private Ball[] balls;
	
	public Box(int w, int h){
		balls = new Ball[1];
		this.height = h;
		this.width = w;
		balls = new Ball[0];
		MAX_BALL_RADIUS =  Math.min(h, w)/10;
	}
	
	public boolean isFull(){
		return balls.length>MAX_BALLS;
	}
	
	public int getBallsCount(){
		return balls.length;
	}
	
	public Ball[] getBalls(){
		return balls;
	}
	
	public void AddBall(Ball ball){
		synchronized(balls){
			if(balls.length==0){
				balls = new Ball[1];
				balls[0] = ball;
				return;
			}
			if(!isFull()){
				Ball[] newBalls = new Ball[balls.length+1];
				for(int i = 0; i < balls.length; i++){
					newBalls[i] = new Ball(balls[i].getX(), balls[i].getY(), balls[i].getR(), balls[i].getVx(), balls[i].getVy(), balls[i].getColor());
				}
				newBalls[newBalls.length-1] = ball;
				balls = newBalls;
			}else{
				Ball[] newBalls = new Ball[balls.length];
				for(int i = 0;i<  balls.length-1; i++){
					newBalls[i] = balls[i+1];
				}
				newBalls[newBalls.length-1] = ball;
				balls = newBalls;
			}
		}
	}
	
	public void UpdateState(){
		synchronized(balls){
			for (int i =0;i < balls.length; i++) {
				
				balls[i].setVx(balls[i].getVx() + ax * dT);
				balls[i].setVy(balls[i].getVy() + ay * dT);
				
				balls[i].setX(balls[i].getX() + balls[i].getVx());
				balls[i].setY(balls[i].getY() + balls[i].getVy());
				
				if( (balls[i].getX() <= balls[i].getR() && balls[i].getVx()<0) || 
						(balls[i].getX() >= (width - balls[i].getR()) && balls[i].getVx()>0) ){
					balls[i].setVx(-balls[i].getVx() * 0.95f);
				}
				
				if( (balls[i].getY() <= balls[i].getR()&& balls[i].getVy()<0)
						|| (balls[i].getY() >= (height - balls[i].getR())  && balls[i].getVy()>0) ){
					balls[i].setVy(-balls[i].getVy() * 0.95f);
				}
				//check screen bounds
				if (balls[i].getY() < balls[i].getR()){
					balls[i].setY(balls[i].getR());
				}
				if (balls[i].getX() < balls[i].getR()){
					balls[i].setX(balls[i].getR());
				}
				if (balls[i].getY() > (height - balls[i].getR())){
					balls[i].setY(height - balls[i].getR());
				}
				if (balls[i].getX() > (width - balls[i].getR())){
					balls[i].setX(width - balls[i].getR());
				}
				
				for (int j = i;j < balls.length; j++) {
					
					if(i==j) continue;

					float Px = balls[i].getX() - balls[j].getX();
					float Py = balls[i].getY() - balls[j].getY();
					float P2 = Px * Px + Py * Py;
					if (P2 <= 4 * balls[i].getR() * balls[j].getR()) {
						float vx1p = (balls[j].getVx() * Px + balls[j].getVy() * Py) * Px / P2;
						float VY1P = (balls[j].getVx() * Px + balls[j].getVy() * Py) * Py / P2;
						float VX1N = (Py * balls[j].getVx() - Px * balls[j].getVy()) * Py / P2;
						float VY1N = (Px * balls[j].getVy() - Py * balls[j].getVx()) * Px / P2;
						float VX2P = (balls[i].getVx() * Px + balls[i].getVy() * Py) * Px / P2;
						float VY2P = (balls[i].getVx() * Px + balls[i].getVy() * Py) * Py / P2;
						float VX2N = (Py * balls[i].getVx() - Px * balls[i].getVy()) * Py / P2;
						float VY2N = (Px * balls[i].getVy() - Py * balls[i].getVx()) * Px / P2;
						if ((Px * (VX2P - vx1p) + Py * (VY2P - VY1P)) < 0) { // if become closer
							balls[j].setVx(VX2P + VX1N);
							balls[j].setVy(VY2P + VY1N);
							balls[i].setVx(vx1p + VX2N);
							balls[i].setVy(VY1P + VY2N);
						}
					}

					////
					
					/*
					if( i!=j &&
					(balls[i].getX()-balls[j].getX())*(balls[i].getX()-balls[j].getX()) + (balls[i].getY()-balls[j].getY())*(balls[i].getY()-balls[j].getY()) <= 4 * balls[i].getR() * balls[j].getR()
					)
					{
						float px = balls[i].getVx()*balls[i].getM();
						float py = balls[i].getVy()*balls[i].getM();
						
						balls[i].setVx( balls[j].getVx() * balls[j].getM() / balls[i].getM());
						balls[i].setVy( balls[j].getVy() * balls[j].getM() / balls[i].getM());
						
						balls[j].setVx( px/ balls[j].getM());
						balls[j].setVy( py/ balls[j].getM());
					}
					*/
				}//internal 'for'
				
				
			}
		}
	}
	
	public void Render(Canvas canvas, int renderBallsOption){
		//Log.d("BOX", "render");
		synchronized(balls){
			for (int i =0;i < balls.length; i++) {
				balls[i].Render(canvas, renderBallsOption);
			}
		}
	}
}
