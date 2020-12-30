import java.util.Random;

public class ProblemData {
	private Random random;
	private Hospital[] hospital;
	private int numberofHospitals;

	public ProblemData(int seedNumberforRandomGeneration, double[] annualDemandRates) {
		this.random = new Random();
		this.random.setSeed(seedNumberforRandomGeneration);
		this.numberofHospitals = annualDemandRates.length;
		this.hospital = new Hospital[this.numberofHospitals];
		for (int i = 0; i < this.numberofHospitals; i++) {
			this.hospital[i] = new Hospital(i, annualDemandRates[i]);
		}
	}

	public Hospital[] getHospitals() {
		return this.hospital;
	}
}
