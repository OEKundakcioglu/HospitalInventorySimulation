import java.io.IOException;

public class Launcher {

	public static void main(String[] args) throws IOException {

		for (int i = 0; i < 6; i++) {
			ProblemParameters problemParameters = new ProblemParameters(i);
			ProblemData problemData = new ProblemData(problemParameters.annualDemandRates);
			PerformAllSimulations simulation = new PerformAllSimulations(
					problemParameters.seedNumberforDemandGeneration, problemParameters.replication, problemData,
					problemParameters.fractionsofPooled, problemParameters.totalStockinAnnualDemand,
					problemParameters.recoveryRates, problemParameters.outFile);
			simulation.simulateOptimalLevels();
			simulation.sensitivityAnalysis(problemParameters.poolSensitivityParameters,
					problemParameters.thresholdSensitivityParameters);
			simulation.reportKPIs();
		}

	}

}
