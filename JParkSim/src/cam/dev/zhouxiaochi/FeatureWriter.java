package cam.dev.zhouxiaochi;
import com.esri.runtime.ArcGISRuntime;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.CoordinateConversion.UTMConversionMode;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Transformation2D;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.map.ArcGISFeatureLayer;
import com.esri.map.ArcGISFeatureLayer.SELECTION_METHOD;
import com.esri.map.ArcGISTiledMapServiceLayer;
import com.esri.map.JMap;
import com.esri.map.LayerList;
import com.esri.map.MapOptions;
import com.esri.map.MapOptions.MapType;
import com.esri.map.MapOverlay;
import com.esri.map.QueryMode;



public class FeatureWriter {

	public static double delta_x;
	public static double delta_y;
	
	public static double _x;
	public static double _y;
	
	public static double center_x;
	public static double center_y;
	
	public static String name;
	public static int angle = 45;
	
	
	public static double x_min;
	public static double x_max;
 
	
	public static double y_min;
	public static double y_max;
	
	
	
 
	public static double length1 ;
	public static double length2;
	public static double beta1;
	public static double beta2;
	
	public static Point p1 = new Point();
	public static Point p2 = new Point();
	public static Point p3 = new Point();
	public static Point p4 = new Point();
 
	public static Point p_left_button = new Point (11541440.87421663,140088.23280191634);
 	
	public static double bioplant_length = 0;
	public static double bioplant_width  = 0;
	
	public Rectangle obstacle;
	
	
	
