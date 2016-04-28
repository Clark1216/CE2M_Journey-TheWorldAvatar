package Semakau;
/* ArcGIS Online Account that stores hosted feature layers:
 * Username: semakausimulator
 * Password: c4tsemakau
*/
/* CURRENTLY KNOWN ISSUES THAT CAN BE IMPROVED:
 * 3. No method to extract layers from LayerList map.getLayers(), have to manually add layers to array ArcGISFeatureLayer[] completeLayerList
 * 4. Cannot draw and edit features yet
 */

// For more information and API reference on the ArcGIS SDK for Java, go to https://developers.arcgis.com/java/
//import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.Style;
import com.esri.core.symbol.Symbol;
import com.esri.map.ArcGISFeatureLayer;
import com.esri.map.ArcGISTiledMapServiceLayer;
import com.esri.map.JMap;
import com.esri.map.LayerList;
import com.esri.map.MapEvent;
import com.esri.map.MapEventListenerAdapter;
import com.esri.map.popup.PopupDialog;
import com.esri.map.popup.PopupView;
import com.esri.map.popup.PopupViewEvent;
import com.esri.map.popup.PopupViewListener;
import com.esri.runtime.ArcGISRuntime;
import com.esri.toolkit.legend.JLegend;
import com.esri.toolkit.overlays.HitTestEvent;
import com.esri.toolkit.overlays.HitTestListener;
import com.esri.toolkit.overlays.HitTestOverlay;

public class Semakau {

//	private static final long serialVersionUID = 1L;
	// style of different layers
	final static SimpleFillSymbol Buildingscolor       = new SimpleFillSymbol(Color.orange); // 2D
	final static SimpleFillSymbol Desalinationcolor    = new SimpleFillSymbol(new Color(139,69,19)); // 2D
	final static SimpleFillSymbol EnergyStoragecolor   = new SimpleFillSymbol(Color.green); // 2D
	final static SimpleFillSymbol FishHatcherycolor    = new SimpleFillSymbol(Color.cyan); // 2D
	final static SimpleFillSymbol LandLotscolor        = new SimpleFillSymbol(Color.cyan, new SimpleLineSymbol(Color.cyan, 1), SimpleFillSymbol.Style.NULL); // 2D
	final static SimpleMarkerSymbol Loadpointcolor     = new SimpleMarkerSymbol(new Color(127,0,255), 10, Style.DIAMOND); // 0D
	final static SimpleLineSymbol LoadTLinecolor       = new SimpleLineSymbol(new Color(0,100,0), 3); // 1D
	final static SimpleFillSymbol MarinePowerGencolor  = new SimpleFillSymbol(Color.red); // 2D
	final static SimpleLineSymbol MarineTLinecolor     = new SimpleLineSymbol(Color.green, 3); // 1D
	final static SimpleLineSymbol Roadscolor           = new SimpleLineSymbol(new Color(204,204,0), 3); // 1D
	final static SimpleFillSymbol Solarfarmcolor       = new SimpleFillSymbol(Color.yellow); // 2D
	final static SimpleMarkerSymbol SolarInvertercolor = new SimpleMarkerSymbol(Color.blue, 20, Style.CIRCLE); // 0D	
	final static SimpleLineSymbol SolarPowerTLinecolor = new SimpleLineSymbol(Color.green, 3); // 1D
	final static SimpleFillSymbol Windfarmcolor        = new SimpleFillSymbol(Color.blue, new SimpleLineSymbol(Color.blue, 2), SimpleFillSymbol.Style.NULL); // 2D
	final static SimpleLineSymbol WindPowerTLinecolor  = new SimpleLineSymbol(Color.green, 3); // 1D
	final static SimpleMarkerSymbol WindTransformercolor = new SimpleMarkerSymbol(Color.magenta, 10, Style.TRIANGLE); // 0D
	final static SimpleMarkerSymbol WindTurbinecolor   = new SimpleMarkerSymbol(Color.red, 20, Style.CROSS); // 0D
	final static SimpleFillSymbol dieselgencolor       = new SimpleFillSymbol(Color.pink);
	
	private JFrame window;
	private JMap map;
	public static JLayeredPane contentPane;
	// initialize layers

