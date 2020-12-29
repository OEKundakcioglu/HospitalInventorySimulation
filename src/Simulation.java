import java.util.Random;

public class Simulation {

	private Random random;
	private Hospital[] hospital;
	private double totalDemand;
	private double[] fractionsofPooled;
	private double[] totalStockinAnnualDemand;
	
	
	public Simulation(int seedNumberforDemandGeneration, ProblemData problemData,double[] fractionsofPooled, double[] totalStockinAnnualDemand) {
		this.random=new Random();
		this.random.setSeed(seedNumberforDemandGeneration);
		this.hospital=problemData.getHospitals();
		this.fractionsofPooled=fractionsofPooled;
		this.totalStockinAnnualDemand=totalStockinAnnualDemand;
		this.totalDemand=0;
		for(Hospital h:this.hospital)
		{
			this.totalDemand+=h.annualDemandRate;
		}
	}

	public void simulateOptimalLevels() {
	
		
		//optimal level'i pasla, diger parametrelerle vs (her hastane icin guncellesin o kritik seviyeleri!!  simulator ayni olsun!!!! alttakinde de ceviriip aynisini cagiralim!!!
	}

	public void sensitivityAnalysis(double sensitivityIncrements, int sensitivityNumberofSteps) {
		// TODO Auto-generated method stub
		
	}

	public void reportKPIs() {
		// TODO Auto-generated method stub
		
	}

}
