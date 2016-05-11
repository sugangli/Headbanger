package edu.rutgers.winlab.headbanger.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.rutgers.winlab.headbanger.util.InputArray;
import edu.rutgers.winlab.headbanger.util.ResultsArray;

public class Authentication {
	
	private String filename;
	
	private double mean;
	
	private double stddev;
	
	private double nstd;
	
	public long time1;
	
	public long time2;
	
	public Authentication() {
		
		//this.filename = filename;
		
		this.mean =  152.103298795;
		this.stddev = 66.858137709;
		this.nstd = 2.7;
		
	}
	
	public int ReadThreshold(String thr_filename){
		
		int len = 0;
		
		try {
		
		BufferedReader br = new BufferedReader (new FileReader(thr_filename));
		
		String line;
		
		
			if ((line = br.readLine()) != null){
				
				String[] entry = line.split("\\s");
				
				this.mean = Double.parseDouble(entry[0]);
						
				this.stddev = Double.parseDouble(entry[1]);
				
				len = entry.length;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return len;
		
	}
	
	public boolean AuthenTest(String testfile, String filtered_testfile,  String exemplar_filteredfile, int sample_rate){
		
		DataPreprocess dp = new DataPreprocess(2000);
		
		dp.OutputFilteredData(testfile, filtered_testfile, sample_rate);
		
		DataComparison dc = new DataComparison(filtered_testfile, exemplar_filteredfile);
		
		double[] distance = dc.ComputeDTW();
		
		double mag = Math.sqrt(distance[0]*distance[0]);//+distance[1]*distance[1]+distance[2]*distance[2]);
		
		System.out.println("mag :"+mag);
		
		if(mag< (this.mean + this.nstd*this.stddev)){
			
			return true;
			
		}
		
		return false;
	}
	
	public boolean AuthenTestInMem(String test_file, String exemplar_filteredfile, int sample_rate){
		
		long starttime = System.currentTimeMillis();
		
		DataPreprocess dp = new DataPreprocess(500);
		
		ResultsArray ra = dp.OutputFilteredDatatoArrays(test_file, sample_rate);
		
		time1 = System.currentTimeMillis() - starttime;
		
		DataComparison dc = new DataComparison(ra, exemplar_filteredfile);
		
		time2 = System.currentTimeMillis() - starttime + time1;
		
		System.out.println("time1: "+time1);
		System.out.println("time2: "+time2);
		
		
		double[] distance = dc.ComputeDTW();
		
		
		double mag = Math.sqrt(distance[0]*distance[0]);//+distance[1]*distance[1]+distance[2]*distance[2]);
		
		System.out.println("mag :"+mag);
		
		if(mag< (this.mean + this.nstd*this.stddev)){
			
			return true;
			
		}
		
		return false;
		
		
		
	}
	
	public boolean AuthenTestInMem(InputArray ia, InputArray eia, int lengthtoread, int sample_rate){
		
		long starttime = System.currentTimeMillis();
		
		DataPreprocess dp = new DataPreprocess(lengthtoread);
		
		ResultsArray ra = dp.OutputFilteredDatatoArrays(ia, sample_rate);
		
		time1 = System.currentTimeMillis() - starttime;
		
		DataComparison dc = new DataComparison(ra, eia);
		
		
		
		
		double[] distance = dc.ComputeDTW();
		
		time2 = System.currentTimeMillis() - starttime + time1;
		
		long totaltime = time1+time2;
		
		System.out.println("time1: "+time1);
		System.out.println("time2: "+time2);
		System.out.println("total time:"+totaltime);
		double mag = Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+distance[2]*distance[2]);
		
		System.out.println("mag :"+mag);
		
		if(mag< (this.mean + this.nstd*this.stddev)){
			
			return true;
			
		}
		
		
		
		return false;
		
	}
	
	
	public boolean AuthenTestInMem(InputArray ia, String exemplar_filteredfile, int lengthtoread, int sample_rate){
		
		long starttime = System.currentTimeMillis();
		
		DataPreprocess dp = new DataPreprocess(lengthtoread);
		
		ResultsArray ra = dp.OutputFilteredDatatoArrays(ia, sample_rate);
		
		
		
		
		//time1 = System.currentTimeMillis() - starttime;
		
		DataComparison dc = new DataComparison(ra, exemplar_filteredfile);
		
		//time2 = System.currentTimeMillis() - starttime + time1;
		
		//System.out.println("time1: "+time1);
		//System.out.println("time2: "+time2);
		
		
		double[] distance = dc.ComputeDTW();
		
		
		double mag = Math.sqrt(distance[0]*distance[0]);//+distance[1]*distance[1]+distance[2]*distance[2]);
		
		System.out.println("mag :"+mag);
		
		if(mag< (this.mean + this.nstd*this.stddev)){
			
			return true;
			
		}
		
		return false;
		
		
		
	}
	
	public static void main(String args[]){
		
		
		
		Authentication au = new Authentication();
		
		int len = 2000;
		
		long[] time = new long[len];
		double[] buf1 = new double[len];
		double[] buf2 = new double[len];
		double[] buf3 = new double[len];
		
		
			try {
				BufferedReader br  = new BufferedReader(new FileReader("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_6/acc.txt"));
				String line;
				for (int i = 0; i < len; i++){
				
					if ((line = br.readLine()) != null){
						
						String[] entry = line.split("\\s");
						time[i] = Long.parseLong(entry[0]);
						buf1[i] = Double.parseDouble(entry[1]);
						buf2[i] = Double.parseDouble(entry[2]);
						buf3[i] = Double.parseDouble(entry[3]);
						
						System.out.println(buf1[i] + " " + buf2[i] + " " + buf3[i]);
						
						
					}
				}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		InputArray ia = new InputArray(time, buf1, buf2, buf3);
		
		for(int j = 0; j < ia.getbuf1().length ; j++){
			
			System.out.println(ia.getbuf1().length+" "+ia.getbuf1()[j] + " " + ia.getbuf2()[j] + " " + ia.getbuf3()[j]);
			
		}
		
		//boolean result = au.AuthenTest("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_6/acc.txt", "testfile/filtered_acc6.csv", "testfile/filtered_acc6.csv");//
		//boolean result = au.AuthenTestInMem("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_6/acc.txt", "testfile/filtered_acc7.csv");
		int sample_rate = 50;
		boolean result = au.AuthenTestInMem(ia, "testfile/filtered_acc7.csv",len, sample_rate);
		
		
		if(result){
			
			System.out.println("true");
			
		}else{
			
			System.out.println("false");
			
		}
		
		
		
	}
	

}