	public static ArcGISFeatureLayer Buildingslayer;
	public static ArcGISFeatureLayer Desalinationlayer;
	public static ArcGISFeatureLayer EnergyStoragelayer;
	public static ArcGISFeatureLayer FishHatcherylayer;
	public static ArcGISFeatureLayer LandLotslayer;
	public static ArcGISFeatureLayer Loadpointlayer;
	public static ArcGISFeatureLayer LoadTLinelayer;
	public static ArcGISFeatureLayer MarinePowerGenlayer;
	public static ArcGISFeatureLayer MarineTLinelayer;
	public static ArcGISFeatureLayer Roadslayer;  
	public static ArcGISFeatureLayer Solarfarmlayer;
	public static ArcGISFeatureLayer SolarInverterlayer;
	public static ArcGISFeatureLayer SolarPowerTLinelayer;
	public static ArcGISFeatureLayer Windfarmlayer;
	public static ArcGISFeatureLayer WindPowerTLinelayer;
	public static ArcGISFeatureLayer WindTransformerlayer;
	public static ArcGISFeatureLayer WindTurbinelayer;
	public static ArcGISFeatureLayer dieselgenlayer;
	
	// method to render all layers in an array using a certain style (multiple layer renderer)
	private void createRenderer(LayerList layers, ArcGISFeatureLayer[] arrayoflayers, Symbol col) {
		for (ArcGISFeatureLayer layer : arrayoflayers) {
			layer.setRenderer(new SimpleRenderer(col));
			layers.add(layer);
		}
	}
/*
	private JPanel successMessage (long updatingTime) {
	    JPanel panel2 = new JPanel();
	    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
	    panel2.setSize(200, 50);
	    panel2.setLocation(500, 50);
	    panel2.setBackground(new Color(0, 0, 0));
	    panel2.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
	    JTextArea indicator = new JTextArea("PowerWorld has Finished Running (" + String.valueOf(updatingTime) + "ms)");
	    indicator.setFont(new Font("Verdana", Font.BOLD, 16));
	    indicator.setForeground(Color.WHITE);
	    indicator.setBackground(new Color(0, 0, 0, 0));
	    indicator.setEditable(false);
	    indicator.setLineWrap(true);
	    indicator.setWrapStyleWord(true);
	    indicator.setAlignmentX(Component.LEFT_ALIGNMENT);
	    panel2.add(indicator);
	    panel2.setVisible(false);
		return panel2;
	}
*/	
	
