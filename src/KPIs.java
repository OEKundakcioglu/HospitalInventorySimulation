import java.util.ArrayList;

public class KPIs {
	public int serviceSatisfiedCount;
	public int serviceSatisfiedfromInventoryCount;
	public int arrivalCount;
	public double shortageEnd;
	ArrayList<Double> demand;
	ArrayList<Integer> pooled;
	ArrayList<Integer> safety;
	public KPIs(){
		this.demand=new ArrayList<Double>();
		this.pooled=new ArrayList<Integer>();
		this.safety=new ArrayList<Integer>();
	}
}
