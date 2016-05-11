package edu.rutgers.winlab.headbanger.core;

import com.dtw.TimeWarpInfo;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import edu.rutgers.winlab.headbanger.util.InputArray;
import edu.rutgers.winlab.headbanger.util.ResultsArray;

public class DataComparison {
	
	private TimeSeries timeserie1;
	private TimeSeries timeserie2;
	
	private TimeSeries timeserie3;
	private TimeSeries timeserie4;
	
	private TimeSeries timeserie5;
	private TimeSeries timeserie6;
	
	
	private DistanceFunction distFn;
	
	private int radius;// higher the radius, higher delay, higher accuracy
	
	
	public DataComparison (String timeseriefilename1, String timeseriefilename2){
		
		
		//this.timeserie1 = new TimeSeries(timeserie1, false, false, ',');
		//this.timeserie2 = new TimeSeries(timeserie2, false, false, ',');
		int[] colnum1 = {0,1,2};
		int[] colnum2 = {1};
		int[] colnum3 = {2};
		
		this.timeserie1 = new TimeSeries(timeseriefilename1, colnum1, false);
		this.timeserie2 = new TimeSeries(timeseriefilename2, colnum1, false);
		
		/*this.timeserie3 = new TimeSeries(timeseriefilename1, colnum2, false);
		this.timeserie4 = new TimeSeries(timeseriefilename2, colnum2, false);
		
		this.timeserie5 = new TimeSeries(timeseriefilename1, colnum3, false);
		this.timeserie6 = new TimeSeries(timeseriefilename2, colnum3, false);*/
		
		this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
		
		this.radius = 10;
		
		
		
	}
	
	public DataComparison (ResultsArray ra, String timeseriefilename) {
		
		int[] colnum = {0,1,2};
		
		this.timeserie1 = new TimeSeries(ra.getbuf1(),ra.getbuf2(),ra.getbuf3());
		this.timeserie2 = new TimeSeries(timeseriefilename,colnum, false);
		
		this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
		
		this.radius = 20;
		
	}
	
	public DataComparison (ResultsArray ra, InputArray exemplaria) {
		
		int[] colnum = {0,1,2};
		
		/*this.timeserie1 = new TimeSeries(ra.getbuf1(),ra.getbuf2(),ra.getbuf3());// put x, y, z into one time series
		this.timeserie2 = new TimeSeries(exemplaria.getbuf1(), exemplaria.getbuf2(), exemplaria.getbuf3());
		*/
		this.timeserie1 = new TimeSeries(ra.getbuf1());
		this.timeserie2 = new TimeSeries(exemplaria.getbuf1());
		
		this.timeserie3 = new TimeSeries(ra.getbuf2());
		this.timeserie4 = new TimeSeries(exemplaria.getbuf2());
		
		this.timeserie5 = new TimeSeries(ra.getbuf3());
		this.timeserie6 = new TimeSeries(exemplaria.getbuf3());
		
		this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
		
		this.radius = 20;
		
	}
	
	public double[] ComputeDTW () {
		
		System.out.println("ComputeDTW time size "+this.timeserie1.size());
		TimeWarpInfo info1 = com.dtw.FastDTW.getWarpInfoBetween(this.timeserie1 , this.timeserie2, this.radius, this.distFn);
		TimeWarpInfo info2 = com.dtw.FastDTW.getWarpInfoBetween(this.timeserie3 , this.timeserie4, this.radius, this.distFn);
		TimeWarpInfo info3 = com.dtw.FastDTW.getWarpInfoBetween(this.timeserie5 , this.timeserie6, this.radius, this.distFn);
		
		double[] distance = {info1.getDistance(),info2.getDistance(), info3.getDistance()};
		
		return distance;
	}
	
	public static void main(String args[]){
		
		String file1 = "testfile/filtered_acc6.csv";
		String file2 = "testfile/filtered_acc7.csv";
		
		long starttime = 0;
		long starttime2 = 0;
		long latency = 0;
		long latency2 = 0;
		int itime = 1;
		double[] distance = null;
		
		for(int i = 0; i < itime; i++){
			
			starttime = System.currentTimeMillis();
		    
		    DataComparison dc = new DataComparison(file1, file2);
			
			//DataComparison dc = new DataComparison()
		    
		    latency = latency + System.currentTimeMillis() - starttime;
		    
		    starttime2 = System.currentTimeMillis();
		    
		    
			distance = dc.ComputeDTW();
			
			latency2 = latency2 + System.currentTimeMillis() - starttime2;
			
		}
		latency = latency/itime;
		
		latency2 = latency2/itime;
		
		long starttime3 = System.currentTimeMillis();
		
		double mag = Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+distance[2]*distance[2]);
		//System.out.println("distance: "+ distance[0]+" "+distance[1]+" "+distance[2]);
		
		long latency3 = System.currentTimeMillis() - starttime3;
		System.out.println("mag: "+mag);
		System.out.println("" + latency );
		System.out.println("" + latency2 );
		System.out.println("" + latency3 );
		
		
		
	}

}
