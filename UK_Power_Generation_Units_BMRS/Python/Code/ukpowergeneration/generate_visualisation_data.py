##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date:  27 Jan 2022                      #
##########################################

# Get settings and functions from the kg utils module
import kg_utils_generation as kg
from datetime import datetime as dt
import json
import os
import os.path
# Get the JVM module view (via jpsBaseLibGateWay instance) from the jpsSingletons module to access
# the TimeSeriesClient in the JPB_BASE_LIB
from jpsSingletons import jpsBaseLibView

# Specify plotting properties for GeoJSON features
geojson_attributes = { 'displayName': '',
                  'description': '',
                  'circle-color': '#FF0000',
                  'circle-stroke-width': 1,
                  'circle-stroke-color': '#000000',
                  'circle-stroke-opacity': 0.75,
                  'circle-opacity': 0.75
                  }

def get_all_time_series(powerplant, KGClient, TSClient, now, duration, start_1, start_2, start_7):
    '''
        Returns all time series data of the powerplants
    '''

    # Define query
    query = kg.create_sparql_prefix('om') + \
            kg.create_sparql_prefix('rdfs') + \
            kg.create_sparql_prefix('comp') + \
            '''SELECT ?powerplant ?dataIRI ?unit \
               WHERE { ?powerplant comp:hasTaken ?gas_amount ; \
                                 rdfs:label ?name . \
                       ?gas_quantity om:hasPhenomenon ?gas_amount . \
                       ?gas_quantity om:hasValue ?dataIRI . \
                       ?dataIRI om:hasUnit ?unit }'''
                                
    # Execute query
    response = KGClient.execute(query)

    # Convert JSONArray String back to list
    response = json.loads(response)

    # Initialise lists
    dataIRIs = []
    utilities = []
    units = []
    measurement_iri = None
    # Append lists with all query results
    for r in response:
        if r['powerplant'].lower() == powerplant.lower():
            dataIRIs.append(r['dataIRI'])
            utilities.append("Instantaneous Flow")
            units.append((r['unit']))
            measurement_iri = r['dataIRI']
            break
    # Initialise timestamps for gas flow time series retrieval durations (time series entries stored as UTC times!)
    # Java Instant instances are associated with UTC (i.e. hold a value of date-time with a UTC time-line)
    print("INFO: Submitting TimeSeriesClient SPARQL queries at",
          dt.utcfromtimestamp(now.getEpochSecond()).strftime("%Y-%m-%dT%H:%M:%SZ"))
    # Get results for last "duration" hours (e.g. 24h)
    timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_1, now)
    if not timeseries.getTimes():
        # Try last "2 x duration" hours if nothing available for last "duration" hours (e.g. 48h)
        print("WARNING: No results in last %i h, trying last %i h ..." % (duration, 2 * duration))
        timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_2, now)
        if not timeseries.getTimes():
           # Last resort, try last "7 x duration" hours (e.g. last week)
           print("WARNING: No results in last %i h, trying last %i h ..." % (2 * duration, 7 * duration))
           timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_7, now)

    # Retrieve time series data for retrieved set of dataIRIs
    timeseries = TSClient.getTimeSeries(dataIRIs)

    # Return time series and associated lists of variables and units
    return timeseries, utilities, units


def put_metadata_in_json(feature_id, lon, lat):
    """
       Structures metadata in JSON format
    """
    metadata = { 'id': feature_id,
                 'Longitude': lon,
                 'Latitude': lat
                 }
    return metadata


def get_metadata(powerplant_iri, KGClient):
    '''
        Returns meta data for the requested powerplant identified by its IRI.
    '''

    # Defines query to retrieve latitude and longitude of powerplants
    query = kg.create_sparql_prefix('rdf') + \
            kg.create_sparql_prefix('ontoenergysystem') + \
            '''SELECT ?iri ?loc \
               WHERE { ?iri rdf:type ontoenergysystem:PowerPlant ;
                             ontoenergysystem:hasWGS84LatitudeLongitude ?loc .}'''
    # Executes query
    response = KGClient.execute(query)
    # Converts JSONArray String back to list
    response = json.loads(response)
    lon = None
    lat = None
    for r in response:
        if r['iri'].lower() == powerplant_iri.lower():
            coordinates = r['loc'].split('#')
            lon = coordinates[1]
            lat = coordinates[0]
    return lon, lat


