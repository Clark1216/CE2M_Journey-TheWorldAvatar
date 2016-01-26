package callAP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import javax.script.*;

import com.esri.core.tasks.query.QueryParameters;

//testing script for the "runPyScript" command  ZL-151202
public class CallAP {
	public static String test = new String("C:/Users/Zhou/workspace/RunAspen/test.CSV"); 
 	public static String APINCSV = new String("C:/Users/Zhou/workspace/RunAspen/APIN.CSV"); 
 	public static String APOUTCSV = new String("C:/Users/Zhou/workspace/RunAspen/APOUT.CSV"); 
 	public static String runPythonCommandAP = new String("python C:/Users/Zhou/workspace/RunAspen/APrun.pyw");
	static ArrayList<String[]> editStack = new ArrayList<String[]>();
	
	private CallAP(){
				
	}
/*ZL-151203	
	public static void initiate(){
		
		String[] layers = new String[1];
		String[] FIDs = new String[2];
		
		layers[0]="ChemProcess";
		FIDs[0]= "2";
		//manually define the editStack ZL-151202
		for (int i=0; i<layers.length; i++) {
			editStack.add(new String[] {layers[i], FIDs[i]});
			System.out.println("editStack="+editStack);
	    }
		FileWriter test = null;  //testing FileWriter ZL-151202
		try {
			test = new FileWriter(test1);
			test.append("layers=" + layers[0]);
			test.append(", FIDs=" + FIDs[0]);
			test.flush();
			test.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
ZL-151203*/	
	public static void main(String args[]){
		ArrayList<String[]> skeleton = new ArrayList<String[]>(); 
		
//		initiate(); //initiate editStack ZL-151202
		skeleton.add(new String[] {"OIL,MEOH","FOIL,FMEOH"});  //manually add elements to skeleton ZL-151202
		writeAPCSV(skeleton,APINCSV); //write the APIN.CSV i.e. the input data for Aspen Plus ZL-151202
		runPyScript(editStack); //call python script to run Aspen Plus model "BiodiesePlant" ZL-151202
		
	}
	
 	public static void writeAPCSV(ArrayList<String[]> skeleton, String APINCSV){  
	        FileWriter flieWriter = null;
	        BufferedReader fileReader =null;
	        
	        try{
//	        long start =System.currentTimeMillis();
	        String line=null;
	        fileReader =new BufferedReader(new FileReader(APINCSV));	
	        fileReader.readLine(); //Read the CSV file header to skip it
	        flieWriter = new FileWriter(test);
	        
	        while((line=fileReader.readLine())!=null){
	        	String[] data =line.split(",");
	        	//data[0] =FOIL;
	        	//data[1] =FMEOH;
	        	for(int i=0; i<skeleton.size(); i++){
	   	         flieWriter.append(skeleton.get(i)[0]); //write headers-Aspen Plus input names
	   	         flieWriter.append("\n");  //New line
	        	if(!data[0].trim().isEmpty()){flieWriter.append(data[0]);flieWriter.append(",");}
	        	if(!data[1].trim().isEmpty()){flieWriter.append(data[1]);}
	           }
	       }        
/*ZL-151203*	        	
	        flieWriter = new FileWriter(test);
	        for(int i=0; i<skeleton.size(); i++){
	         flieWriter.append(skeleton.get(i)[0]); //write headers-Aspen Plus input names
	         flieWriter.append("\n");  //New line
	         String[] ArcGISfields = skeleton.get(i)[1].split(",");
//	         Map<String,Object> attributes = attributeslist.get(i); //pulls all date fields available from ArcGIS
	         
		 for(int j=0; j<ArcGISfields.length; j++) {
		    if(ArcGISfields[j].equals("FOIL")) {
			String ArcGISOILF ="28.04";
//		    String ArcGISOILF =String.valueOf(APINCSV.get(j));
			flieWriter.append(ArcGISOILF);
			flieWriter.append(",");
			}
			else if(ArcGISfields[j].equals("FMEOH")){
			String ArcGISMEOHF ="101.899";
			flieWriter.append(ArcGISMEOHF);
			}			  	  	         
	        } 	         
 
	        }
ZL-151203*/	        
	       } catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					flieWriter.flush();
					flieWriter.close();
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
 	
    public static void runPyScript(ArrayList<String[]> editStack) {

		try {
				Process p = Runtime.getRuntime().exec(runPythonCommandAP);
				p.waitFor();
				System.out.println("Exit Value (0 means success): " + p.exitValue()); // if console prints 0 it means success
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line; // retrieves console from python script
				System.out.println("Python input:");
				while ((line=br.readLine())!=null) {
					System.out.println(line); // print input array from Python (see python code for more details)
				}
				line = br.readLine();					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}