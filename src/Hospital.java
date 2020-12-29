
public class Hospital {
	public int id;
	public double annualDemandRate;
	public double optThreshold; //as per Theorem 2, given total threshold quantity
	public double optPooledInventory; //as per Theorem 5, given total pooled quantity
	
	public Hospital(int id, double annualDemandRate) {
		this.id=id;
		this.annualDemandRate=annualDemandRate;
	}
}