def format_in_geojson(feature_id, properties, coordinates):
    """
       It structures geodata of powerplants into geoJSON format.
    """
    feature = {'type': 'Feature',
               'id': int(feature_id),
               'properties': properties.copy(),
               'geometry': {'type': 'Point',
                            'coordinates': coordinates
                            }
               }
    return feature

#GPS Check (check_GPS)

"""The following functions are used to check (and correct) GPS coordinates. """
def check_GPS_char(c):
  #Checks to provided character is acceptable in GPS format
  acceptable = "-.0123456789"
  if c in acceptable:
    return True
  return False

def GPS_special_chars(name, coordinate):
  #Checks that if there's a '-' it's only in the first position.
  #Checks that there is only one '.' decimal point.
  acceptable = ".0123456789"
  tick = 0
  point = 0
  for i in coordinate[0]:
    if (tick > 0) and not (i in acceptable):
      coordinate[0] = coordinate[0][:tick] + coordinate[0][tick+1:]
      tick -= 1
      print("Warning: " + name + " contains (automatically removed) misplaced '-' in longitude.")
    if i == '.':
      if point > 0:
        coordinate[0] = coordinate[0][:tick] + coordinate[0][tick+1:]
        tick -= 1
        print("Warning: " + name + " contains (automatically removed) additional '.' in longitude.")
      point += 1
    tick += 1
  tick = 0
  point = 0
  for i in coordinate[1]:
    if (tick > 0) and not (i in acceptable):
      coordinate[1] = coordinate[1][:tick] + coordinate[1][tick+1:]
      tick -= 1
      print("Warning: " + name + " contains (automatically removed) misplaced '-' in latitude.")
    if i == '.':
      if point > 0:
        coordinate[1] = coordinate[1][:tick] + coordinate[1][tick+1:]
        tick -= 1
        print("Warning: " + name + " contains (automatically removed) additional '.' in latitude.")
      point += 1
    tick += 1
  return coordinate

def check_GPS_dict(ret):
  #Checks the GPS coordinates are of the correct format.
  #the 'ret' input array should have dictionaries as elements, these elements being:
  # {'name': [lon, lat]}
  #Note that the 'automatic replacement' is not in the knowledge graph, just on the output side (i.e. this funciton will not query the kg, just deal with the input dictionary).
  for i in ret:
    ret[i][0] = str(ret[i][0])
    #Check the longitude and latitude don't have unexpected characters and have at least some permitted characters. 
    for j in ret[i][0]:
      if not check_GPS_char(j):
        print("Warning: " + i + " contains (automatically removed) longitude (GPS) incompatible character: " + j)
        ret[i][0] = ret[i][0].replace(j, '')
    if len(ret[i][0]) == 0:
      print("Warning: " + i + " contains no valid longitude, 0.0000000 substituted")
      ret[i][0] = "0.0000000"
    ret[i][1] = str(ret[i][1])
    for j in ret[i][1]:
      if not check_GPS_char(j):
        print("Warning: " + i + " contains (automatically removed) latitude (GPS) incompatible character: " + j)
        ret[i][1] = ret[i][1].replace(j, '')
    if len(ret[i][1]) == 0:
      print("Warning: " + i + " contains no valid latitude, 0.0000000 substituted")
      ret[i][1] = "0.0000000"
    #Check the longitude and latitude have special characters ('-' and '.') used correctly.
    ret[i] = GPS_special_chars(i, ret[i])
    #Check the longitude and latitude are now valid numbers.
    try:
        float(ret[i][0])
    except ValueError:
        print("Warning: " + i + " contains longitude that is not a number, 0.0000000 substituted")
        ret[i][0] = "0.0000000"
    try:
        float(ret[i][1])
    except ValueError:
        print("Warning: " + i + " contains latitude that is not a number, 0.0000000 substituted")
        ret[i][1] = "0.0000000"
    #Check the longitude and latitude are in the valid range (-180 to 180 for longitude, -90 to 90 for latitude).
    if (float(ret[i][0]) < -180.0) or (float(ret[i][0]) > 180.0):
      print("Warning: " + i + " contains longitude that is out of the valid -180 to 180 range, 0.0000000 substituted")
      ret[i][0] = "0.0000000"
    if (float(ret[i][1]) < -90.0) or (float(ret[i][1]) > 90.0):
      print("Warning: " + i + " contains latitude that is out of the valid -90 to 90 range, 0.0000000 substituted")
      ret[i][1] = "0.0000000"
  ret[i][0] = float(ret[i][0])
  ret[i][1] = float(ret[i][1])
  print("GPS check completed. If there was a 'Warning' in the output above then the 'automatic replacement / substitution' mentioned does not effect the knowledge graph, just the created JSON file. ")
  return ret


