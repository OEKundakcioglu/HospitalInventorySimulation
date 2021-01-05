// import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class Simulate extends KPIs {

	public KPIs kpi;
	private Random random;
	// private BufferedWriter bufferedWriter;
	
	public Simulate(Random random, HashMap<Hospital, Double> thresholds, HashMap<Hospital, Double> pooledInventories,
			double recoveryRate) throws IOException {
		// this.bufferedWriter = new BufferedWriter(new FileWriter("log.txt"));
		this.kpi = new KPIs();
		this.random=random;
		// start with rounding to the nearest integer
		int numberofHospitals = thresholds.keySet().size();
		HashMap<Hospital, Integer> thresholdLevel = new HashMap<Hospital, Integer>();
		HashMap<Hospital, Integer> pooledLevel = new HashMap<Hospital, Integer>();
		for (Hospital h : thresholds.keySet()) {
			int threshold=(int) Math.round(thresholds.get(h));
			int pooled = (int) Math.round(pooledInventories.get(h));
			thresholdLevel.put(h, threshold);
			pooledLevel.put(h, pooled);
			this.kpi.demand.add(h.getDemand());
			this.kpi.safety.add(threshold);
			this.kpi.pooled.add(pooled);
			// this.bufferedWriter.write("Hospital "+h.id+" pooled "+pooled+" safety "+threshold+"\n");
		}

		double shortageEnd = getNext(recoveryRate);
		
		HashMap<Hospital, Double> arrivalTime = new HashMap<Hospital, Double>();

		for (Hospital h : thresholds.keySet()) {
			arrivalTime.put(h,0.0);
		}
		
		for (Hospital h : thresholds.keySet()) {
			GenerateArrival(h, arrivalTime);
		}
		Hospital firstArrivalHospital = which(arrivalTime, shortageEnd);
		while (firstArrivalHospital != null) {
			// this.bufferedWriter.write("Next arrival is at hospital "+firstArrivalHospital.id+" pooled " +pooledLevel.get(firstArrivalHospital)+" safety " +thresholdLevel.get(firstArrivalHospital)+"\n");
			kpi.arrivalCount=kpi.arrivalCount+1;
			if(!poolUsed(pooledLevel,firstArrivalHospital,kpi))
			{
				safetyUsed(thresholdLevel,firstArrivalHospital,kpi);
			}
			
			// Alternative 1. All are generated once
			// GenerateArrival(firstArrivalHospital, arrivalTime);
			// Alternative 2. All are regenerated with the next arrival event to any hospital
			// due to memoryless property, each hospital & shortage end is refreshed
			shortageEnd = GenerateArrivalWithMemoryless(firstArrivalHospital, arrivalTime, shortageEnd,recoveryRate);
			firstArrivalHospital = which(arrivalTime, shortageEnd);
		}
		this.kpi.shortageEnd=shortageEnd;
		// this.bufferedWriter.write("Shortage ends at "+shortageEnds+"\n");
		
		// this.bufferedWriter.close();
		
	}

	private double GenerateArrivalWithMemoryless(Hospital firstArrivalHospital, HashMap<Hospital, Double> arrivalTime, double shortageEnd, double recoveryRate) {
		double thisTime = arrivalTime.get(firstArrivalHospital);
		for(Hospital h:arrivalTime.keySet())
		{
			arrivalTime.put(h, thisTime+getNext(h.getDemand()));
		}
		// this.bufferedWriter.write("Expecting arrival at "+thisTime+" for hospital "+hospital.id+"\n");
		return thisTime+getNext(recoveryRate);
	}



	private Hospital which(HashMap<Hospital, Double> arrivalTime, double shortageEnds) {
		double res = shortageEnds;
		Hospital ret = null;
		for (Hospital h : arrivalTime.keySet()) {
			double thisres = arrivalTime.get(h);
			if (thisres < res) {
				res = thisres;
				ret = h;
			}
		}
		return ret;
	}

	public double getNext(double rate) {
		return Math.log(1 - this.random.nextDouble()) / (-rate);
	}
	private void GenerateArrival(Hospital hospital, HashMap<Hospital, Double> arrivalTime) throws IOException {
		double thisTime = arrivalTime.get(hospital);
		thisTime += getNext(hospital.getDemand());
		arrivalTime.put(hospital, thisTime);
		// this.bufferedWriter.write("Expecting arrival at "+thisTime+" for hospital "+hospital.id+"\n");
	}

	private void safetyUsed(HashMap<Hospital, Integer> thresholdLevel, Hospital firstArrivalHospital, KPIs kpi) throws IOException {
		int stock=thresholdLevel.get(firstArrivalHospital);
		if(stock>0)// used from its own safety
		{
			kpi.serviceSatisfiedCount=kpi.serviceSatisfiedCount+1;
			kpi.serviceSatisfiedfromInventoryCount=kpi.serviceSatisfiedfromInventoryCount+1;
			thresholdLevel.put(firstArrivalHospital, stock-1);
			// this.bufferedWriter.write("own safety used and now at "+ (stock-1)+"\n");
		}
	}

	private boolean poolUsed(HashMap<Hospital, Integer> pooledLevel, Hospital firstArrivalHospital, KPIs kpi) throws IOException {
		boolean used=false;
		int totalPooled=0;
		for(Hospital h:pooledLevel.keySet())
		{
			totalPooled+=pooledLevel.get(h);
		}
		if(totalPooled>0)
		{
			used=true;
			kpi.serviceSatisfiedCount=kpi.serviceSatisfiedCount+1; //somehow satisfied from the pool
			int stock=pooledLevel.get(firstArrivalHospital);
			if(stock>0)// used from its own pool
			{
				kpi.serviceSatisfiedfromInventoryCount=kpi.serviceSatisfiedfromInventoryCount+1;
				pooledLevel.put(firstArrivalHospital, stock-1);
				// this.bufferedWriter.write("own pool used and now at "+ (stock-1)+"\n");
			}
			else
			{
				//find some hospital!
				Hospital bestHosp = findAbundant(pooledLevel);
				int bestStatusLevel = pooledLevel.get(bestHosp);
				pooledLevel.put(bestHosp, bestStatusLevel-1);
				// this.bufferedWriter.write("pool of "+ bestHosp.id +" used and now at "+ (bestStatusLevel-1)+"\n");
			}
		}
		return used;
	}

	private Hospital findAbundant(HashMap<Hospital, Integer> pooledLevel) {
		double ratio=0;
		Hospital best=null;
		for (Hospital h : pooledLevel.keySet()) {
			double thisratio=pooledLevel.get(h)/h.getDemand();
			if(thisratio>ratio)
			{
				best=h;
				ratio=thisratio;
			}
		}
		return best;
	}

}
