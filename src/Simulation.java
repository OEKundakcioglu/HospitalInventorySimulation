import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Simulation {

	private Random random;
	private Hospital[] hospital;
	private int replication;
	private double systemAnnualDemandRate;
	private double[] fractionsofPooled;
	private double[] totalStockinAnnualDemand;
	private double[] recoveryRates;
	private ArrayList<Scenario> scenarios;

	public Simulation(int seedNumberforDemandGeneration, int replication, ProblemData problemData, double[] fractionsofPooled,
			double[] totalStockinAnnualDemand, double[] recoveryRates) {
		this.random = new Random();
		this.random.setSeed(seedNumberforDemandGeneration);
		this.replication=replication;
		this.hospital = problemData.getHospitals();
		this.fractionsofPooled = fractionsofPooled;
		this.totalStockinAnnualDemand = totalStockinAnnualDemand;
		this.recoveryRates = recoveryRates;
		this.systemAnnualDemandRate = 0;
		for (Hospital h : this.hospital) {
			this.systemAnnualDemandRate += h.annualDemandRate;
		}
		scenarios = new ArrayList<Scenario>();
	}

	public void simulateOptimalLevels() {
		runGenericSimulator(new double[0]);
	}

	public void sensitivityAnalysis(double[] sensitivityParameters) {
		runGenericSimulator(sensitivityParameters);
	}

	private void runGenericSimulator(double[] sensitivityParameters) {
		for (double recoveryRate : recoveryRates) {
			for (double pooledFraction : fractionsofPooled) {
				for (double totalStockRatio : totalStockinAnnualDemand) {
					double stocked = totalStockRatio * this.systemAnnualDemandRate;
					double pooled = pooledFraction * stocked;
					HashMap<Hospital, Double> thresholds = ComputeOptimal.levels(stocked - pooled, recoveryRate,
							this.hospital);
					HashMap<Hospital, Double> pooledInventories = ComputeOptimal.levels(pooled, recoveryRate,
							this.hospital);
					if (sensitivityParameters.length > 0) {
						for (double sensitivityParameter : sensitivityParameters) {
							HashMap<Hospital, Double> hmTransfer = alter(thresholds, pooledInventories,
									sensitivityParameter);
							for(int r=0;r<this.replication;r++) {
								KPIs kpi = simulate(thresholds, pooledInventories, recoveryRate);
								scenarios.add(new Scenario(r+1,recoveryRate, pooledFraction, totalStockRatio,
										sensitivityParameter, kpi));
							}
							alterBack(thresholds, pooledInventories, hmTransfer); // before the next change, we take
																					// this one back
						}
					} 
					else {
						for(int r=0;r<this.replication;r++) {
							KPIs kpi = simulate(thresholds, pooledInventories, recoveryRate);
							scenarios.add(new Scenario(r+1,recoveryRate, pooledFraction, totalStockRatio, 0, kpi));
						}
					}

				}
			}
		}
	}

	private void alterBack(HashMap<Hospital, Double> thresholds, HashMap<Hospital, Double> pooledInventories,
			HashMap<Hospital, Double> hmTransfer) {
		for (Hospital h : thresholds.keySet()) {
			double threshold = thresholds.get(h);
			double pooled = pooledInventories.get(h);
			double transfer = hmTransfer.get(h);

			double newThreshold = threshold - transfer;
			double newPooled = pooled + transfer;

			thresholds.put(h, newThreshold);
			pooledInventories.put(h, newPooled);
		}

	}

	private KPIs simulate(HashMap<Hospital, Double> thresholds, HashMap<Hospital, Double> pooledInventories,
			double recoveryRate) {
		// start with rounding to the nearest integer
		KPIs kpi = new KPIs();
		int numberofHospitals = this.hospital.length;
		HashMap<Hospital, Integer> thresholdLevel = new HashMap<Hospital, Integer>();
		HashMap<Hospital, Integer> pooledLevel = new HashMap<Hospital, Integer>();
		for (Hospital h : thresholds.keySet()) {
			thresholdLevel.put(h, (int) Math.round(thresholds.get(h)));
			pooledLevel.put(h, (int) Math.round(pooledInventories.get(h)));
		}

		double shortageEnds = getNext(recoveryRate);
		for (Hospital h : thresholds.keySet()) {
			thresholdLevel.put(h, (int) Math.round(thresholds.get(h)));
			pooledLevel.put(h, (int) Math.round(pooledInventories.get(h)));
		}

		HashMap<Hospital, Double> arrivalTime = new HashMap<Hospital, Double>();

		for (Hospital h : this.hospital) {
			arrivalTime.put(h,0.0);
		}
		
		for (Hospital h : this.hospital) {
			GenerateArrival(h, arrivalTime);
		}
		Hospital firstArrivalHospital = which(arrivalTime, shortageEnds);
		while (firstArrivalHospital != null) {
			kpi.arrivalCount=kpi.arrivalCount+1;
			if(!poolUsed(pooledLevel,firstArrivalHospital,kpi))
			{
				safetyUsed(thresholdLevel,firstArrivalHospital,kpi);
			}
			GenerateArrival(firstArrivalHospital, arrivalTime);
			firstArrivalHospital = which(arrivalTime, shortageEnds);
		}

		return kpi;
	}

	private void safetyUsed(HashMap<Hospital, Integer> thresholdLevel, Hospital firstArrivalHospital, KPIs kpi) {
		int stock=thresholdLevel.get(firstArrivalHospital);
		if(stock>0)// used from its own safety
		{
			kpi.serviceSatisfiedCount=kpi.serviceSatisfiedCount+1;
			kpi.serviceSatisfiedfromInventoryCount=kpi.serviceSatisfiedfromInventoryCount+1;
			thresholdLevel.put(firstArrivalHospital, stock-1);
		}
	}

	private boolean poolUsed(HashMap<Hospital, Integer> pooledLevel, Hospital firstArrivalHospital, KPIs kpi) {
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
			}
			else
			{
				//find some hospital!
				Hospital bestHosp = findAbundant(pooledLevel);
				int bestStatusLevel = pooledLevel.get(bestHosp);
				pooledLevel.put(bestHosp, bestStatusLevel-1);
			}
		}
		return used;
	}

	private Hospital findAbundant(HashMap<Hospital, Integer> pooledLevel) {
		double ratio=0;
		Hospital best=null;
		for (Hospital h : pooledLevel.keySet()) {
			double thisratio=pooledLevel.get(h)/h.annualDemandRate;
			if(thisratio>ratio)
			{
				best=h;
				ratio=thisratio;
			}
		}
		return best;
	}

	private void GenerateArrival(Hospital hospital, HashMap<Hospital, Double> arrivalTime) {
		double thisTime = arrivalTime.get(hospital);
		thisTime += getNext(hospital.annualDemandRate);
		arrivalTime.put(hospital, thisTime);
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

	private HashMap<Hospital, Double> alter(HashMap<Hospital, Double> thresholds,
			HashMap<Hospital, Double> pooledInventories, double delta) {
		// deviate from optimal levels: transfer from pooled to safety stock
		// if negative, transfer from safety stock to pooled
		HashMap<Hospital, Double> hmTransfer = new HashMap<Hospital, Double>();
		for (Hospital h : thresholds.keySet()) {
			double threshold = thresholds.get(h);
			double pooled = pooledInventories.get(h);
			double transfer = 0;
			if (delta > 0)
				transfer = pooled * delta;
			else
				transfer = threshold * delta;

			hmTransfer.put(h, transfer);

			double newThreshold = threshold + transfer;
			double newPooled = pooled - transfer;

			thresholds.put(h, newThreshold);
			pooledInventories.put(h, newPooled);
		}
		return hmTransfer;
	}

	public void reportKPIs() throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("output.csv"));
		bufferedWriter.write("replicationNo, recoveryRate, pooledFraction, totalStockRatio, sensitivityParameter, Type I, Type II\n");
		for(Scenario s:scenarios)
		{
			s.print(bufferedWriter);
		}
		bufferedWriter.close();
	}

}
