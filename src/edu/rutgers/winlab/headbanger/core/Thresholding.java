package edu.rutgers.winlab.headbanger.core;

import java.util.ArrayList;

public class Thresholding {
	
	private double threshold;
	
	private String filepath;
	
	private double[][] DTWMatrix;
	
	private int datasize;
	
	private double mean;
	
	private double stddev;
	
	public Thresholding(String filepath, int datasize){
		
		this.filepath = filepath;
		
		this.datasize = datasize;
		
		this.DTWMatrix = new double[datasize][datasize];// training data size
		
		
	}
	
	public int ConstructExemplar(){
		
		int Exemplarnum=0;
		
		String filename1 = null;
		
		String filename2 = null;
		
		double[] DTWSumlist = new double[datasize];
		
		//ArrayList<Double> DTWSumlist = new ArrayList<Double>();
		
		
		for(int i = 1; i <= datasize; i++){
			
			filename1 = filepath + "/filtered_acc"+i+".csv";//"/data_" + i + "/acc.txt";
			
			System.out.println("filename1"+filename1);
			
			for(int j = 1; j <= datasize; j++){// 
				
					if(j != i) {
						
						filename2 = filepath + "/filtered_acc"+j+".csv";
						
						DataComparison dc = new DataComparison(filename1, filename2);
						
						double[] distance = dc.ComputeDTW();
						
						double mag = Math.sqrt(distance[0]*distance[0]);//+distance[1]*distance[1]+distance[2]*distance[2]);
						
						//System.out.println("mag: " + mag);
						
						this.DTWMatrix[i-1][j-1] = mag;
						
						DTWSumlist[i-1] = DTWSumlist[i-1] + mag;
					
					
					}				
				
			}
			
		   
		}
		
		// find the largest number and its index in the array 
		double smallest = DTWSumlist[0];
		
		for ( int i = 0; i < DTWSumlist.length ; i++) {
			
			System.out.println("DTWSum" + i + " " + DTWSumlist[i]);
			
			if (smallest >= DTWSumlist[i]){
				
				smallest = DTWSumlist[i];
				
				Exemplarnum = i+1;
				
			}
			
			
			
		}
		
		
		
		mean = smallest/(DTWSumlist.length-1);
		
		System.out.println("mean :"+mean);
		
		double fvalue = 0;
		
		for ( int i = 0; i < datasize ; i++){
			
			fvalue = fvalue + Math.pow((this.DTWMatrix[Exemplarnum-1][i] - mean), 2);
			
		}
		
		
		stddev = Math.sqrt(fvalue/datasize);
		
		System.out.println("stddev :"+stddev);
		
		this.threshold = mean + 3 * stddev;
		
		
		return Exemplarnum;
	}
	
	
	public void SetThreshold(double threshold ){//It should be called at Training Phase
		
		this.threshold = threshold;
	}
	
	public double GetThreshold(){//It should be called at authenticate phases
		
		return this.threshold;
		
	}
	
	public static void main(String args[]){
		
		DataPreprocess dp = new DataPreprocess(2000);
		
		int sample_rate = 50;
		
		for (int i = 1; i <=40 ; i++){
			
			dp.OutputFilteredData("/home/sugang/Documents/glass_sugang/data/music_movement/somebody/nodding_sub1/data_"+i+"/acc.txt","/home/sugang/Documents/glass_data/"+"/filtered_acc"+i+".csv", sample_rate);
			
		}
		
		Thresholding thg = new Thresholding("/home/sugang/Documents/glass_data", 40);
		
		int index = thg.ConstructExemplar();
		
		double th = thg.GetThreshold();
		
		System.out.println("Exemplar Num: " + index + " th: " + th);
		
		
	}

}
