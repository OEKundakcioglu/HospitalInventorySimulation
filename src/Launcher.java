import java.io.IOException;

public class Launcher {
	
	
	public static void main(String[] args) throws IOException {
		ProblemParameters problemParameters=new ProblemParameters();
		ProblemData problemData = new ProblemData(problemParameters.seedNumberforDataGeneration,problemParameters.annualDemandRates);
		Simulation simulation = new Simulation(problemParameters.seedNumberforDemandGeneration,problemParameters.replication,problemData,problemParameters.fractionsofPooled,problemParameters.totalStockinAnnualDemand,problemParameters.recoveryRates);
		simulation.simulateOptimalLevels();
		simulation.sensitivityAnalysis(problemParameters.sensitivityParameters);	
		simulation.reportKPIs();
	}

}
