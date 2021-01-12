import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Scenario {
	public int replicationNo;
	public double recoveryRate;
	public double pooledFraction;
	public double totalStockRatio;
	public String sensitivityType;
	public double sensitivityParameter;
	public KPIs kpi;

	public Scenario(int replicationNo, double recoveryRate, double pooledFraction, double totalStockRatio, String sensitivityType, double sensitivityParameter,
			KPIs kpi) {
		this.replicationNo = replicationNo;
		this.recoveryRate = recoveryRate;
		this.pooledFraction = pooledFraction;
		this.totalStockRatio = totalStockRatio;
		this.sensitivityType = sensitivityType;
		this.sensitivityParameter = sensitivityParameter;
		this.kpi = kpi;
	}

	public void print(BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.write(this.replicationNo + "," + this.recoveryRate + "," + this.pooledFraction + "," + this.totalStockRatio + ","+
				this.sensitivityType + "," + this.sensitivityParameter + ",");
		if(this.kpi.arrivalCount>0)
			{
			bufferedWriter.write((double)this.kpi.serviceSatisfiedCount / this.kpi.arrivalCount + ","
				+ (double)this.kpi.serviceSatisfiedfromInventoryCount / this.kpi.arrivalCount+",");
			}
		else
		{
			bufferedWriter.write("1.0,1.0,"); // if there is no arrivals, we assume 100% service satisfaction as no patients are missed
		}
		String printedDemand=this.kpi.demand.toString();
		String printedSafety=this.kpi.safety.toString();
		String printedPooled=this.kpi.pooled.toString();
		
		printedDemand=correct(printedDemand);
		printedSafety=correct(printedSafety);
		printedPooled=correct(printedPooled);
		
		bufferedWriter.write(this.kpi.shortageEnd+","+printedDemand+","+printedSafety+","+printedPooled+"\n");
	}

	private String correct(String cutBrackets) {
		return cutBrackets.substring(1,cutBrackets.length()-1);		
	}
}