def get_all_powerplant_geodata(KGClient):
    '''
        Returns coordinates ([lon, lat]) and name (label) for all powerplants
    '''

# SPARQL query string
    query = kg.create_sparql_prefix('rdf') + \
            kg.create_sparql_prefix('rdfs') + \
            kg.create_sparql_prefix('ontoenergysystem') + \
            '''SELECT ?location ?name
                WHERE
                {
                    ?term rdf:type ontoenergysystem:PowerPlant ;
                        rdfs:label ?name ;
                        ontoenergysystem:hasWGS84LatitudeLongitude ?location.
                }'''

    # Execute query
    response = KGClient.execute(query)

    # Convert JSONArray String back to list
    response = json.loads(response)
    #print("Location Response: ", response)
    powerplant_coorindates = dict()
    for r in response:
        coordinates = r['location'].split('#')
        coordinates = [float(i) for i in coordinates]
        coordinates = coordinates[::-1]
        powerplant_coorindates[r['name'].lower()] = coordinates
    #Check GPS / GPS Check (check_GPS) here. 
    powerplant_coorindates = check_GPS_dict(powerplant_coorindates)
    return powerplant_coorindates

def get_all_powerplants(KGClient):
    '''
        Returns all powerplants instantiated in KG as list
    '''
    # Initialise SPARQL query variables for powerplant IRIs and names
    var1, var2 = 'iri', 'name'

    # Define query
    query = kg.create_sparql_prefix('ontoenergysystem') + \
            kg.create_sparql_prefix('rdf') + \
            kg.create_sparql_prefix('rdfs') + \
            'SELECT distinct ?' + var1 + ' ?' + var2 + ' ' \
            'WHERE { ?' + var1 + ' rdf:type ontoenergysystem:PowerPlant; \
                                   rdfs:label ?' + var2 + '. }'
    # Execute query
    response = KGClient.execute(query)
    # Convert JSONArray String back to list
    response = json.loads(response)
    #print("RESPONSE: ", response)
    # A key-value paired list where key is the name and
    # value is the IRI of powerplant
    powerplants = dict()
    for r in response:
        powerplants[r[var2]] = r[var1]
    return powerplants

def geojson_initialise_dict():
    # Start GeoJSON FeatureCollection
    geojson = {'type': 'FeatureCollection',
               'features': []
               }
    return geojson

