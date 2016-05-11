package edu.rutgers.winlab.headbanger.util;

public class ResultsArray {
	
	private double[] buf1;
	private double[] buf2;
	private double[] buf3;
	
	public ResultsArray(double[] buf1, double[] buf2, double[] buf3){
		
		this.buf1 = buf1;
		this.buf2 = buf2;
		this.buf3 = buf3;
		
	}
	
	public double[] getbuf1(){
		
		return buf1;
		
	}
	
	public double[] getbuf2(){
		
		return buf2;
		
	}
	
	public double[] getbuf3(){
		
		return buf3;
		
	}
	
	

}
