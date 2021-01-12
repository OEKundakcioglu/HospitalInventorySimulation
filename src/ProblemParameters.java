
public class ProblemParameters {

	// parameters for random generation
	public int seedNumberforDemandGeneration = 1;
	public int replication = 1000;

	// demand and recovery parameters

	public double[][] annualDemandRateAlternatives = { { 530, 210, 94 }, { 940, 380, 9 }, { 940, 380, 9 },
			{ 5980, 440, 14 }, { 760, 430 }, { 50, 60, 70, 150, 160, 170 } };
	public double[] annualDemandRates;
	public String outFilePre = "results/output";
	public String outFile = "";

	public double[] recoveryRates = { 1, 2, 3, 4, 5, 6 };

	// inventory parameters to be plotted
	public double[] totalStockinAnnualDemand = { 0.25, 0.5, 0.75, 1 };
	public double[] fractionsofPooled = { 0, 0.25, 0.5, 0.75, 1 };

	// sensitivity parameters to analyze deviation from optimal levels to transfer
	// from pooled to reserved
	public double[] poolSensitivityParameters = { 0.1, 0.2 };
	public double[] thresholdSensitivityParameters = { 0.1, 0.2 };

	public ProblemParameters(int i) {
		this.annualDemandRates = this.annualDemandRateAlternatives[i];
		outFile = outFilePre + (i + 1) + ".csv";
	}
}
