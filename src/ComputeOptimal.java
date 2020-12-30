import java.util.ArrayList;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

public class ComputeOptimal {
	public static HashMap<Hospital, Double> levels(double tobeDistributed, double recoveryRate,
			Hospital[] hospital) {
		// as per Theorem 2, given total threshold quantity
		// as per Theorem 5, given total pooled quantity
		HashMap<Hospital, Double> hm = new HashMap<Hospital, Double>();
		int numberofHospitals = hospital.length;
		if(tobeDistributed>0) {
			
			
			ArrayList<Double> A = new ArrayList<Double>();
			ArrayList<Double> b = new ArrayList<Double>();
	
			double firstCoef = findCoef(hospital[0].getDemand(), recoveryRate);
	
			for (int i = 1; i < numberofHospitals; i++) {
				A.add(firstCoef);
				for (int j = 1; j < i; j++) {
					A.add(0.0);
				}
				double secondCoef = findCoef(hospital[i].getDemand(), recoveryRate);
				A.add(-secondCoef);
				for (int j = i + 1; j < numberofHospitals; j++) {
					A.add(0.0);
				}
				b.add(Math.log((hospital[i].getDemand() * secondCoef) / (hospital[0].getDemand() * firstCoef)));
			}
			for (int i = 0; i < numberofHospitals; i++) {
				A.add(1.0);
			}
			b.add(tobeDistributed);
			
			double[] arrayA=convert(A);
			double[] arrayb=convert(b);
			
			SimpleMatrix smA = new SimpleMatrix(numberofHospitals, numberofHospitals, true, arrayA);
			SimpleMatrix smb = new SimpleMatrix(numberofHospitals, 1, true, arrayb);
			SimpleMatrix result= smA.invert().mult(smb);
			
			
			for (int i = 0; i < numberofHospitals; i++) {
				hm.put(hospital[i], Math.max(0, result.get(i,0)));
			}
		}
		else
		{
			for (int i = 0; i < numberofHospitals; i++) {
				hm.put(hospital[i], 0.0);
			}
		}
		return hm;
	}

	private static double[] convert(ArrayList<Double> a) {
		int size=a.size();
		double[] result=new double[size];
		for(int i=0;i<size;i++)
		{
			result[i]=a.get(i);
		}
		return result;
	}

	private static double findCoef(double annualDemandRate, double recoveryRate) {
		return Math.log(annualDemandRate / (annualDemandRate + recoveryRate));
	}

}