  public Semakau() { // class constructor
	  
    // empty JMap constructor and add a tiled basemap layer
    map = new JMap();
    final LayerList layers = map.getLayers(); // object storing all the map layers (NOT AN ARRAY - use completelayerlist instead)
    ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
    layers.add(tiledLayer); // add basemap layer
    
    // map centered on Jurong Island
    Point mapCenter = new Point(11551580,134038);
    map.setExtent(new Envelope(mapCenter,7200,5400));
    map.addMapEventListener(new MapEventListenerAdapter() {
    	@Override
    	public void mapReady(MapEvent event) {
    		System.out.println("Map has finished loading");
    	}
    });
  
    // adds layers uploaded onto ArcGIS for Developers
    UserCredentials user = new UserCredentials();
    user.setUserAccount("semakausimulator", "c4tsemakau"); // Access secure feature layer service using login username and password
    Buildingslayer       = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Building/FeatureServer/0", user);
    Desalinationlayer    = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Desalination/FeatureServer/0", user);
    EnergyStoragelayer   = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/EnergyStorage/FeatureServer/0", user);
    FishHatcherylayer    = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/FishHatchery/FeatureServer/0", user);
    LandLotslayer        = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/LandLots/FeatureServer/0", user);
    Loadpointlayer       = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Loadpoint/FeatureServer/0", user);
    LoadTLinelayer       = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/ArcGIS/rest/services/LoadTLine/FeatureServer/0", user);
    MarinePowerGenlayer  = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/MarinePowerGen/FeatureServer/0", user);
    MarineTLinelayer     = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/MarineTLine/FeatureServer/0", user);
    Roadslayer           = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Roads/FeatureServer/0", user);  
    Solarfarmlayer       = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Solarfarm/FeatureServer/0", user);
    SolarInverterlayer   = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/ArcGIS/rest/services/SolarInverter/FeatureServer/0", user);   
    SolarPowerTLinelayer = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/SolarPowerTLine/FeatureServer/0", user);
    Windfarmlayer        = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/Windfarm/FeatureServer/0", user);
    WindPowerTLinelayer  = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/WindPowerTLine/FeatureServer/0", user);
    WindTransformerlayer = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/WindTransformer/FeatureServer/0", user);
    WindTurbinelayer     = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/WindTurbine/FeatureServer/0", user);
    dieselgenlayer       = new ArcGISFeatureLayer("http://services3.arcgis.com/785KAqvbaBANxwtT/arcgis/rest/services/DieselGen/FeatureServer/0", user);
	// UPDATE THIS LIST whenever new layers are added: first layer is the bottommost layer *see currently known issues #3


	ArcGISFeatureLayer[] completeLayerList = {Buildingslayer, Desalinationlayer, EnergyStoragelayer, FishHatcherylayer, LandLotslayer, Loadpointlayer,
			LoadTLinelayer, MarinePowerGenlayer, MarineTLinelayer, Roadslayer, Solarfarmlayer, SolarInverterlayer, SolarPowerTLinelayer, Windfarmlayer, 
			WindPowerTLinelayer, WindTransformerlayer, WindTurbinelayer,dieselgenlayer};

    // render layers

    createRenderer(layers, new ArcGISFeatureLayer [] {Buildingslayer}, Buildingscolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {Desalinationlayer}, Desalinationcolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {EnergyStoragelayer}, EnergyStoragecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {FishHatcherylayer}, FishHatcherycolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {LandLotslayer}, LandLotscolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {Loadpointlayer}, Loadpointcolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {LoadTLinelayer}, LoadTLinecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {MarinePowerGenlayer}, MarinePowerGencolor);   
    createRenderer(layers, new ArcGISFeatureLayer [] {MarineTLinelayer}, MarineTLinecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {Roadslayer}, Roadscolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {Solarfarmlayer}, Solarfarmcolor);  
    createRenderer(layers, new ArcGISFeatureLayer [] {SolarInverterlayer}, SolarInvertercolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {SolarPowerTLinelayer}, SolarPowerTLinecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {Windfarmlayer}, Windfarmcolor);  
    createRenderer(layers, new ArcGISFeatureLayer [] {WindPowerTLinelayer}, WindPowerTLinecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {WindTransformerlayer}, WindTransformercolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {WindTurbinelayer}, WindTurbinecolor);
    createRenderer(layers, new ArcGISFeatureLayer [] {dieselgenlayer}, dieselgencolor);
        
    // initialize window and add contentPane to window
    window = new JFrame("Semakau Simulator");
    window.setSize(1200, 900);
    window.setLocationRelativeTo(null); // centered on screen
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.getContentPane().setLayout(new BorderLayout(0, 0));

    
    // textbox indicating that PowerWorld finished running
    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
    panel2.setSize(200, 50);
    panel2.setLocation(500, 50);
    panel2.setBackground(new Color(0, 0, 0));
    panel2.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    JTextArea indicator = new JTextArea("PowerWorld has Finished Running");
    indicator.setFont(new Font("Verdana", Font.BOLD, 16));
    indicator.setForeground(Color.WHITE);
    indicator.setBackground(new Color(0, 0, 0, 0));
    indicator.setEditable(false);
    indicator.setLineWrap(true);
    indicator.setWrapStyleWord(true);
    indicator.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel2.add(indicator);
    panel2.setVisible(false);
    
    // PowerWorld warning message
    JPanel panel3 = new JPanel();
    panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
    panel3.setSize(200, 50);
    panel3.setLocation(500, 50);
    panel3.setBackground(new Color(0, 0, 0));
    panel3.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    JTextArea warning = new JTextArea("You did not edit any features!");
    warning.setFont(new Font("Verdana", Font.BOLD, 16));
    warning.setForeground(Color.WHITE);
    warning.setBackground(new Color(0, 0, 0, 0));
    warning.setEditable(false);
    warning.setLineWrap(true);
    warning.setWrapStyleWord(true);
    warning.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel3.add(warning);
    panel3.setVisible(false);
    
	// create panel to select layer to edit
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setSize(220, 80);
    panel.setLocation(260, 10); // located near top left next to legend
    
    // create text
    JTextArea description = new JTextArea("Click on a feature to start editing");
    description.setFont(new Font("Verdana", Font.PLAIN, 11));
    description.setForeground(Color.WHITE);
    description.setBackground(new Color(0, 0, 0, 0));
    description.setEditable(false);
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    description.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // create label for dropdown selector
    JLabel lblLayer = new JLabel("Select a feature to edit:");
    lblLayer.setForeground(Color.WHITE);
    lblLayer.setAlignmentX(Component.LEFT_ALIGNMENT);
    // create dropdown selector for layer via key-value pairs
    final Map<String, ArcGISFeatureLayer> editlayer = new LinkedHashMap<>();
    // dropdown options with key = String layer name and value = layer object

    editlayer.put("Building", Buildingslayer);
    editlayer.put("Desalination", Desalinationlayer);
    editlayer.put("EnergyStorage", EnergyStoragelayer);
    editlayer.put("FishHatchery", FishHatcherylayer);
    editlayer.put("LandLots", LandLotslayer);
    editlayer.put("Loadpoint", Loadpointlayer);
    editlayer.put("LoadTLine", LoadTLinelayer);
    editlayer.put("MarinePowerGen", MarinePowerGenlayer);
    editlayer.put("MarineTLine", MarineTLinelayer);
    editlayer.put("Roads", Roadslayer);
    editlayer.put("Solarfarm", Solarfarmlayer);
    editlayer.put("SolarInverter", SolarInverterlayer);
    editlayer.put("SolarPowerTLine", SolarPowerTLinelayer);
    editlayer.put("Windfarm", Windfarmlayer);
    editlayer.put("WindPowerTLine", WindPowerTLinelayer);
    editlayer.put("WindTransformer", WindTransformerlayer);
    editlayer.put("WindTurbine", WindTurbinelayer);
    editlayer.put("Diesel Generator", dieselgenlayer);
    

    // initialize dropdown box
    final JComboBox<String> cbxLayer = new JComboBox<>(editlayer.keySet().toArray(new String[0]));
    cbxLayer.setMaximumSize(new Dimension(220, 25));
    cbxLayer.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    // create a stack of edited features for PowerWorld to execute on
    ArrayList<String[]> editStack = new ArrayList<String[]>();
    
    // container for listeners arranged by index of layers in completeLayerList
    HitTestOverlay[] listenerList = new HitTestOverlay[completeLayerList.length];
    System.out.println(completeLayerList.length);
    for (ArcGISFeatureLayer layer : completeLayerList) { // add event listener to all layers in completeLayerList
        final HitTestOverlay hitTestOverlay = new HitTestOverlay(layer); // listener is of type HitTestOverlay
        hitTestOverlay.addHitTestListener(new HitTestListener() { // listens for MOUSE CLICK on feature
        	@Override
        	public void featureHit(HitTestEvent event) {
    	        HitTestOverlay overlay = event.getOverlay();
    	        Graphic hitGraphic = (Graphic) overlay.getHitFeatures().get(overlay.getHitFeatures().size()-1); // get bottom-most graphic hit by mouse-click
    	        try {
    		          // create a popup in edit view
    		          PopupView contentPanel = PopupView.createEditView("Edit Attributes", layer); 
    		          contentPanel.setGraphic(layer, hitGraphic);
    		          // highlight selected graphic and unselect previously selected graphic by searching all layers
    		          for (ArcGISFeatureLayer somelayer : completeLayerList) {
    		        	  
    		        	  if (somelayer.getSelectedFeatures() != null) {  // layers outside map extent will produce null error, ignore these
	    			          for (Graphic graphic : somelayer.getSelectedFeatures()) { // search for selected features
	    			        	  somelayer.unselect((int) graphic.getId()); // unselect them by graphic Id
	    			          }
    		        	  }
    		          }
    		          layer.select((int) hitGraphic.getId()); // highlight selected graphic
    		          // create map popup to display the popup view
    		          final PopupDialog popup = map.createPopup(
    		              new JComponent[]{contentPanel}, hitGraphic);
    		          popup.setTitle("Edit Attributes: " + layer.getName());
    		          popup.setVisible(true);
    		          popup.setLocationRelativeTo(null); // centered on screen
    		          contentPanel.addPopupViewListener(new PopupViewListener() {
    		              @Override
    		              public void onCommitEdit(PopupViewEvent popupViewEvent, Feature feature) { // save button
    		            	  // newFeature is a new String[] element to be added to editStack (e.g. {Load_Points, 103})
    		            	  String[] newFeature = new String[] {layer.getName(), String.valueOf(hitGraphic.getAttributes().get("FID"))};
    		            	  boolean addtoStack = true;
    		            	  for (int i=0; i<editStack.size(); i++) { // check through each element in editStack
   		            		  String itemlayer = editStack.get(i)[0];
    		            		  String graphicFID = editStack.get(i)[1];
    		            		  if (layer.getName().equals(itemlayer) && String.valueOf(hitGraphic.getAttributes().get("FID")).equals(graphicFID)) {
    		            			  addtoStack = false; // if identical is feature found, don't add to editStack
    		            		  }
    		            	  }
     		            	  if (addtoStack) { // add only if not duplicate
    		            		  editStack.add(newFeature);
    		            	  }
    		            	  popup.close();
    		              }
    		              @Override
    		              public void onCancelEdit(PopupViewEvent popupViewEvent, Feature feature) { // cancel button
	  		            	  layer.unselect((int) hitGraphic.getId()); // unselect feature on cancel
    		            	  popup.close();
    		              }
    		            });
    		          } catch (Exception e) {
    		            e.printStackTrace();
    		          }
        	}
        });
        hitTestOverlay.setActive(false); // disable all listeners initially to prevent conflict
        listenerList[Arrays.asList(completeLayerList).indexOf(layer)] = hitTestOverlay; // add listener(overlay) to an array arranged in indexed order
        map.addMapOverlay(hitTestOverlay); // add all layer listeners to map
    }
    listenerList[0].setActive(true); // default layer listener enabled is the first one (landlots layer)
    
    cbxLayer.addItemListener(new ItemListener() { // dropdown list event listener
      @Override
      public void itemStateChanged(ItemEvent arg0) {
        if (arg0.getStateChange() == ItemEvent.SELECTED) { // whenever dropdown list changes
          for (ArcGISFeatureLayer somelayer : completeLayerList) { // unselect all graphics
        	  if (somelayer.getSelectedFeatures() != null) { // ignore layers outside map extent
		          for (Graphic graphic : somelayer.getSelectedFeatures()) { // search for selected features
		        	  somelayer.unselect((int) graphic.getId()); // unselect them by graphic Id
		          }
			  }
			}
          for (HitTestOverlay overlay : listenerList) {
        	  overlay.setActive(false); // disable all listeners everytime selected layer is changed (reset)
          }
          
          ArcGISFeatureLayer chosenlayer = editlayer.get(cbxLayer.getSelectedItem());
          int index = Arrays.asList(completeLayerList).indexOf(chosenlayer); // get index of selected layer from completeLayerList
          listenerList[index].setActive(true); // enable the listener currently selected
        }
      }
    });

    // Run PowerWorld button
    JButton PWbutton = new JButton("Run PowerWorld");
    PWbutton.addActionListener(new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent arg0) {
    		HttpURLConnection urlCon;
    		OutputStreamWriter out;
    		URL url;
    		try {
				url = new URL("http://www.jparksimulator.com/semPWServlet/"); // URL of servlet
				urlCon = (HttpURLConnection) url.openConnection();
				urlCon.setRequestMethod("POST");
				urlCon.setDoOutput(true);
				JOptionPane.showMessageDialog(null, "urlCon output is " + new OutputStreamWriter(urlCon.getOutputStream(), "UTF-8"));
				
				if (editStack.isEmpty()) {
					JOptionPane.showMessageDialog(null, "You did not edit any features for PowerWorld!");
				} else {
					out = new OutputStreamWriter(urlCon.getOutputStream(), "UTF-8");
					StringBuilder layers = new StringBuilder();
					StringBuilder FIDs = new StringBuilder();
					for (String[] item : editStack) { // create comma separated values
						layers.append(item[0]);
						layers.append(",");
						FIDs.append(item[1]);
						FIDs.append(",");
					}
					StringBuilder outputString = new StringBuilder();
					// Only URL encoded string values can be sent over a HTTP connection
					outputString.append(URLEncoder.encode("layers", "UTF-8"));
					outputString.append("=");
					outputString.append(URLEncoder.encode(layers.toString(), "UTF-8"));
					outputString.append("&");
					outputString.append(URLEncoder.encode("FIDs", "UTF-8"));
					outputString.append("=");
					outputString.append(URLEncoder.encode(FIDs.toString(), "UTF-8"));
					
					// Example of comma separated outputString is "layers=Load_Points,Load_Points,&FIDs=103,104,"
					DataOutputStream wr = new DataOutputStream(urlCon.getOutputStream());
					wr.writeBytes(outputString.toString()); // write query string into servlet doPost() method
					wr.flush();
					wr.close();
					
					if (urlCon.getResponseCode()==200) {
						JOptionPane.showMessageDialog(null, "PowerWorld has finished running!");
						editStack.clear(); // delete all items in editStack
					} else {
						JOptionPane.showMessageDialog(null, "An error has occurred. HTTP Error: " + urlCon.getResponseCode()
								+ "\nPlease try running PowerWorld again");
					}
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    		for (ArcGISFeatureLayer layer : completeLayerList) {
    			layer.requery();
    			layer.refresh();
/*
    		SemPowerWorld PWinstance = new SemPowerWorld();
    		PWinstance.runPowerWorld(editStack);
    		Loadpointlayer.requery(); // Won't exactly refresh user interface unless pan or zoom is employed
    		map.zoom(1.01); // Workaround to refresh map
    		// FIND A WAY TO MAKE THIS MESSAGE DISAPPEAR AFTER 2 SECONDS
    		if (!editStack.isEmpty()) {
//   			JPanel panel2 = successMessage(PWinstance.updatingTime);
    			panel2.setVisible(true);
    			panel3.setVisible(false);
    		} else {
    			panel3.setVisible(true);
    			panel2.setVisible(false);
    		}
    		editStack.clear(); // delete all items in editStack
    	}
*/
			}
		}	
	});
    PWbutton.setEnabled(true);
    PWbutton.setVisible(true);
    PWbutton.setSize(150,30);
    PWbutton.setLocation(500, 10);    

    JButton refreshButton = new JButton("Refresh Map");
    refreshButton.addActionListener(new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent arg0) {
    		for (ArcGISFeatureLayer layer : completeLayerList) {
    			layer.requery();
    			layer.refresh();
    		}
    	}
    });
    refreshButton.setEnabled(true);
    refreshButton.setVisible(true);
    refreshButton.setSize(150,30);
    refreshButton.setLocation(650, 10);
    
    // combine text, label and dropdown list into one panel for selecting layer to edit
    panel.setBackground(new Color(0, 0, 0, 180));
    panel.add(description);
    panel.add(Box.createRigidArea(new Dimension(0, 5)));
    panel.add(lblLayer);
    panel.add(Box.createRigidArea(new Dimension(0, 5)));
    panel.add(cbxLayer);
    panel.add(Box.createRigidArea(new Dimension(0, 5)));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
    // create legend
    JLegend legend = new JLegend(map);
    legend.setPreferredSize(new Dimension(250, 700));
    legend.setBorder(new LineBorder(new Color(205, 205, 255), 3));
    
    // initialize contentPane and add contents
    contentPane = new JLayeredPane();
    contentPane.setLayout(new BorderLayout(0,0));
    contentPane.setVisible(true);
    contentPane.add(PWbutton);
    contentPane.add(refreshButton);
    contentPane.add(panel);
    contentPane.add(panel2);
    contentPane.add(panel3);
    contentPane.add(legend, BorderLayout.WEST);
    contentPane.add(map, BorderLayout.CENTER);
    window.add(contentPane);
    
    // dispose map just before application window is closed.
    window.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
      	  if (!editStack.isEmpty()) { // check if ArcGIS edits have not been saved in PowerWorld .pwb file
        	  int reply = JOptionPane.showConfirmDialog(null, "ArcGIS edits may not have not been saved in PowerWorld. "
          	  		+ "Are you sure you want to close?", "Window Closing", JOptionPane.YES_NO_OPTION);
          	  if (reply == JOptionPane.YES_OPTION) {
      	        super.windowClosing(windowEvent);
      	        map.dispose();
      	        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // confirm close window
          	  } else { // if reply is NO
          		  window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // don't close window
          	  }
      	  }
        }
    });
  }
  
  /**
   * Starting point of this application.
   * @param args
   */
  public static void main(String[] args) { // main function invoked
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        try {
          Semakau application = new Semakau(); // instance of this application
          application.window.setVisible(true);
          ArcGISRuntime.setClientID("aSg9q12qgnN4OQq2"); // license app
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}