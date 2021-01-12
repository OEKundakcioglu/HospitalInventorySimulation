import java.util.ArrayList;
import java.util.HashMap;

public class ComputeLinear {
	public static HashMap<Hospital, Double> levels(double tobeDistributed, double recoveryRate, Hospital[] hospital) {
		HashMap<Hospital, Double> hm = new HashMap<Hospital, Double>();
		int numberofHospitals = hospital.length;
		if (tobeDistributed > 0) {
			double totalDemand = 0;
			for (Hospital h : hospital) {
				totalDemand = totalDemand + h.getDemand();
			}
			for (Hospital h : hospital) {
				hm.put(h, h.getDemand() * tobeDistributed / totalDemand);
			}
		} else {
			for (Hospital h : hospital) {
				hm.put(h, 0.0);
			}
		}
		return hm;
	}

	private static double[] convert(ArrayList<Double> a) {
		int size = a.size();
		double[] result = new double[size];
		for (int i = 0; i < size; i++) {
			result[i] = a.get(i);
		}
		return result;
	}

}
