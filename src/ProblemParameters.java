
public class ProblemParameters {

	// parameters for random generation
	public int seedNumberforDataGeneration = 1;
	public int seedNumberforDemandGeneration = 1;
	public int replication = 10;

	// demand and recovery parameters
	public double[] annualDemandRates = { 940.0, 380.0, 9.0, 5980.0, 440.0, 14.0 };
	public double[] recoveryRates = { 1, 2, 3, 4, 5, 6 };

	// inventory parameters to be plotted
	public double[] totalStockinAnnualDemand = { 0.25, 0.5, 0.75, 1 };
	public double[] fractionsofPooled = { 0, 0.25, 0.5, 0.75, 1 };

	// sensitivity parameters to analyze deviation from optimal levels to transfer
	// from pooled to reserved
	public double[] sensitivityParameters = { -0.2, -0.1, 0.1, 0.2 };
}
