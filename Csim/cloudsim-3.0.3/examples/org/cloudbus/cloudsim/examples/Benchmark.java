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

public class Benchmark {
	public static int heterogeneity = 40;// % x/100

	Benchmark() {
		System.out.println("Benchmark constructor");
	}

	void Benchmark() {
		System.out.println("Benchmark mathod");
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
		String fileName = "D:\\random50_500_4_1000_1_20.txt";

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
			int z = 0;
			while ((line = bufferedReader.readLine()) != null) {
				Pattern pattern1 = Pattern.compile("(\\d+)");
				Matcher matcher1 = pattern1.matcher(line);

				while (matcher1.find()) {
					int temp;
					if (z == 0) {
						temp = Integer.valueOf(matcher1.group());
						WIJList.Add(new WIJ(0, y, temp));
						z++;
					} else {
						temp = Integer.valueOf(matcher1.group());
						ProfitList.Add(new WIJ(0, y, temp));
						z = 0;
						y++;
					}

				}

			}

		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		// fixing WIJ
		for (int j = 0; j < n; j++) {
			int temp = (int) WIJList.get(0, j);
			for (int i = 0; i < m; i++) {
				Random randomGenerator = new Random();
				int randomInt =  ((100-heterogeneity)*temp+randomGenerator.nextInt(2*heterogeneity*temp))/100;
				if(!(i==0)){
					WIJList.Add(new WIJ(i,j,randomInt));
				}else{
					WIJList.Set(0, j, randomInt);

				}

			}
		}
		
		Log.printLine("n: " + n + " , m: " + m);

		for (int j = 0; j < n; j++) {
			Log.printLine("P (" + j + "): " + ProfitList.get(0, j));
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				Log.printLine("W (" + j + " ," + i + "): " + WIJList.get(j, i));

			}
		}
		Log.printLine();
		for (int i = 0; i < m; i++) {
			Log.printLine("C (" + i + "): " + CapList.Get(i).C);

		}
		// writing new file for Execution
		PrintWriter writer;
		try {
			writer = new PrintWriter("D:\\output.txt", "UTF-8");
			writer.println(m);
			writer.println(n);
			for(int i=0; i< m;i++){
				writer.println((int) CapList.Get(i).C);
			}
			for (int j = 0; j < n; j++) {
				writer.println((int) ProfitList.get(0, j));
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					writer.print((int) WIJList.get(j, i) + "	" );
				}
				writer.println();
			}
			writer.close();
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis(); // in
																		// millis
		System.out.println("Total time: " + timeElapsed);

	}

	private static void pressAnyKeyToContinue() {
		System.out.println("Press Enter key to continue...");
		try {
			System.in.read();
		} catch (Exception e) {
		}
	}
}
