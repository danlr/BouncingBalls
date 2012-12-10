package com.anod.bouncingballs.model;

import java.util.Random;

import com.anod.bouncingballs.GameView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class Ball {
	
	private int m = 100;
	private int r;
	private float x;
	private float y;
	private float vx;
	private float vy;
	private int color;
	public final long id = System.currentTimeMillis();
	
	public int getM(){
		return m;
	}
	public int getR(){
		return r;
	}
	public void setR(int r){
		this.r = r;
	}
	
	public int getColor(){
		return color;
	}
	public void setColor(int color){
		this.color = color;
	}
	
	public float getX(){
		return x;
	}
	public float getY(){
		return y;
	}
	public float getVx(){
		return vx;
	}
	public float getVy(){
		return vy;
	}
	
	
	public void setX(float x){
		this.x = x;
	}
	public void setY(float y){
		this.y = y;
	}
	public void setVx(float vx){
		this.vx = vx;
	}
	public void setVy(float vy){
		this.vy = vy;
	}
	
	public Ball(float x, float y, int r, float vx, float vy){
		this.x = x;
		this.y = y;
		this.r = r;
		this.vx = vx;
		this.vy = vy;
		Random rnd = new Random();
		this.color = Color.argb(255, 50 + rnd.nextInt(206), 50 + rnd.nextInt(206), 50 + rnd.nextInt(206));
		this.m = 75 + rnd.nextInt(50);
	}
	
	public Ball(float x, float y, int r, float vx, float vy, int colour){
		this.x = x;
		this.y = y;
		this.r = r;
		this.vx = vx;
		this.vy = vy;
		Random rnd = new Random();
		this.color = colour;
		this.m = 75 + rnd.nextInt(50);
	}
	
	public Ball(){
		this.x = 1;
		this.y = 1;
		this.r = 5;
		this.vx = 0;
		this.vy = 0;
		Random r = new Random();
		this.color = Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
		//this.color = Color.WHITE;
	}
	
	public void Render(Canvas canvas, int renderBallsOption){
		Paint p = new Paint();
		
		p.setColor(color);
		
		if (renderBallsOption == GameView.RENDER_BALLS_NO_FILL){
			p.setStyle(Style.STROKE);
			p.setStrokeMiter(3);
			p.setStrokeWidth(3);
		} else if (renderBallsOption == GameView.RENDER_BALLS_FILL){
			p.setStyle(Style.FILL_AND_STROKE);
			p.setStrokeMiter(3);
			p.setStrokeWidth(3);
		}
		
		p.setAntiAlias(true);
		
		canvas.drawCircle(x, y, r, p);
	}
}
