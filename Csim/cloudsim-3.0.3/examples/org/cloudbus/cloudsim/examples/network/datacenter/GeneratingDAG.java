package org.cloudbus.cloudsim.examples.network.datacenter;

import java.io.*;
import java.text.DecimalFormat;
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
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.network.datacenter.NetDatacenterBroker;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

public class GeneratingDAG {
	
	public static void main(String[] args) {

        // The name of the file to open.
        String fileName = "D:\\1.txt";

        // This will reference one line at a time
        String line = null;
        int TaskNo = 0;
        int EdgeNo = 0;
        int ProcessorNo = 0;
        int FileSize = 50000; // 5000 = small, 10000 = medium, 50000 = large
		int NoAppType = 50; // for creating cloudletfiles
		int NoFiles = 5; //No of files in each App
		
		

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            int y = 0;
            int counterEdge = 0 ;
            
            ////
            line = bufferedReader.readLine();
            TaskNo = Integer.valueOf(line) ;
    		System.out.println("No. of tasks is equal: " + TaskNo);
            
    		line = bufferedReader.readLine();
    		EdgeNo = Integer.valueOf(line) ;
    		System.out.println("No. of Edge is equal: " + EdgeNo);
    		line = bufferedReader.readLine();

            ////
    		int [] Source = new int [EdgeNo];
    		int [] Destination = new int [EdgeNo];
    		int [] Cost = new int [EdgeNo];
    		int [] TotalCost = new int [TaskNo];
            
            while((line = bufferedReader.readLine()) != null) {
            		 
            			Pattern pattern = Pattern.compile("(\\d+)");
            			Matcher matcher = pattern.matcher(line);
            			y = 0;
            			while(matcher.find())
            			{	if (y==0){
            				Source[counterEdge] = Integer.valueOf(matcher.group());
            			}else if (y ==1 ){
            				Destination[counterEdge] = Integer.valueOf(matcher.group());
            			}else if (y ==2){
            				Cost[counterEdge] = Integer.valueOf(matcher.group());
            				            			}
            			y++;
            			}
            			System.out.print(Source[counterEdge]+ ", ");
        				System.out.print(Destination[counterEdge]+ ", ");
        				System.out.print(Cost[counterEdge]+ ", ");

                        System.out.println();
                        counterEdge++;
            		
                
            }   
            System.out.println();
            
            for (y=0; y < TaskNo; y++){
            	TotalCost [y] = 0;
            }
           
            
            for (int x=0; x < EdgeNo ; x++){
        	   for (y =0; y < TaskNo; y++){
            	   if (Source[x] == y){
            		   TotalCost [y-1] = TotalCost [y-1] + Cost [x];
            		   System.out.println("source["+Source[x]+"],Destination["+Destination[x]+"]: cost is: "+Cost [x]);
            	   }
        	   }
           }
            // Always close files.
            bufferedReader.close();         
       
        
     // The name of the file to open.
        fileName = "D:\\2.txt";

        // This will reference one line at a time
        line = null;

        
            // FileReader reads text files in the default encoding.
            FileReader fileReader1 = 
                new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader1 = 
                new BufferedReader(fileReader1);
            y = 0;
            
            int TaskCounter = 0;
            ////
            line = bufferedReader1.readLine();
            line = bufferedReader1.readLine();
            ProcessorNo = Integer.valueOf(line) ;
    		System.out.println("No. of processors is equal: " + ProcessorNo);
            line = bufferedReader1.readLine();

    		////
            int [][] CompCost = new int [TaskNo][ProcessorNo];
            while((line = bufferedReader1.readLine()) != null) {
            		 
            			Pattern pattern = Pattern.compile("(\\d+)");
            			Matcher matcher = pattern.matcher(line);
            			y = 0;
            			while(matcher.find())
            			{	
            				CompCost[TaskCounter][y] = (Integer.valueOf(matcher.group()));   
            				y++;
            			}
            			for (int z =0; z < y; z++){
            			System.out.print(CompCost[TaskCounter][z]+", ");
            			}
                        System.out.println();
                        TaskCounter++;
            		
                
            }   
            System.out.println();

            // Always close files.
            bufferedReader1.close();         
        
        
        ///////////////////////// Here i will initiate BW and Energy for transferring based on BW /////////////////////////
        
        int [][] BW = new int [ProcessorNo][ProcessorNo+1];
        int [] EnergyExePerSec = new int [ProcessorNo];
        int [] EnergyTrans = new int [ProcessorNo];
        int [][] EnergyTotal = new int [TaskNo][ProcessorNo];
        
        Random rand = new Random();
        System.out.println("No. of task is equal to:"+TaskNo );
        System.out.println("No. of ProcessorNo is equal to:"+ProcessorNo );

        for (int x=0; x < ProcessorNo; x++){
        	for (y=0; y < ProcessorNo; y++){
        		if (x<y){
        		BW[x][y] = 600 + rand.nextInt(800);
        		}else if (x>y){
        			BW[x][y] = BW[y][x];
        		}else {
        			BW[x][y] = 100000000;
        		}
        		System.out.print(BW[x][y]+", ");
        	}
        	System.out.println();
        }
        int temp =0;
        for (int x=0; x < ProcessorNo; x++){
        	temp =0;
        	for (y=0; y < ProcessorNo; y++){
        		if (!(x==y)){
        			temp = temp + BW[x][y];
        		}
        	}
        	int reza =0 ;
        	BW[x][ProcessorNo] = (int) temp/(ProcessorNo-1);
        	System.out.println(BW[x][ProcessorNo]);
        }
        // writing in excel for inputing data for simulator
        /////////////////                            Writing DAG file
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("DAG");
            
			//Write all row
			int rowCount = 0;
			
			
			
			
            for (int taskcounter=0;taskcounter < TaskNo; taskcounter++ ){
            	//counting No of stage
            	int NoS = 1;
            	int StageID = 0;
            	for (int edgecounter =0; edgecounter < EdgeNo; edgecounter++){
            		if (Source[edgecounter] == taskcounter){
            			NoS++;
            		}
            		if (Destination[edgecounter] == taskcounter){
            			NoS++;
            		}
            		}
            	
            	////////////////////                            create receiving stages
            	for (int edgecounter =0; edgecounter < EdgeNo; edgecounter++){
            	if (Destination[edgecounter]== (taskcounter+1)){
            		Row row = sheet.createRow(++rowCount);
        			
                    Cell CID = row.createCell(0); //Cloudlet ID***************************
                    CID.setCellValue((int) taskcounter);
                    
        			Cell CL = row.createCell(1); //Cloudlet Length
        			CL.setCellValue((int) 0);
        			
        			Cell PesN = row.createCell(2); //Cloudlet PesNumber	
        			PesN.setCellValue((int) 1);
        			
        			Cell FS = row.createCell(3); //Cloudlet FileSize
        			FS.setCellValue((int) (Cost[edgecounter]*1024));
        			
        			Cell OS = row.createCell(4); //Cloudlet OutputSize********************
        			OS.setCellValue((int) (Cost[edgecounter]*1024));
        			//OS.setCellValue((int) (TotalCost[taskcounter]*1024));

        			Cell Battery = row.createCell(5); //Cloudlet Battery
        			Battery.setCellValue((int) 0); 
        			
        			Cell Memory = row.createCell(6); //Cloudlet Memory
        			Memory.setCellValue((int) 0);
        			
        			Cell NumS = row.createCell(7); //Cloudlet Number of Stages
        			NumS.setCellValue((int) NoS);
        			
        			Cell vm = row.createCell(8); //Cloudlet VM
        			vm.setCellValue((int) 0);
        			
        			Cell StageNum = row.createCell(9);// Stage Number
        			StageNum.setCellValue((int) StageID);
        			
        			Cell SType = row.createCell(10);// Stage Type
        			SType.setCellValue((int) 2);
        			
        			Cell Sdata = row.createCell(11);// Stage data
        			Sdata.setCellValue((int) 0);
        			
        			Cell Stime = row.createCell(12);// Stage time
        			Stime.setCellValue((int) 0);
        			
        			Cell SID = row.createCell(13);// Stage ID
        			SID.setCellValue((int) StageID);
        			
        			Cell SMemory = row.createCell(14);// Stage Memory
        			SMemory.setCellValue((int) 0);
        			
        			Cell SPeer = row.createCell(15);// Stage Peer
        			SPeer.setCellValue((int) (Source[edgecounter]-1));
        			
        			Cell SVPeer = row.createCell(16);// Stage VPeer
        			SVPeer.setCellValue((int) (Source[edgecounter]-1));
        			
        			Cell CMIPS = row.createCell(17);// Stage MIPS *************************
        			CMIPS.setCellValue((int) 1);
        			
        			StageID++;
            	}
            	}
            	
            	///////////                             Execution Stage
            	Row row1 = sheet.createRow(++rowCount);
    			
                Cell CID1 = row1.createCell(0); //Cloudlet ID***************************
                CID1.setCellValue((int) taskcounter);
                
    			Cell CL1 = row1.createCell(1); //Cloudlet Length
    			CL1.setCellValue((int) 0);
    			
    			Cell PesN1 = row1.createCell(2); //Cloudlet PesNumber	
    			PesN1.setCellValue((int) 1);
    			
    			Cell FS1 = row1.createCell(3); //Cloudlet FileSize
    			FS1.setCellValue((int) (TotalCost[taskcounter]*1024));
    			
    			Cell OS1 = row1.createCell(4); //Cloudlet OutputSize********************
    			OS1.setCellValue((int) (TotalCost[taskcounter]*1024));
    			
    			Cell Battery1 = row1.createCell(5); //Cloudlet Battery
    			Battery1.setCellValue((int) 0); 
    			
    			Cell Memory1 = row1.createCell(6); //Cloudlet Memory
    			Memory1.setCellValue((int) 0);
    			
    			Cell NumS1 = row1.createCell(7); //Cloudlet Number of Stages
    			NumS1.setCellValue((int) NoS);
    			
    			Cell vm1 = row1.createCell(8); //Cloudlet VM
    			vm1.setCellValue((int) 0);
    			
    			Cell StageNum1 = row1.createCell(9);// Stage Number
    			StageNum1.setCellValue((int) StageID);
    			
    			Cell SType1 = row1.createCell(10);// Stage Type
    			SType1.setCellValue((int) 0);
    			
    			Cell Sdata1 = row1.createCell(11);// Stage data
    			Sdata1.setCellValue((int) 0);
    			
    			Cell Stime1 = row1.createCell(12);// Stage time
    			Stime1.setCellValue((int) 0);
    			
    			Cell SID1 = row1.createCell(13);// Stage ID
    			SID1.setCellValue((int) StageID);
    			
    			Cell SMemory1 = row1.createCell(14);// Stage Memory
    			SMemory1.setCellValue((int) 0);
    			
    			Cell SPeer1 = row1.createCell(15);// Stage Peer
    			SPeer1.setCellValue((int) taskcounter);
    			
    			Cell SVPeer1 = row1.createCell(16);// Stage VPeer
    			SVPeer1.setCellValue((int) taskcounter);
    			
    			Cell CMIPS1 = row1.createCell(17);// Stage MIPS *************************
    			CMIPS1.setCellValue((int) 1);
    			
    			StageID++;
    			
    			
    			///////////////                     Sending Stages
    			for (int edgecounter =0; edgecounter < EdgeNo; edgecounter++){
                	if (Source[edgecounter]== (taskcounter+1)){
                		Row row = sheet.createRow(++rowCount);
            			
                        Cell CID = row.createCell(0); //Cloudlet ID***************************
                        CID.setCellValue((int) taskcounter);
                        
            			Cell CL = row.createCell(1); //Cloudlet Length
            			CL.setCellValue((int) 0);
            			
            			Cell PesN = row.createCell(2); //Cloudlet PesNumber	
            			PesN.setCellValue((int) 1);
            			
            			Cell FS = row.createCell(3); //Cloudlet FileSize
            			FS.setCellValue((int) (Cost[edgecounter]*1024));
            			
            			Cell OS = row.createCell(4); //Cloudlet OutputSize********************
            			OS.setCellValue((int) (Cost[edgecounter]*1024));
            			
            			Cell Battery = row.createCell(5); //Cloudlet Battery
            			Battery.setCellValue((int) 0); 
            			
            			Cell Memory = row.createCell(6); //Cloudlet Memory
            			Memory.setCellValue((int) 0);
            			
            			Cell NumS = row.createCell(7); //Cloudlet Number of Stages
            			NumS.setCellValue((int) NoS);
            			
            			Cell vm = row.createCell(8); //Cloudlet VM
            			vm.setCellValue((int) 0);
            			
            			Cell StageNum = row.createCell(9);// Stage Number
            			StageNum.setCellValue((int) StageID);
            			
            			Cell SType = row.createCell(10);// Stage Type
            			SType.setCellValue((int) 1);
            			
            			Cell Sdata = row.createCell(11);// Stage data
            			Sdata.setCellValue((int) 0);
            			
            			Cell Stime = row.createCell(12);// Stage time
            			Stime.setCellValue((int) 0);
            			
            			Cell SID = row.createCell(13);// Stage ID
            			SID.setCellValue((int) StageID);
            			
            			Cell SMemory = row.createCell(14);// Stage Memory
            			SMemory.setCellValue((int) 0);
            			
            			Cell SPeer = row.createCell(15);// Stage Peer
            			SPeer.setCellValue((int) Destination[edgecounter]-1);
            			
            			Cell SVPeer = row.createCell(16);// Stage VPeer
            			SVPeer.setCellValue((int) Destination[edgecounter]-1);
            			
            			Cell CMIPS = row.createCell(17);// Stage MIPS *************************
            			CMIPS.setCellValue((int) 1);
            			
            			StageID++;
                	}
                	}
            }
            try (FileOutputStream outputStream = new FileOutputStream("d:\\DAG.xlsx")) {
                workbook.write(outputStream);
            }
			} catch (Exception e) {
					e.printStackTrace();
			}
        
