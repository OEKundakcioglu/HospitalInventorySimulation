import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Scenario {
	public int replicationNo;
	public double recoveryRate;
	public double pooledFraction;
	public double totalStockRatio;
	public double sensitivityParameter;
	public KPIs kpi;

	public Scenario(int replicationNo, double recoveryRate, double pooledFraction, double totalStockRatio, double sensitivityParameter,
			KPIs kpi) {
		this.replicationNo = replicationNo;
		this.recoveryRate = recoveryRate;
		this.pooledFraction = pooledFraction;
		this.totalStockRatio = totalStockRatio;
		this.sensitivityParameter = sensitivityParameter;
		this.kpi = kpi;
	}

	public void print(BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.write(this.replicationNo + "," + this.recoveryRate + "," + this.pooledFraction + "," + this.totalStockRatio + ","
				+ this.sensitivityParameter + ",");
		if(this.kpi.arrivalCount>0)
			bufferedWriter.write((double)this.kpi.serviceSatisfiedCount / this.kpi.arrivalCount + ","
				+ (double)this.kpi.serviceSatisfiedfromInventoryCount / this.kpi.arrivalCount);
		bufferedWriter.write("\n");

	}
}