def generate_all_visualisation_data():
    """
       Generates all data for Gas Grid visualisation.
    """
    # Set Mapbox API key in DTVF 'index.html' file
    kg.set_mapbox_apikey()

    # Initialise remote KG client with only query endpoint specified
    KGClient = jpsBaseLibView.RemoteStoreClient(kg.QUERY_ENDPOINT)

    # Retrieve Java's Instant class to initialise TimeSeriesClient
    Instant = jpsBaseLibView.java.time.Instant
    instant_class = Instant.now().getClass()
    # Initialise TimeSeriesClass
    TSClient = jpsBaseLibView.TimeSeriesClient(instant_class, kg.PROPERTIES_FILE)
    # Initialise timestamps for gas flow time series retrieval durations (time series entries stored as UTC times!)
    # Java Instant instances are associated with UTC (i.e. hold a value of date-time with a UTC time-line)
    """
    now = Instant.now()
    duration = 24
    print("INFO: Submitting TimeSeriesClient SPARQL queries at",
          dt.utcfromtimestamp(now.getEpochSecond()).strftime("%Y-%m-%dT%H:%M:%SZ"))
    start_1 = now.minusSeconds(int(1 * duration * 60 * 60))
    start_2 = now.minusSeconds(int(2 * duration * 60 * 60))
    start_7 = now.minusSeconds(int(7 * duration * 60 * 60))
    """

    # Initialise a dictionary for geoJSON outputs
    geojson = geojson_initialise_dict()
    # Initialise an array for metadata outputs
    metadata = []
    # Initialise an array with four elements for timeseries outputs
    ts_data = { 'ts': [],
                'id': [],
                'units': [],
                'headers': []
                }
    feature_id = 0

    # Get all powerplants of interest
    powerplants = get_all_powerplants(KGClient)
    # Retrieve all geocoordinates of powerplants for GeoJSON output
    powerplant_coordinates = get_all_powerplant_geodata(KGClient)
    
    # Iterate over all powerplants
    for powerplant, iri in powerplants.items():
        feature_id += 1
        # Update GeoJSON properties
        geojson_attributes['description'] = str(powerplant)
        geojson_attributes['displayName'] = powerplant
        # Append results to overall GeoJSON FeatureCollection
        if powerplant.lower() in powerplant_coordinates:
            #print('powerplant_coordinates[powerplant.lower()]:', powerplant_coordinates[powerplant.lower()])
            geojson['features'].append(format_in_geojson(feature_id, geojson_attributes, powerplant_coordinates[powerplant.lower()]))
        # Retrieve powerplant metadata
        lon, lat = get_metadata(iri, KGClient)
        if lon == None and lat == None:
            print('The following powerplant is not represented in the knowledge graph with coordinates:', iri)
        else:
            metadata.append(put_metadata_in_json(feature_id, lon, lat))
        # Retrieve time series data
        # timeseries, utilities, units = get_all_time_series(iri, KGClient, TSClient, now, duration, start_1, start_2, start_7)
        # ts_data['ts'].append(timeseries)
        # ts_data['id'].append(feature_id)
        # ts_data['units'].append(units)
        # ts_data['headers'].append(utilities)
    # 
    # # Retrieve all time series data for collected 'ts_data' from Java TimeSeriesClient at once
    # ts_json = TSClient.convertToJSON(ts_data['ts'], ts_data['id'], ts_data['units'], ts_data['headers'])
    # # Make JSON file readable in Python
    # ts_json = json.loads(ts_json.toString())
    # Write GeoJSON dictionary formatted to file
    file_name = os.path.join(kg.OUTPUT_DIR, 'data/powerplants', 'powerplants.geojson')
    with open(file_name, 'w') as f:
        json.dump(geojson, indent=4, fp=f)
    file_name = os.path.join(kg.OUTPUT_DIR, 'data/powerplants', 'powerplants-meta.json')
    with open(file_name, 'w') as f:
        json.dump(metadata, indent=4, fp=f)
    # file_name = os.path.join(kg.OUTPUT_DIR, 'data/set-1/scenario-0', 'powerplants-timeseries.json')
    # with open(file_name, 'w') as f:
    #     json.dump(ts_json, indent=4, fp=f)


if __name__ == '__main__':
    """
    If this module is executed, it will generate all data required for the
    Digital Twin Visualisation Framework.
    """
    generate_all_visualisation_data()
