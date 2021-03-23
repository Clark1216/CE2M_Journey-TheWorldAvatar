from py4jps.resources import JpsBaseLib
import os
from tqdm import tqdm
import time
import numpy as np 
import pandas as pd


# start of geoJSON file 
geojson_file = """
{
  "type": "FeatureCollection",
  "features": ["""

offtake_types = ['LocalDistribution','PowerStation','IndustrialUser','Storage']
colors = ['#f78086','#ca3549','#5c1d20','#f9372d']

jpsBaseLibGW = JpsBaseLib()
jpsBaseLibGW.launchGateway()

jpsGW_view = jpsBaseLibGW.createModuleView()
jpsBaseLibGW.importPackages(jpsGW_view,"uk.ac.cam.cares.jps.base.query.*")

KGRouter = jpsGW_view.KGRouter

for i in range(len(offtake_types)):
  offtake_type = offtake_types[i]
  color = colors[i]
  queryString = """PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
  PREFIX ns1:     <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
  PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
  PREFIX gasgrid: <http://www.theworldavatar.com/ontology/ontogasgrid/gas_network_system.owl#>
  PREFIX loc:     <http://www.bigdata.com/rdf/geospatial/literals/v1#>
  PREFIX geo:     <http://www.bigdata.com/rdf/geospatial#>
  PREFIX comp:	<http://www.theworldavatar.com/ontology/ontogasgrid/gas_network_components.owl#>

  SELECT ?location ?label
  WHERE
  {
  ?term rdf:type comp:%s.
  ?term rdfs:label ?label.
  ?term loc:lat-lon ?location.
  }"""%(offtake_type)
  
  KGClient = KGRouter.getKnowledgeBaseClient('http://kb/ontogasgrid', True, False)
  ret = KGClient.executeQuery(queryString)
  ret = ret.toList()
  num_ret = len(ret)
  ret_array = np.zeros((num_ret,3),dtype='object')
  header = ['lat','lon','name']
  for i in tqdm(range(num_ret)):
      try:
          lat,lon = ret[i]['location'].split('#')
          ret_array[i,:] = [lat,lon+',',ret[i]['label']]
      except:
          ret_array[i,:] = ['','',ret[i]['label']]
  ret = pd.DataFrame(ret_array,columns=header).values
  

  # <http://www.theworldavatar.com/kb/ontogasgrid/offtakes_abox/Langholm>

  for i in range(num_ret):
    if len(ret[i,0]) > 1:

      feature = """{
        "type": "Feature",
        "properties": {
          "marker-color": "%s",
          "marker-size": "medium",
          "marker-symbol": "",
          "Offtake Point (License Name)": "%s",
          "Type of Offtake": "%s"
        },
        "geometry": {
          "type": "Point",
          "coordinates": [
            %s
            %s
          ]
        }
      },"""%(color,ret[i,2],offtake_type,ret[i,1],ret[i,0])
      geojson_file += '\n'+feature

    else:
      feature = """{
        "type": "Feature",
        "properties": {
          "marker-color": "%s",
          "marker-size": "medium",
          "marker-symbol": "",
          "Offtake Point (License Name)": "%s",
          "Type of Offtake": "%s"
        },
        "geometry": {
          "type": "Polygon",
          "coordinates": []
        }
      },"""%(color,ret[i,2],offtake_type)
      geojson_file += '\n'+feature

# removing last comma as is last line
geojson_file = geojson_file[:-1]
# finishing file end 
end_geojson = """
  ]
}
"""
geojson_file += end_geojson
# saving as geoJSON
output_folder = 'OntoGasGrid/query_tools/geoJSON_output'
try:
  os.mkdir(output_folder)
except FileExistsError:
  print('Directory already exists')

geojson_written = open(output_folder+'/offtakes.geojson','w')
geojson_written.write(geojson_file)
geojson_written.close() 

            
    
