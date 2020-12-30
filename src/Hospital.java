
public class Hospital {
	public int id;
	private double annualDemandRate;

	public Hospital(int id, double annualDemandRate) {
		this.id = id;
		this.annualDemandRate = annualDemandRate;
	}
	
	public double getDemand() {
		return this.annualDemandRate;
	}
}
