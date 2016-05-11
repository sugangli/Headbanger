package edu.rutgers.winlab.headbanger.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import de.fau.cs.jstk.sampled.filters.Butterworth;
import edu.rutgers.winlab.headbanger.util.InputArray;
import edu.rutgers.winlab.headbanger.util.ResultsArray;

public class DataPreprocess {
	
	private FileInputStream is;
	
	private InputArray ia;
	
	private String fmt;
	
	private double[] buf1;
	
	private double[] buf2;
	
	private double[] buf3;
	
	private int ordernum = 2;//order of ButterworthFilter
	;
	private double f1 = 10; // highcut at 10 Hz
	
	public static long time1;
	
	public long time2;
	
	
	
	int buflen; // len of the buf
	
	public DataPreprocess(int lengthtoread) {
		
		
			
			this.buf1 = new double [lengthtoread];// specify the length to read
			this.buf2 = new double [lengthtoread];
			this.buf3 = new double [lengthtoread];
			
		
	}
	
	protected int DataFiltering(double[] buf1, double[] buf2, double[] buf3, int sample_rate){
	
		Butterworth bwf = new Butterworth(this.is, ordernum, f1, true, sample_rate);
		
		int r = 0;
		try {
			
			r = bwf.readfile(this.buf1, this.buf2, this.buf3);
			//TODO output textfile for the DTW, ONLY ONE colum for each file.
			time1 = System.currentTimeMillis();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return r;
	}
	
	protected ResultsArray DataFiltering(InputArray ia, int sample_rate){
		
		
		Butterworth bwf = new Butterworth(this.ia, ordernum, f1, true, sample_rate);
		
		ResultsArray ra;
		
			ra = bwf.readarray(ia);
			//TODO output textfile for the DTW, ONLY ONE colum for each file.
			time1 = System.currentTimeMillis();
		
	
		return ra;
	}
	
	protected void OutputFilteredData(String filename, String outputfilename, int sample_rate) {
		
		
		PrintWriter writer;
		
		try {
			this.is = new FileInputStream(filename);
			
			writer = new PrintWriter(outputfilename);
			
			int r = DataFiltering(this.buf1, this.buf2, this.buf3, sample_rate);
			
			for(int i = 0; i < r; i++ ){
				
				writer.println(String.format("%.12f", buf1[i])+","+String.format("%.12f", buf2[i])+","+String.format("%.12f", buf3[i]));
				
			}
			
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	protected ResultsArray OutputFilteredDatatoArrays(String filename, int sample_rate) {
		
		
		try {
			
			this.is = new FileInputStream(filename);
			int r = DataFiltering(this.buf1, this.buf2, this.buf3, sample_rate);
			
			
			
			
			/*for (int i = 0; i < pbuf1.length; ++i){
			//out[i] = (short)(buf1[i] * scale);
			System.out.println(String.format("%.12f", pbuf1[i])+" "+String.format("%.12f", pbuf2[i])+" "+String.format("%.12f", pbuf3[i]));
			
		}*/
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResultsArray(this.buf1, this.buf2, this.buf3);
		
	}
	
	protected ResultsArray OutputFilteredDatatoArrays(InputArray ia, int sample_rate) {
		
		this.ia = ia;
			
		ResultsArray ra = DataFiltering(ia, sample_rate);
			
		return ra;
		
	}
	
	
	
	
	public static void main(String args[]){
		
		long starttime = System.currentTimeMillis();
		
		DataPreprocess dp = new DataPreprocess(500);
		
		long stoptime1 = System.currentTimeMillis();
		
		//dp.OutputFilteredData("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_30/acc.txt","testfile/filtered_acc2s.csv");
		
		int sample_rate = 50;
		ResultsArray ra = dp.OutputFilteredDatatoArrays("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_6/acc.txt", sample_rate);
		
		long stoptime2 = System.currentTimeMillis();
		for (int i = 0; i <ra.getbuf1().length; ++i){
			//out[i] = (short)(buf1[i] * scale);
			System.out.println(String.format("%.12f", ra.getbuf1()[i])+" "+String.format("%.12f", ra.getbuf2()[i])+" "+String.format("%.12f", ra.getbuf3()[i]));
			
		}
		
		
		
		long latency = time1 - starttime;
		long latency2 = stoptime2 - time1;
		
		System.out.println(""+ latency);
		System.out.println(""+ latency2);
		
	}

}
