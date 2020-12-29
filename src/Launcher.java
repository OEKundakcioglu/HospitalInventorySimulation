public class Launcher {
	
	
	public static void main(String[] args) {
		int seedNumberforDataGeneration=1;
		int seedNumberforDemandGeneration=1;
		double[] annualDemandRates = {530.0 ,210.0 ,94.0};
		
		double[] totalStockinAnnualDemand = {0.25, 0.5, 0.75, 1};
		double[] fractionsofPooled = {0, 0.25, 0.5, 0.75, 1};
		
		// these indicate the deviation from optimal levels
		double sensitivityIncrements=0.25;
		int sensitivityNumberofSteps=2;
	
		ProblemData problemData = new ProblemData(seedNumberforDataGeneration,annualDemandRates);
		
		Simulation simulation = new Simulation(seedNumberforDemandGeneration,problemData,fractionsofPooled,totalStockinAnnualDemand);
		simulation.simulateOptimalLevels();
		simulation.sensitivityAnalysis(sensitivityIncrements,sensitivityNumberofSteps);	
		simulation.reportKPIs();
	}

}
