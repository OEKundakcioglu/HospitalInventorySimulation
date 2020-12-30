import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PerformAllSimulations {

	private Random random;
	private Hospital[] hospital;
	private int replication;
	private double systemAnnualDemandRate;
	private double[] fractionsofPooled;
	private double[] totalStockinAnnualDemand;
	private double[] recoveryRates;
	private ArrayList<Scenario> scenarios;
	private String outFile;

	public PerformAllSimulations(int seedNumberforDemandGeneration, int replication, ProblemData problemData, double[] fractionsofPooled,
			double[] totalStockinAnnualDemand, double[] recoveryRates, String outFile) {
		this.random = new Random();
		this.random.setSeed(seedNumberforDemandGeneration);
		this.replication=replication;
		this.hospital = problemData.hospital;
		this.fractionsofPooled = fractionsofPooled;
		this.totalStockinAnnualDemand = totalStockinAnnualDemand;
		this.recoveryRates = recoveryRates;
		this.systemAnnualDemandRate = 0;
		for (Hospital h : this.hospital) {
			this.systemAnnualDemandRate += h.getDemand();
		}
		scenarios = new ArrayList<Scenario>();
		this.outFile=outFile;
	}

	public void simulateOptimalLevels() throws IOException {
		runGenericSimulator(new double[0]);
	}

	public void sensitivityAnalysis(double[] sensitivityParameters) throws IOException {
		runGenericSimulator(sensitivityParameters);
	}

	private void runGenericSimulator(double[] sensitivityParameters) throws IOException {
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
								Simulate simulate = new Simulate(this.random, thresholds, pooledInventories, recoveryRate);
								KPIs kpi = simulate.kpi;
								scenarios.add(new Scenario(r+1,recoveryRate, pooledFraction, totalStockRatio,
										sensitivityParameter, kpi));
							}
							alterBack(thresholds, pooledInventories, hmTransfer); // before the next change, we take
																					// this one back
						}
					} 
					else {
						for(int r=0;r<this.replication;r++) {
							Simulate simulate = new Simulate(this.random, thresholds, pooledInventories, recoveryRate); 
							KPIs kpi = simulate.kpi;
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
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.outFile));
		bufferedWriter.write("replicationNo, recoveryRate, pooledFraction, totalStockRatio, sensitivityParameter, Type I, Type II,	shortageEnd");
		for(int i=0;i<hospital.length;i++)
		{
			bufferedWriter.write(", demand "+(i+1));
		}
		for(int i=0;i<hospital.length;i++)
		{
			bufferedWriter.write(", safety "+(i+1));
		}
		for(int i=0;i<hospital.length;i++)
		{
			bufferedWriter.write(", pooled "+(i+1));
		}
		bufferedWriter.write("\n");
		for(Scenario s:scenarios)
		{
			s.print(bufferedWriter);
		}
		bufferedWriter.close();
	}

}
