################################################
# Authors: Jieyang Xu (jx309@cam.ac.uk) #
# Date: 30/11 2022                            #
################################################

# The purpose of this module is to provide templates for
# required SPARQL queries

from agent.datamodel import *
from agent.kgutils.stackclients import PostGISClient


def input_query_template(
    mes_uuid,
    used_uuid,
    start_time,
    end_time,
    region,
    kw_uuid,
    cons,
    met_uuid,
    meters,
    non_meters: int = None,
) -> str:

    # Add prefix to strings
    mes_uuid = COMPA + mes_uuid
    used_uuid = COMPA + used_uuid
    kw_uuid = COMPA + kw_uuid
    met_uuid = COMPA + met_uuid

    region = ONS_ID + region

    triples = f""" <{mes_uuid}> <{RDF_TYPE}> <{OM_MEASURE}>.
                <{mes_uuid}> <{OM_HAS_UNIT}> <{OM_KW}>.
                <{used_uuid}> <{RDF_TYPE}> <{COMPA_ELEC}>;
                        <{COMPA_HAS_STARTUTC}>  "{start_time}"^^<{XSD_DATETIME}> ;
                        <{COMPA_HAS_ENDUTC}>  "{end_time}"^^<{XSD_DATETIME}>  .
                
                <{region}> <{RDF_TYPE}> <{ONS_DEF_STAT}>.
                <{region}> <{COMPA_HAS_CONSUMED}> <{used_uuid}> .

                <{kw_uuid}> <{RDF_TYPE}> <{OM}>;
                        <{OM_HAS_PHENO}> <{used_uuid}> ;
                        <{OM_HAS_VALUE}> <{mes_uuid}>.

                <{mes_uuid}> <{OM_HAS_NUMERICALVALUE}> <{cons}>.
                <{met_uuid}> <{RDF_TYPE}> <{GAS_ELEC}>.
                <{region}> <{GAS_HAS_ELECMETERS}> <{met_uuid}>.
                <{met_uuid}> <{GAS_HAS_CONSUM_ELECMETERS}> <{meters}>;
                        <{COMPA_HAS_STARTUTC}> "{start_time}"^^<{XSD_DATETIME}> ;
                        <{COMPA_HAS_ENDUTC}>  "{end_time}"^^<{XSD_DATETIME}> . 
                """

    return triples


