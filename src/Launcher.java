import java.io.IOException;

public class Launcher {

	public static void main(String[] args) throws IOException {

		ProblemParameters problemParameters = new ProblemParameters();
		
		for (int i = 0; i < problemParameters.annualDemandRateAlternatives.length; i++) {
			problemParameters.setProblemParameters(i);
			ProblemData problemData = new ProblemData(problemParameters.annualDemandRates);
			PerformAllSimulations simulation = new PerformAllSimulations(
					problemParameters.seedNumberforDemandGeneration, problemParameters.replication, problemData,
					problemParameters.fractionsofPooled, problemParameters.totalStockinAnnualDemand,
					problemParameters.recoveryRates, problemParameters.outFile, problemParameters.epsilon);
			simulation.simulateOptimalLevels();
			simulation.sensitivityAnalysis(problemParameters.poolSensitivityParameters,
					problemParameters.thresholdSensitivityParameters);
			simulation.reportKPIs();
		}

	}

}