        //////////////// Write VMBW
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("WMBW");
            
			//Write all row
			int rowCount = 0;
			
			for (int x=0; x < ProcessorNo; x++){
				Row row = sheet.createRow(++rowCount);
	        	for (y=0; y < (ProcessorNo+1); y++){
	        		Cell CID = row.createCell(y+1); //Cloudlet ID***************************
                    CID.setCellValue((int) BW[x][y]);
	        	}
	        }
        
        
        
        try (FileOutputStream outputStream = new FileOutputStream("d:\\VMBW.xlsx")) {
            workbook.write(outputStream);
        }
        
        
		
       
		} catch (Exception e) {
				e.printStackTrace();
		}
        ////////////////Write HostList
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("HOSTlist");
            
			//Write all row
			int rowCount = 0;
			
			for (int x=0; x < ProcessorNo; x++){
				Row row = sheet.createRow(++rowCount);
	        	
	        	Cell hostid = row.createCell(0); 
	        	hostid.setCellValue((int) x);
	        	
	        	Cell ram = row.createCell(1); 
	        	ram.setCellValue((int) 1024);
	        	
	        	Cell bw = row.createCell(2); 
	        	bw.setCellValue((int) 1024);
	        	
	        	Cell storage = row.createCell(3); 
	        	storage.setCellValue((int) 102400);
	        	
	        	Cell battery = row.createCell(4); 
	        	battery.setCellValue((int) 100000000);
	        	
	        	Cell MIPS = row.createCell(5); 
	        	MIPS.setCellValue((int) 1000);
	        	
	        	int pickedS = rand.nextInt(95000)+31000 ;
	    		double tempx = 0.0;
	    		tempx = ((double)26000/(double)126000);
	    			    		
	    		Cell ReadSpeed = row.createCell(6); 
	    		ReadSpeed.setCellValue(pickedS);
	    		
	    		Cell WriteSpeed = row.createCell(7); 
	    		WriteSpeed.setCellValue(((double)pickedS*(tempx)));
	    		
	    		Cell EnergyHD = row.createCell(8); 
	    		EnergyHD.setCellValue((double)588*((double)pickedS/(double)126000));
	    		
	        }
        
        try (FileOutputStream outputStream = new FileOutputStream("d:\\Hostlist.xlsx")) {
            workbook.write(outputStream);
        }
      	} catch (Exception e) {
				e.printStackTrace();
		}
        
