package edu.rutgers.winlab.headbanger.util;

public class InputArray {
	
	private long[] time;
	
	private double[] buf1;
	
	private double[] buf2;
	
	private double[] buf3;
	
	public InputArray(long[] time, double[] buf1, double[] buf2, double[] buf3){
		
		this.time = time;
		this.buf1 = buf1;
		this.buf2 = buf2;
		this.buf3 = buf3;
		
	}
	
	public InputArray(double[] buf1, double[] buf2, double[] buf3){
		
		this.buf1 = buf1;
		this.buf2 = buf2;
		this.buf3 = buf3;
		
	}
	
	public long[] getTime(){
		
		return this.time;
		
	}
	
	public double[] getbuf1(){
		
		return this.buf1;
		
	}
	
	public double[] getbuf2(){
		
		return this.buf2;
		
	}
	
	public double[] getbuf3(){
		
		return this.buf3;
		
	}

}