	/***
	 * Create a feature basing on the coordinates, type and spatial reference
	 * @param type  type of the feature e.g. Blower
	 * @param x		the longitude of the feature (decimal coordinate)
	 * @param y		the latitude    of the feature (decimal coordinate)
	 * @param ref    the spatial reference of the layer
	 *  @return Graphic[] the array of features to be added
	 * @throws Exception 
	 */
	public Graphic[] createFeature(String type , double x , double y,SpatialReference ref, String input_name ) throws Exception
	{
		
		
		bioplant_length = 35;
		bioplant_width  =  30;
		
		
      OWLReader.read_owl_file(null, input_name);
    
		  Polygon polygon = new Polygon();
		 
 	  SimpleLineSymbol outline = new SimpleLineSymbol(new Color(33, 150, 0), 300);
	   SimpleFillSymbol symbol2 = new SimpleFillSymbol(new Color(0, 240, 0, 180), outline);
		    
	 	  SimpleLineSymbol outline2 = new SimpleLineSymbol(new Color(133, 234, 0), 300);
		   SimpleFillSymbol symbol3 = new SimpleFillSymbol(new Color(163, 23, 0, 180), outline2);
		  
		  
		  
 		  double value = 1.521513125;
 		  
		 Map<String,Object> attributes = new HashMap<String,Object>();
		
		 for(int i = 0 ; i < OWLReader.name_list.size(); i++)
		 {
			 attributes.put(OWLReader.name_list.get(i), OWLReader.value_list.get(i));
			 System.out.println("------------------------------");
			 System.out.println(OWLReader.name_list.get(i)); 
			 System.out.println(OWLReader.value_list.get(i));
			 System.out.println("------------------------------");
		 }
		 
		 
 
	 	InfoReader.readXML(type, input_name);
	 	System.out.println("===============================================");
	 	System.out.println(input_name);
	 	System.out.println("input x  ---> " + x);
	 	System.out.println("input y  ---> " + y);
	 	
	 	
	 	center_x = InfoReader.center_x;
	 	center_y = InfoReader.center_y;
 
	    
 
	    
		 
		File inputFile = new File("Prefab/"+ type + ".xml");
		
        DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize(); 
        Element root = doc.getDocumentElement();
        Node coordinates = root.getChildNodes().item(1);
       
        
        String coordinate_string = coordinates.getTextContent();
        int length_2 = coordinate_string.split(" ").length;
        String[] temp = new String[length_2];
        temp = coordinate_string.split(" ");
        
        // get the orientation from owl file; 
        
        
        int counter = 0;
        for(String point : temp)
        {
         	 point = point.trim();
        	 if(point.contains(","))
 
          	 {
        	String[] temp2 = new String[3];
        	temp2 = point.split(",");
 
               _x = Double.parseDouble(temp2[0]);
        	   _y = Double.parseDouble(temp2[1]);
 
        	   
            if(counter == 0)
            {
             delta_x = x - center_x;
             delta_y = y - center_y; // y center
             polygon.startPath(_x + delta_x , _y + delta_y); 
             
 
             
             
            }
            else
            {
  
            	polygon.lineTo(_x + delta_x , _y + delta_y);
            	 
             
            }
             
          	 }
        	 
        	 counter++;
        }
        
        polygon.closePathWithLine();
        
         
        
        
        
  /*
        double angleRad = Math.toRadians(-45);
        Transformation2D rotateTx = new Transformation2D();
        
        rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), new Point(_x,_y));

        Transformation2D scale = new Transformation2D();
        scale.scale(0.5, 0.5);
     
        // apply transformation on the point
        polygon.applyTransformation(rotateTx);
        polygon.applyTransformation(scale);
           */

 
 
         
        
 
         
          Polygon Enclosure_polygon = new Polygon();
          Enclosure_polygon.startPath(x_min, y_min);
          Enclosure_polygon.lineTo(x_max, y_min);
          Enclosure_polygon.lineTo(x_max, y_max);
          Enclosure_polygon.lineTo(x_min,y_max);
          Enclosure_polygon.closePathWithLine();
         
          
          
          Polygon temp_poly = new Polygon();
          
          
          double angleRad = Math.toRadians(45);
          Transformation2D rotateTx = new Transformation2D();
          rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), new Point(x,y));
          temp_poly = polygon;
          temp_poly.applyTransformation(rotateTx);
       
          double max_y = temp_poly.getPoint(0).getY();
          double min_y = temp_poly.getPoint(0).getY();
          double max_x = temp_poly.getPoint(0).getX();
          double min_x = temp_poly.getPoint(0).getX();
          
          
          
          
          for(int i = 0; i < temp_poly.getPointCount();i++)
          {
        	Point pointx =  temp_poly.getPoint(i);
         	if(pointx.getX() > max_x)
        	{
         		max_x = pointx.getX();
 
        	}
        	
        	if(pointx.getX() < min_x)
        	{
        		min_x = pointx.getX() ;
 
        	}
        	
        	if( pointx.getY() > max_y)
        	{
        		max_y =  pointx.getY();
 
        	}
        	
        	if(pointx.getY()< min_y)
        	{
        		min_y =  pointx.getY();
 
        	}
        	
          }
          
          
          
          
          
          
          
          
          Polygon Enclosure2 = new Polygon();
          Enclosure2.startPath(min_x, min_y);
          Enclosure2.lineTo(max_x, min_y);
          Enclosure2.lineTo(max_x, max_y);
          Enclosure2.lineTo(min_x,max_y);
          
          Enclosure2.closePathWithLine();
  
          
          
          
          
          
          
          
          
          
          
     
         angleRad = Math.toRadians(-45);
          rotateTx = new Transformation2D();
          rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), new Point(x,y));
          polygon.applyTransformation(rotateTx);
     
          /*
          angleRad = Math.toRadians(45);
          rotateTx = new Transformation2D();
          rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), p_left_button);
          polygon.applyTransformation(rotateTx);
          */
          
          
       
       //   p_right_top
 
           Enclosure2.applyTransformation(rotateTx);
          
          
           
           
           angleRad = Math.toRadians(45);
           rotateTx = new Transformation2D();
           rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), p_left_button);
           Enclosure2.applyTransformation(rotateTx);
            
         
           double enclosure_width = 0; 
           double enclosure_height = 0;
           enclosure_width =max_x - min_x;
           enclosure_height = max_y - min_y;
           
           
           
           
           obstacle = new Rectangle(Enclosure2.getPoint(3).getX() - p_left_button.getX(),Enclosure2.getPoint(3).getY() - p_left_button.getY(),enclosure_width,enclosure_height);
           
           
           
           
           
          Polygon bioplant_enclosure = new Polygon();
          bioplant_enclosure.startPath(p_left_button);
          bioplant_enclosure.lineTo(p_left_button.getX(), p_left_button.getY() + bioplant_length);
          bioplant_enclosure.lineTo(p_left_button.getX() + bioplant_width, p_left_button.getY() + bioplant_length);
          bioplant_enclosure.lineTo(p_left_button.getX() + bioplant_width, p_left_button.getY() );
          
          
          
          
          
          bioplant_enclosure.closePathWithLine();
           
           
 	    Graphic polygonGraphic = new Graphic(polygon, symbol2,attributes);
 	    Graphic  enclosure2 = new Graphic(Enclosure2,symbol3, null);
 	    Graphic bioplant_enclosure_Graphic = new Graphic(bioplant_enclosure,symbol3,null);
 	    
 	    
 	    
 	    
 //	    Graphic[] adds = {enclosure2,polygonGraphic};
 	   Graphic[] adds = {polygonGraphic};
		return adds;
		 
	}
	
	
	 
	
 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	 
}
