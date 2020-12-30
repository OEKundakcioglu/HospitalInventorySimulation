public class ProblemData {
	public Hospital[] hospital;
	private int numberofHospitals;

	public ProblemData(double[] annualDemandRates) {
		this.numberofHospitals = annualDemandRates.length;
		this.hospital = new Hospital[this.numberofHospitals];
		for (int i = 0; i < this.numberofHospitals; i++) {
			this.hospital[i] = new Hospital(i, annualDemandRates[i]);
		}
	}
}