////////////////Write VMList
try{
	XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("VMList");
    
	//Write all row
	int rowCount = 0;
	
	for (int x=0; x < ProcessorNo; x++){
		Row row = sheet.createRow(++rowCount);
    	
    	Cell vmid = row.createCell(0); 
    	vmid.setCellValue((int) x);
    	
    	Cell MIPS = row.createCell(1); 
    	MIPS.setCellValue((int) 1000);
    	
    	Cell storage = row.createCell(2); 
    	storage.setCellValue((int) 102400);
    	
    	Cell battery = row.createCell(3); 
    	battery.setCellValue((int) (2000 + rand.nextInt(1000))*60 );
    	
    	Cell ram = row.createCell(4); 
    	ram.setCellValue((int) 1024);
    	
    	Cell bw = row.createCell(5); 
    	bw.setCellValue((int) 1000);
    	
    	Cell PesN = row.createCell(6); 
    	PesN.setCellValue((int) 1);
    	
    	Cell VMM = row.createCell(7); 
    	VMM.setCellValue("Xen");
    }

try (FileOutputStream outputStream = new FileOutputStream("d:\\VMlist.xlsx")) {
    workbook.write(outputStream);
}
	} catch (Exception e) {
		e.printStackTrace();
}

//////////////////////////// writing computation cost in file
try{
	XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("ComCost");
    
	//Write all row
	int rowCount = 0;
	for (int x=0; x < TaskNo; x++){
		Row row = sheet.createRow(++rowCount);
		for (y=0; y < ProcessorNo; y++){
    	
			Cell cell = row.createCell(y); 
			cell.setCellValue((int) CompCost[x][y]);
    	}
	}
try (FileOutputStream outputStream = new FileOutputStream("d:\\CompCost.xlsx")) {
    workbook.write(outputStream);
}
	} catch (Exception e) {
		e.printStackTrace();
}