'''
def add_om_quantity(
    station_iri,
    quantity_iri,
    quantity_type,
    data_iri,
    data_iri_type,
    unit,
    symbol,
    is_observation: bool,
    creation_time=None,
    comment=None,
):
    """
    Create triples to instantiate station measurements
    """
    # Create triple for measure vs. forecast
    if is_observation:
        triple = f"""<{quantity_iri}> <{OM_HAS_VALUE}> <{data_iri}> . """

    else:
        triple = f"<{quantity_iri}> <{EMS_HAS_FORECASTED_VALUE}> <{data_iri}> . "
        if creation_time:
            triple += f'<{data_iri}> <{EMS_CREATED_ON}> "{creation_time}"^^<{XSD_DATETIME}> . '

    # Create triples to instantiate station measurement according to OntoEMS
    triples = f"""
        <{station_iri}> <{EMS_REPORTS}> <{quantity_iri}> . 
        <{quantity_iri}> <{RDF_TYPE}> <{quantity_type}> . 
        <{data_iri}> <{RDF_TYPE}> <{data_iri_type}> . 
        <{data_iri}> <{OM_HAS_UNIT}> <{unit}> . 
        <{unit}> <{RDF_TYPE}> <{OM_UNIT}> . 
        <{unit}> <{OM_SYMBOL}> "{symbol}"^^<{XSD_STRING}> . 
    """
    triples += triple

    # Create optional comment to quantity
    if comment:
        triples += (
            f"""<{quantity_iri}> <{RDFS_COMMENT}> "{comment}"^^<{XSD_STRING}> . """
        )

    return triples


def instantiated_observations(station_iris: list = None):
    # Returns query to retrieve (all) instantiated observation types per station
    if station_iris:
        iris = ", ".join(["<" + iri + ">" for iri in station_iris])
        substring = f"""FILTER (?station IN ({iris}) ) """
    else:
        substring = f"""
            ?station <{RDF_TYPE}> <{EMS_REPORTING_STATION}> ;
                     <{EMS_DATA_SOURCE}> "Met Office DataPoint" . """
    query = f"""
        SELECT ?station ?stationID ?quantityType
        WHERE {{
            ?station <{EMS_HAS_IDENTIFIER}> ?stationID ;
                     <{EMS_REPORTS}> ?quantity .
            {substring}                     
            ?quantity <{OM_HAS_VALUE}> ?measure ;
                      <{RDF_TYPE}> ?quantityType .
        }}
        ORDER BY ?station
    """
    return query


def instantiated_forecasts(station_iris: list = None):
    # Returns query to retrieve (all) instantiated forecast types per station
    if station_iris:
        iris = ", ".join(["<" + iri + ">" for iri in station_iris])
        substring = f"""FILTER (?station IN ({iris}) ) """
    else:
        substring = f"""
            ?station <{RDF_TYPE}> <{EMS_REPORTING_STATION}> ;
                     <{EMS_DATA_SOURCE}> "Met Office DataPoint" . """
    query = f"""
        SELECT ?station ?stationID ?quantityType
        WHERE {{
            ?station <{EMS_HAS_IDENTIFIER}> ?stationID ;
                     <{EMS_REPORTS}> ?quantity .
            {substring} 
            ?quantity <{EMS_HAS_FORECASTED_VALUE}> ?forecast ;
                      <{RDF_TYPE}> ?quantityType .
        }}
        ORDER BY ?station
    """
    return query


def instantiated_observation_timeseries(station_iris: list = None):
    # Returns query to retrieve (all) instantiated observation time series per station
    if station_iris:
        iris = ", ".join(["<" + iri + ">" for iri in station_iris])
        substring = f"""FILTER (?station IN ({iris}) ) """
    else:
        substring = f"""
            ?station <{RDF_TYPE}> <{EMS_REPORTING_STATION}> ;
                     <{EMS_DATA_SOURCE}> "Met Office DataPoint" . """
    query = f"""
        SELECT ?station ?stationID ?quantityType ?dataIRI ?unit ?tsIRI 
        WHERE {{
            ?station <{EMS_HAS_IDENTIFIER}> ?stationID ;
                     <{EMS_REPORTS}> ?quantity .
            {substring} 
            ?quantity <{OM_HAS_VALUE}> ?dataIRI ;
                      <{RDF_TYPE}> ?quantityType .
            ?dataIRI <{TS_HAS_TIMESERIES}> ?tsIRI ;   
                     <{OM_HAS_UNIT}>/<{OM_SYMBOL}> ?unit .
            ?tsIRI <{RDF_TYPE}> <{TS_TIMESERIES}> .
        }}
        ORDER BY ?tsIRI
    """
    return query


def instantiated_forecast_timeseries(station_iris: list = None):
    # Returns query to retrieve (all) instantiated forecast time series per station
    if station_iris:
        iris = ", ".join(["<" + iri + ">" for iri in station_iris])
        substring = f"""FILTER (?station IN ({iris}) ) """
    else:
        substring = f"""
            ?station <{RDF_TYPE}> <{EMS_REPORTING_STATION}> ;
                     <{EMS_DATA_SOURCE}> "Met Office DataPoint" . """
    query = f"""
        SELECT ?station ?stationID ?quantityType ?dataIRI ?unit ?tsIRI
        WHERE {{
            ?station <{EMS_HAS_IDENTIFIER}> ?stationID ;
                     <{EMS_REPORTS}> ?quantity .
            {substring} 
            ?quantity <{EMS_HAS_FORECASTED_VALUE}> ?dataIRI ;
                      <{RDF_TYPE}> ?quantityType .
            ?dataIRI <{TS_HAS_TIMESERIES}> ?tsIRI ;
                     <{OM_HAS_UNIT}>/<{OM_SYMBOL}> ?unit .
            ?tsIRI <{RDF_TYPE}> <{TS_TIMESERIES}> .
        }}
        ORDER BY ?tsIRI
    """
    return query


def split_insert_query(triples: str, max: int):
    """ "
    Split large SPARQL insert query into list of smaller chunks (primarily
    to avoid heap size/memory issues when executing large SPARQL updated)

    Arguments
        triples - original SPARQL update string with individual triples
                  separated by " . ", i.e. in the form
                  <s1> <p1> <o1> .
                  <s2> <p2> <o2> .
                  ...
        max - maximum number of triples per SPARQL update

    """

    # Initialise list of return queries
    queries = []

    # Split original query every max'th occurrence of " . " and append queries list
    splits = triples.split(" . ")
    cutoffs = list(range(0, len(splits), max))
    cutoffs.append(len(splits))
    for i in range(len(cutoffs) - 1):
        start = cutoffs[i]
        end = cutoffs[i + 1]
        query = " . ".join([t for t in splits[start:end]])
        query = " INSERT DATA { " + query + " } "
        queries.append(query)

    return queries


def update_forecast_creation_datetime(issue_time: str):
    # Returns beginning of update query to update forecast creation times
    query = f"""
        DELETE {{
	        ?forecast <{EMS_CREATED_ON}> ?old }}
        INSERT {{
	        ?forecast <{EMS_CREATED_ON}> \"{issue_time}\"^^<{XSD_DATETIME}> }}
        WHERE {{
	        ?forecast <{EMS_CREATED_ON}> ?old .
            FILTER ( ?forecast IN (
    """

    return query
'''
