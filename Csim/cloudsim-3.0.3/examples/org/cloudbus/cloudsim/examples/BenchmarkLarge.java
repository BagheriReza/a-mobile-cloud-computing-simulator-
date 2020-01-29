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

public class BenchmarkLarge {
	public static int heterogeneity = 40;// % x/100

	BenchmarkLarge() {
		System.out.println("Benchmark constructor");
	}

	void BenchmarkLarge() {
		System.out.println("Benchmark mathod");
	}

	public static void main(String[] args) {
		Instant start = Instant.now();

		WIJList WIJList = new WIJList();
		WIJList ProfitList = new WIJList();
		EffList EffList = new EffList();
		CapcityList CapList = new CapcityList();

		// Phase 0 Read n(Number of Item) and m(Number of Knapsack/bin/VM)
		int r = 1000;
		int n = 800;
		int m = 200;
		
		for(int i=0; i<m;i++){
			for(int j=0; j<n;j++){
				double temp =0.0;
				temp = (Math.random() * ((r - 1) + 1)) + 1;
				WIJList.Add(new WIJ(i,j,temp));
			}
		}
		//Uncorrelated data instance
		for(int j=0; j<n;j++){
			double temp = (Math.random() * ((r - 1) + 1)) + 1;
			ProfitList.Add(new WIJ(0,j,temp));
		}
		//
		/*
		for(int i=0; i<m;i++){
			double temp =0.0;
			for(int j=0; j<n;j++){
				temp = temp + WIJList.get(i, j);
			}
			temp = temp/m;
			
		}
		*/
		
		
		
		// Calculating Capacity
		double SumW =0.0;
		for (int i=0; i<m;i++){
			for(int j=0; j<n;j++){
				SumW = SumW+ WIJList.get(i, j);
			}
		}
		SumW = SumW / (m);
		for(int i=0; i<m;i++){
			double temp =0.0;
			temp = (Math.random() * (((0.8*SumW) - 1) + 1)) + (0.1*SumW); 
			CapList.Add(new Capcity(i,temp));
		}
		// Phase 1 initial Weight&Profit&Capacity&Eff
		// The name of the file to open.
		
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
			writer = new PrintWriter("D:\\outputLarge.txt", "UTF-8");
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
