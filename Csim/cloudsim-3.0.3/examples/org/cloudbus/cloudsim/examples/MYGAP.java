//exhaustive search

package org.cloudbus.cloudsim.examples;

import java.io.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.Datacenter.MigInfo;
import org.cloudbus.cloudsim.Datacenter.VMtoCloudletEnergy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MYGAP {
	public static int[][] xij;
	public static int test = 0;
	public static double LowerBound;
	public static double[] Wmax;
	public static double TS = 1.005;

	MYGAP() {
		System.out.println("GAP constructor");
	}

	void MYGAP() {
		System.out.println("GAP mathod");
	}

	public static void main(String[] args) {
		Instant start = Instant.now();

		WIJList WIJList = new WIJList();
		WIJList ProfitList = new WIJList();
		EffList EffList = new EffList();
		CapcityList CapList = new CapcityList();

		// Phase 0 Read n(Number of Item) and m(Number of Knapsack/bin/VM)
		int n = 0;
		int m = 0;

		// Phase 1 initial Weight&Profit&Capacity&Eff
		// The name of the file to open.
		String fileName = "D:\\output.txt";

		// This will reference one line at a time
		String line = null;
		String line1 = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int y = 0;
			int x;

			line = bufferedReader.readLine();
			Pattern pattern = Pattern.compile("(\\d+)");
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				m = Integer.valueOf(matcher.group());
			}
			line = bufferedReader.readLine();
			matcher = pattern.matcher(line);
			while (matcher.find()) {
				n = Integer.valueOf(matcher.group());
			}
			x = 0;
			for (int i = 0; i < m; i++) {
				line = bufferedReader.readLine();
				matcher = pattern.matcher(line);
				while (matcher.find()) {
					int temp;
					temp = Integer.valueOf(matcher.group());
					CapList.Add(new Capcity(x, temp));
					CapList.ReminingCap.add(new Capcity(x, temp));
					x++;
				}
			}
			y = 0;
			x = 0;
			for (int j = 0; j < n; j++) {
				line = bufferedReader.readLine();
				matcher = pattern.matcher(line);
				while (matcher.find()) {
					int temp;
					temp = Integer.valueOf(matcher.group());
					ProfitList.Add(new WIJ(0, y, temp));

				}
				y++;
			}

			y = 0;
			x = 0;
			int z = 0;
			for (int j = 0; j < n; j++) {
				line = bufferedReader.readLine();
				Pattern pattern1 = Pattern.compile("(\\d+)");
				Matcher matcher1 = pattern1.matcher(line);
				for (int i = 0; i < m; i++) {
					matcher1.find();
					int temp;
					temp = Integer.valueOf(matcher1.group());
					WIJList.Add(new WIJ(i, j, temp));

				}
			}

		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		Log.printLine("n: " + n + " , m: " + m);

		for (int j = 0; j < n; j++) {
			// Log.printLine("P (" + 0 + " ," + j + "): " + ProfitList.get(0,
			// j));

		}

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				Log.printLine("W (" + i + " ," + j + "): " + WIJList.get(i, j));

			}
		}

		Log.printLine();
		for (int i = 0; i < m; i++) {
			Log.printLine("C (" + i + "): " + CapList.Get(i).C);

		}
		// pressAnyKeyToContinue();

		Log.printLine();
		// Initialization of Eff
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				EffList.Add(new Eff(i, j, (ProfitList.get(0, j) / WIJList.get(i, j))));
				// Log.printLine("Eff ("+i+" ,"+j+"): "+ EffList.get(i, j));
			}
		}
		EffList.sort();
		for (int i = 0; i < EffList.EffList.size(); i++) {
			Log.printLine(EffList.EffList.get(i));
		}

		// start of algorithm
		// Initiation
		int Index = -1;
		double profit = 0.0;
		double weight = 0.0;
		LowerBound = 0;
		xij = new int[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				xij[i][j] = 0;
			}
		}
		double[] y = new double[n];
		for (int i = 0; i < n; i++) {
			y[i] = 0;
		}
		Wmax = new double[m];
		for (int i = 0; i < m; i++) {
			double wtemp = 0;
			for (int j = 0; j < n; j++) {
				if (wtemp < WIJList.get(i, j)) {
					wtemp = WIJList.get(i, j);
				}
			}
			Wmax[i] = wtemp;

		}

		GAPBB(m, n, Index, y, LowerBound, profit, weight, CapList, EffList, WIJList, ProfitList, xij);
		Log.printLine("Exact result: " + LowerBound);
		for (int i = 0; i < m; i++) {
			Log.print("i(" + i + "): ");
			for (int j = 0; j < n; j++) {
				Log.print(xij[i][j] + " ,");
			}
			Log.printLine();
		}
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis(); // in
																		// millis
		System.out.println("Total time: " + timeElapsed);

	}

	private static void GAPBB(int m, int n, int index, double[] yy, double profit, double weight, double LBound,
			CapcityList capList, EffList effList, WIJList wIJList, WIJList profitList, int[][] xijFinal) {
		index++;
		double[] reminingcap = new double[m];
		double[] reminingcapl = new double[m];
		int exchange = 0;
		int PmaxJ = 0;
		for (int i = 0; i < m; i++) {
			reminingcap[i] = capList.CapcityList.get(i).C;
			reminingcapl[i] = capList.CapcityList.get(i).C;

		}
		// ArrayList<Capcity> reminingCapl ;
		// reminingCapl = new ArrayList<Capcity> (capList.ReminingCap);
		double[][] yij = new double[m][n];
		double[][] uxij = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				yij[i][j] = 0;
				uxij[i][j] = xijFinal[i][j];
			}
		}

		// phase 1, case of replace in case of threshold
		// if(Remaining capacity - current assignment < WMax)
		if ((capList.Get(effList.EffList.get(index).I).C - wIJList.get(effList.EffList.get(index).I,
				effList.EffList.get(index).J)) <= Wmax[effList.EffList.get(index).I]) {
			// Log.printLine("checking for exchange");
			int i = effList.EffList.get(index).I;
			int j = effList.EffList.get(index).J;
			// we need to find out candidate
			// calculate LB
			double[] lyj = new double[n];
			int[][] lxij = new int[m][n];
			double LBound1 = 0.0;
			for (int ia = 0; ia < m; ia++) {
				reminingcapl[ia] = capList.CapcityList.get(ia).C;
			}
			for (int ia = 0; ia < m; ia++) {
				for (int ja = 0; ja < n; ja++) {
					lyj[ja] = yy[ja];
					lxij[ia][ja] = xijFinal[ia][ja];
				}
			}
			for (int a = index; a < effList.EffList.size(); a++) {
				int ia = effList.EffList.get(a).I;
				int ja = effList.EffList.get(a).J;
				if (lyj[ja] < 1) {
					// Log.printLine("Second if");
					// Log.print(reminingCapl.get(i).C);
					if (reminingcapl[ia] >= (wIJList.get(ia, ja))) {
						// Log.printLine("Third if");
						lxij[ia][ja] = 1;
						reminingcapl[ia] = reminingcapl[ia] - wIJList.get(ia, ja);
						lyj[ja] = 1;
						LBound1 = LBound1 + profitList.get(0, ja);
					}
				}
			}

			// finding Pmax
			PmaxJ = -1;
			double Ptemp = 0.0;
			for (int ia = 0; ia < m; ia++) {
				for (int ja = 0; ja < n; ja++) {
					lyj[ja] = yy[ja];
					lxij[ia][ja] = xijFinal[ia][ja];
				}
			}
			for (int ia = 0; ia < m; ia++) {
				reminingcapl[ia] = capList.CapcityList.get(ia).C;
			}

			// Log.printLine("reminingcapl[i]" + reminingcapl[i]);
			for (int ja = 0; ja < n; ja++) {
				if (profitList.get(0, ja) > Ptemp) {
					if (lyj[ja] == 0) {
						if ((reminingcapl[i] - wIJList.get(i, ja)) >= 0) {
							Ptemp = profitList.get(0, ja);
							PmaxJ = ja;
						}

					}

				}
			}
			// calculate LB with considering Pmax is selected
			// calculate LB

			double LBoundPmax = 0.0;
			// Log.printLine("PmaxJ: " + PmaxJ);
			if (!(PmaxJ == -1)) {
				lyj[PmaxJ] = 1;
				lxij[i][PmaxJ] = 1;
				reminingcapl[i] = reminingcapl[i] - wIJList.get(i, PmaxJ);
				LBoundPmax = profitList.get(0, PmaxJ);
				for (int a = index; a < effList.EffList.size(); a++) {
					int ia = effList.EffList.get(a).I;
					int ja = effList.EffList.get(a).J;
					if (lyj[ja] < 1) {
						// Log.printLine("Second if");
						// Log.print(reminingCapl.get(i).C);
						if (reminingcapl[ia] >= (wIJList.get(ia, ja))) {
							// Log.printLine("Third if");
							lxij[ia][ja] = 1;
							reminingcapl[ia] = reminingcapl[ia] - wIJList.get(ia, ja);
							lyj[ja] = 1;
							LBoundPmax = LBoundPmax + profitList.get(0, ja);
						}
					}
				}
			}
			// checking profit
			// Log.printLine("LBPmax: " + LBoundPmax + " LB: " + LBound1);
			if (LBoundPmax > LBound1) {
				// exchange current node with Pmax
				Log.printLine("index: " + index + " exchange with pmaxJ: " +effList.getx(i, PmaxJ));
				effList.exchange(index, effList.getx(i, PmaxJ));
				// 
				exchange = 1;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////

		// Log.printLine("(" + effList.EffList.get(index).I + " , " +
		// effList.EffList.get(index).J + ")" + " index:"
		// + index + " profit: " + profit);
		// pressAnyKeyToContinue();
		// pressAnyKeyToContinue();

		// phase 1: UP calculation (LPRelaxation)
		double UProfit = 0.0;
		double[] yj = new double[n];
		for (int i = 0; i < n; i++) {
			yj[i] = yy[i];
		}

		for (int x = index; x < effList.EffList.size(); x++) {
			int i = effList.EffList.get(x).I;
			int j = effList.EffList.get(x).J;
			if (yj[j] < 1) {
				if (reminingcap[i] >= ((1 - yj[j]) * wIJList.get(i, j))) {
					uxij[i][j] = (1 - yj[j]);
					reminingcap[i] = reminingcap[i] - ((1 - yj[j]) * wIJList.get(i, j));
					yj[j] = 1;
					UProfit = UProfit + uxij[i][j] * profitList.get(0, j);
				} else { // partially assignment
					uxij[i][j] = (reminingcap[i] / wIJList.get(i, j));
					yj[j] = yj[j] + uxij[i][j];
					UProfit = UProfit + uxij[i][j] * profitList.get(0, j);
					reminingcap[i] = 0;
				}
			}
		}
		// Log.printLine("Result of uper bound");
		/*
		 * for(int i =0; i < m; i++){ for(int j =0; j < n; j++){ Log.print("; "
		 * +uxij[i][j]); } Log.printLine(); }
		 */
		// Log.printLine("Upper bound profit: " + UProfit);
		// pressAnyKeyToContinue();
		// for(int i=0; i < m; i++){
		// Log.printLine(reminingCap.get(i));
		// }

		// Phase 2: checking upper bound and lower bound
		double[] lyj = new double[n];
		int[][] lxij = new int[m][n];
		double LBound1 = 0.0;
		for (int i = 0; i < m; i++) {
			reminingcapl[i] = capList.CapcityList.get(i).C;
		}
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				lyj[j] = yy[j];
				lxij[i][j] = xijFinal[i][j];
			}
		}
		if ((profit + UProfit) > LBound) {
			// calculate LBound
			// Log.printLine("first if");
			for (int x = index; x < effList.EffList.size(); x++) {
				// Log.printLine("first for");
				int i = effList.EffList.get(x).I;
				int j = effList.EffList.get(x).J;
				if (lyj[j] < 1) {
					// Log.printLine("Second if");
					// Log.print(reminingCapl.get(i).C);
					if (reminingcapl[i] >= (wIJList.get(i, j))) {
						// Log.printLine("Third if");
						lxij[i][j] = 1;
						reminingcapl[i] = reminingcapl[i] - wIJList.get(i, j);
						lyj[j] = 1;
						LBound1 = LBound1 + profitList.get(0, j);
					}
				}
			}
			/*
			 * Log.printLine("Result of lower bound"); for(int i =0; i < m;
			 * i++){ for(int j =0; j < n; j++){ Log.print("; "+lxij[i][j]); }
			 * Log.printLine(); }
			 */
			// Log.printLine("Lower bound profit: " + LBound1);
			if ((profit + LBound1) > LowerBound) {
				Log.printLine("lower bound update: Profit: " + profit + " ,LBoundl: " + LBound1 + " , Totally: "
						+ (profit + LBound1));
				LowerBound = (profit + LBound1);
				xij = lxij;
			}

		}
		// Log.printLine("Profit: " + profit + " ,UProfit: " + UProfit + "
		// Lowerbound: " + LowerBound);
		// Log.printLine("Eff.Size: " + effList.EffList.size() + " , index: " +
		// index);
		// pressAnyKeyToContinue();
		int i = effList.EffList.get(index).I;
		int j = effList.EffList.get(index).J;
		while ((effList.EffList.size() > (index + 1)) && (!(yy[j] < 1))) {
			index++;
			// Log.printLine("while index: "+ index);
			i = effList.EffList.get(index).I;
			j = effList.EffList.get(index).J;
			// pressAnyKeyToContinue();

		}
		if (((profit + UProfit) > (LowerBound*TS)) && (effList.EffList.size() > (index + 1))) {
			// reduction algorithm

			// end of reduction algorithms

			// Log.printLine("checking recursive calling, yy["+yy[j]+"] "+"<
			// 1");
			//// checking for next phases
			// branching selected
			if (capList.CapcityList.get(i).C >= wIJList.get(i, j)) {// is it
																	// feasible
																	// to assign
																	// ?
				capList.CapcityList.get(i).C = capList.CapcityList.get(i).C - wIJList.get(i, j);
				profit = profit + profitList.get(0, j);
				xijFinal[i][j] = 1;
				yy[j] = 1;
				/// Log.printLine("Selecting branch, Index :" + index);
				// Log.printLine("(" + i + "," + j + "):" + yy[j]);
				GAPBB(m, n, index, yy, profit, weight, LowerBound, capList, effList, wIJList, profitList, xijFinal);

				// branching not selected
				// Log.printLine("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				// Log.printLine("profit before deduction: "+ profit+"
				// profitList.get(i, j): "+profitList.get(i, j));
				xijFinal[i][j] = 0;
				yy[j] = 0;
				capList.CapcityList.get(i).C = capList.CapcityList.get(i).C + wIJList.get(i, j);
				profit = profit - profitList.get(0, j);
				// Log.printLine("unselecting branching, Index :" + index);
				// Log.printLine("(" + i + "," + j + "):" + yy[j] + "Profit: " +
				// profit);
				// pressAnyKeyToContinue();
				GAPBB(m, n, index, yy, profit, weight, LowerBound, capList, effList, wIJList, profitList, xijFinal);

			} else {

				// branching not selected
				// Log.printLine("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				// Log.printLine("profit before deduction: "+ profit+"
				// profitList.get(i, j): "+profitList.get(i, j));
				xijFinal[i][j] = 0;
				yy[j] = 0;
				/// Log.printLine("unselecting branching, Index :" + index);
				// Log.printLine("(" + i + "," + j + "):" + yy[j] + "Profit: " +
				/// profit);
				// pressAnyKeyToContinue();
				GAPBB(m, n, index, yy, profit, weight, LowerBound, capList, effList, wIJList, profitList, xijFinal);
			}

		}
		// reverse exchange of efflis for other branches
		// for test
		if (exchange == 1) {
			effList.exchange(effList.getx(i, PmaxJ), index);
		}
	}

	private static void pressAnyKeyToContinue() {
		System.out.println("Press Enter key to continue...");
		try {
			System.in.read();
		} catch (Exception e) {
		}
	}
}
