
public class ProblemParameters {

	// parameters for random generation
	public int seedNumberforDemandGeneration = 1;
	public int replication = 100;

	// demand and recovery parameters
	
	public double[] annualDemandRates = { 530, 210, 94};
	public String outFile = "output1.csv";
/*	public double[] annualDemandRates = { 940,380,9};
	public String outFile = "output2.csv";
	public double[] annualDemandRates = { 5980,440,14};
	public String outFile = "output3.csv";
	public double[] annualDemandRates = { 760, 430 };
	public String outFile = "output4.csv";
	public double[] annualDemandRates = { 50, 60, 70, 150, 160, 170  };
	public String outFile = "output5.csv";
*/	
	public double[] recoveryRates = { 1, 2, 3, 4, 5, 6 };

	// inventory parameters to be plotted
	public double[] totalStockinAnnualDemand = { 0.25, 0.5, 0.75, 1 };
	public double[] fractionsofPooled = { 0, 0.25, 0.5, 0.75, 1 };

	// sensitivity parameters to analyze deviation from optimal levels to transfer
	// from pooled to reserved
	public double[] sensitivityParameters = { -0.2, -0.1, 0.1, 0.2 };
}
