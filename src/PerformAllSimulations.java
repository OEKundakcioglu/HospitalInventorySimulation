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
	private ArrayList<Hospital> sortedRank;
	private double epsilon;

	public PerformAllSimulations(int seedNumberforDemandGeneration, int replication, ProblemData problemData,
			double[] fractionsofPooled, double[] totalStockinAnnualDemand, double[] recoveryRates, String outFile,
			double epsilon) {
		this.random = new Random();
		this.random.setSeed(seedNumberforDemandGeneration);
		this.replication = replication;
		this.hospital = problemData.hospital;
		this.fractionsofPooled = fractionsofPooled;
		this.totalStockinAnnualDemand = totalStockinAnnualDemand;
		this.recoveryRates = recoveryRates;
		this.systemAnnualDemandRate = 0;
		this.sortedRank = sortHospitalsBasedOnDemand();
		for (Hospital h : this.hospital) {
			this.systemAnnualDemandRate += h.getDemand();
		}
		scenarios = new ArrayList<Scenario>();
		this.outFile = outFile;
		this.epsilon = epsilon;
	}

	private ArrayList<Hospital> sortHospitalsBasedOnDemand() {
		ArrayList<Hospital> sr = new ArrayList<Hospital>();
		HashMap<Hospital, Double> demands = new HashMap<Hospital, Double>();
		for (Hospital h : this.hospital) {
			demands.put(h, h.getDemand());
		}
		while (demands.size() > 0) {
			double maxVal = 0;
			Hospital best = null;
			for (Hospital h : demands.keySet()) {
				double thisdemand = demands.get(h);
				if (thisdemand > maxVal) {
					best = h;
					maxVal = thisdemand;
				}
			}
			demands.remove(best);
			sr.add(best);
		}

		return sr;
	}

	public void simulateOptimalLevels() throws IOException {
		runGenericSimulator(new double[0], new double[0]); // optimal and linear allocations are computed here
	}

	public void sensitivityAnalysis(double[] poolSensitivityParameters, double[] thresholdSensitivityParameters)
			throws IOException {
		runGenericSimulator(poolSensitivityParameters, thresholdSensitivityParameters);
	}

	private void runGenericSimulator(double[] poolSensitivityParameters, double[] thresholdSensitivityParameters)
			throws IOException {
		for (double recoveryRate : recoveryRates) {
			for (double pooledFraction : fractionsofPooled) {
				for (double totalStockRatio : totalStockinAnnualDemand) {
					double stocked = totalStockRatio * this.systemAnnualDemandRate;
					double pooled = pooledFraction * stocked;
					HashMap<Hospital, Double> thresholds = ComputeOptimal.levels(stocked - pooled, recoveryRate,
							this.hospital);
					HashMap<Hospital, Double> pooledInventories = ComputeOptimal.levels(pooled, recoveryRate,
							this.hospital);
					if (poolSensitivityParameters.length + thresholdSensitivityParameters.length > 0) {
						if (pooledFraction > this.epsilon) // if nothing pooled don't do the following
						{
							for (double sensitivityParameter : poolSensitivityParameters) {
								alterSimulateAlterBack(thresholds, pooledInventories, sensitivityParameter,
										StockType.POOL, recoveryRate, pooledFraction, totalStockRatio);
							}
						}
						if (pooledFraction < 1 - this.epsilon) // if all pooled don't do the following
						{
							for (double sensitivityParameter : thresholdSensitivityParameters) {
								alterSimulateAlterBack(thresholds, pooledInventories, sensitivityParameter,
										StockType.SAFETY, recoveryRate, pooledFraction, totalStockRatio);
							}
						}
					} else {
						// OPTIMAL DIST'N
						for (int r = 0; r < this.replication; r++) {
							Simulate simulate = new Simulate(this.random, thresholds, pooledInventories, recoveryRate);
							KPIs kpi = simulate.kpi;
							scenarios.add(new Scenario(r + 1, recoveryRate, pooledFraction, totalStockRatio, "optimal",
									0, kpi));
						}
						// LINEAR DIST'N
						for (int r = 0; r < this.replication; r++) {
							HashMap<Hospital, Double> thresholdsLin = ComputeLinear.levels(stocked - pooled,
									recoveryRate, this.hospital);
							HashMap<Hospital, Double> pooledInventoriesLin = ComputeLinear.levels(pooled, recoveryRate,
									this.hospital);
							Simulate simulate = new Simulate(this.random, thresholdsLin, pooledInventoriesLin,
									recoveryRate);
							KPIs kpi = simulate.kpi;
							scenarios.add(new Scenario(r + 1, recoveryRate, pooledFraction, totalStockRatio, "linear",
									0, kpi));
						}
					}
				}
			}
		}
	}

	private void alterSimulateAlterBack(HashMap<Hospital, Double> thresholds,
			HashMap<Hospital, Double> pooledInventories, double sensitivityParameter, StockType st, double recoveryRate,
			double pooledFraction, double totalStockRatio) throws IOException {
		HashMap<Hospital, Double> hmTransfer = alter(thresholds, pooledInventories, sensitivityParameter, st);
		for (int r = 0; r < this.replication; r++) {
			Simulate simulate = new Simulate(this.random, thresholds, pooledInventories, recoveryRate);
			KPIs kpi = simulate.kpi;
			this.scenarios.add(new Scenario(r + 1, recoveryRate, pooledFraction, totalStockRatio, st.name(),
					sensitivityParameter, kpi));
		}
		alterBack(thresholds, pooledInventories, hmTransfer, st); // before the next
																	// change, we take it back!
	}

	private void alterBack(HashMap<Hospital, Double> thresholds, HashMap<Hospital, Double> pooledInventories,
			HashMap<Hospital, Double> hmTransfer, StockType st) {

		for (Hospital h : hmTransfer.keySet()) {
			if (st == StockType.POOL) {
				pooledInventories.put(h, pooledInventories.get(h) + hmTransfer.get(h));
			}
			if (st == StockType.SAFETY) {
				thresholds.put(h, thresholds.get(h) + hmTransfer.get(h));
			}
		}
	}

	private HashMap<Hospital, Double> alter(HashMap<Hospital, Double> thresholds,
			HashMap<Hospital, Double> pooledInventories, double delta, StockType st) {
		HashMap<Hospital, Double> hmTransfer = new HashMap<Hospital, Double>();
		for (int i = 0; i < this.hospital.length / 2; i++) {
			Hospital fromHosp = null;
			Hospital toHosp = null;
			double amount = 0;

			if (st == StockType.POOL) {
				fromHosp = this.sortedRank.get(i);
				toHosp = this.sortedRank.get(this.hospital.length - i - 1);
				amount = pooledInventories.get(fromHosp) * delta;
				pooledInventories.put(fromHosp, pooledInventories.get(fromHosp) - amount);
				pooledInventories.put(toHosp, pooledInventories.get(toHosp) + amount);
			}
			if (st == StockType.SAFETY) {
				fromHosp = this.sortedRank.get(i);
				toHosp = this.sortedRank.get(this.hospital.length - i - 1);
				amount = thresholds.get(fromHosp) * delta;
				thresholds.put(fromHosp, thresholds.get(fromHosp) - amount);
				thresholds.put(toHosp, thresholds.get(toHosp) + amount);
			}
			hmTransfer.put(fromHosp, amount);
			hmTransfer.put(toHosp, -amount);

		}
		return hmTransfer;
	}

	public void reportKPIs() throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.outFile));
		bufferedWriter.write(
				"replicationNo,recoveryRate,pooledFraction,totalStockRatio,sensitivityType,sensitivityParameter,Type I,Type II,shortageEnd");
		for (int i = 0; i < hospital.length; i++) {
			bufferedWriter.write(",demand " + (i + 1));
		}
		for (int i = 0; i < hospital.length; i++) {
			bufferedWriter.write(",safety " + (i + 1));
		}
		for (int i = 0; i < hospital.length; i++) {
			bufferedWriter.write(",pooled " + (i + 1));
		}
		bufferedWriter.write("\n");
		for (Scenario s : scenarios) {
			s.print(bufferedWriter);
		}
		bufferedWriter.close();
	}

}