///////////////////////////// writing Energy consuption cost
System.out.println("Exe Energy Consuption:");
for (int x=0; x < ProcessorNo; x++){
	
	EnergyExePerSec[x] = (500 + rand.nextInt(120));
	System.out.print(EnergyExePerSec[x]);
	
	System.out.println();
}
/*
  try{
 
	XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("ExeEnergy");
    
	//Write all row
	int rowCount = 0;
	for (int x=0; x < TaskNo; x++){
		Row row = sheet.createRow(++rowCount);
		for (y=0; y < ProcessorNo; y++){
    	
			Cell cell = row.createCell(y); 
			cell.setCellValue((int) EnergyExePerSec[x]);
    	}
	}
try (FileOutputStream outputStream = new FileOutputStream("d:\\ExeEnergy.xlsx")) {
    workbook.write(outputStream);
}
	} catch (Exception e) {
		e.printStackTrace();
}
*/
///////////////////// Writing Energy for Trans

        System.out.println("Tras power Consuption:");
 
        for (int x=0; x < ProcessorNo; x++){
        	EnergyTrans[x] = (900 + rand.nextInt(600));;
        	System.out.print(EnergyTrans[x]);
        	System.out.println();
        }
        
        
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Appratio");
            
        	//Write all row
        	int rowCount = 0;
        	for (int x=0; x < ProcessorNo; x++){
        		Row row = sheet.createRow(++rowCount);
        			Cell VMID = row.createCell(0); 
        			VMID.setCellValue((int) x);
        			
        			Cell MIPSRatio = row.createCell(1); 
        			MIPSRatio.setCellValue((int) 1);
        			
        			Cell EnergyExePerunit = row.createCell(2); 
        			EnergyExePerunit.setCellValue((int) EnergyExePerSec[x]);
        			
        			Cell EnergyTransperUnit = row.createCell(3); 
        			EnergyTransperUnit.setCellValue((int) EnergyTrans[x]);
        	}
        try (FileOutputStream outputStream = new FileOutputStream("d:\\Appratio.xlsx")) {
            workbook.write(outputStream);
        }
        	} catch (Exception e) {
        		e.printStackTrace();
        }  
        
        
        
        //// Trans trans time
        try{
        	XSSFWorkbook workbook1 = new XSSFWorkbook();
            XSSFSheet sheet = workbook1.createSheet("Transtime");
            
        	//Write all row
        	int rowCount = 0;
        	for (int x=0; x < TaskNo; x++){
        		Row row = sheet.createRow(++rowCount);
        		           	
        			Cell cell = row.createCell(0); 
        			cell.setCellValue((int) TotalCost[x]*1024);
            	
        	}
        try (FileOutputStream outputStream = new FileOutputStream("d:\\Transtime.xlsx")) {
            workbook1.write(outputStream);
        }
        	} catch (Exception e) {
        		e.printStackTrace();
        }  
        
        
        
        
        
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        
    
        ////////////////Write CloudletFiles need to be read
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
        	XSSFSheet sheet = workbook.createSheet("CloudletFiles");
   
        	//Write all row
        	int rowCount = 0;
        	Random AppR = new Random(TaskNo);
        	int CloudletID = 0;
        	for (int x=0; x < NoFiles; x++){
        		
        	Row row = sheet.createRow(++rowCount);
        	
        		Cell C0 = row.createCell(0); 
        		C0.setCellValue((int) 0); // 0 = read , 1 = write
        		
        		Cell C1 = row.createCell(1); 
        		CloudletID = AppR.nextInt(TaskNo-1)+1;
        		C1.setCellValue(CloudletID);// TaskNo or cloudlet ID
        		
        		Cell C2 = row.createCell(2); 
        		C2.setCellValue("reza"); // File namse
        		
        		Cell C3 = row.createCell(3); 
        		C3.setCellValue(FileSize); // file size
        	}



        	try (FileOutputStream outputStream = new FileOutputStream("d:\\CloudletFiles.xlsx")) {
        		workbook.write(outputStream);
        	}




        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
       // creating random App No.
        try{
        	XSSFWorkbook workbook = new XSSFWorkbook();
        	XSSFSheet sheet = workbook.createSheet("AppTypes");
   
        	//Write all row
        	int rowCount = 0;
        	int AppNo = 100;
        	int AppTypeID = 27;
        	Random AppR = new Random(AppTypeID);
        	Row row = sheet.createRow(rowCount++);
        	Cell C0 = row.createCell(0); 
    		C0.setCellValue("AppID"); // 0 = read , 1 = write
    		
    		Cell C1 = row.createCell(1); 
    		C1.setCellValue("AppTypeID");// TaskNo or cloudlet ID
        	
    		Cell C2 = row.createCell(2); 
    		C1.setCellValue(AppTypeID);// No of App Type
        	
        	for (int x=0; x < AppNo; x++){
        		
        	Row row1 = sheet.createRow(rowCount++);
        	
        		Cell C01 = row1.createCell(0); 
        		C01.setCellValue(x); 
        		
        		Cell C11 = row1.createCell(1); 
        		C11.setCellValue((int) (AppR.nextInt(AppTypeID)));
        		
        	}



        	try (FileOutputStream outputStream = new FileOutputStream("d:\\AppsType.xlsx")) {
        		workbook.write(outputStream);
        	}




        } catch (Exception e) {
        	e.printStackTrace();
        } 
        
    }// main functiuon
	
	
	
}// class