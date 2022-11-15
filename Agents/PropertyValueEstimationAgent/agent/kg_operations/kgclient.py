################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 21 Oct 2022                            #
################################################

# The purpose of this module is to provide functionality to execute KG queries
# and updates using the PySparqlClient module from pyderivationagent

import uuid
import datetime as dt

from py4jps import agentlogging
from pyderivationagent.kg_operations import PySparqlClient

from agent.datamodel.iris import *
from agent.datamodel.data import GBP, TIME_FORMAT_LONG, TIME_FORMAT_SHORT

# Initialise logger instance (ensure consistent logger level with `entrypoint.py`)
logger = agentlogging.get_logger('prod')


class KGClient(PySparqlClient):
    #
    # SPARQL QUERIES
    #
    def get_transaction_details(self, tx_iri:str) -> dict:
        # Retrieve transaction details for given transaction IRI
        # Returns dictionary with keys: price, date, property_iri

        query = f"""
            SELECT ?price ?date ?property_iri
            WHERE {{   
            <{tx_iri}> <{LRPPI_DATE}> ?date ; 
                       <{LRPPI_PRICE}> ?price ; 
                       ^<{OBE_HAS_LATEST_TRANSACTION}> ?property_iri . 
            }}
        """
        query = self.remove_unnecessary_whitespace(query)
        res = self.performQuery(query)
        if not res:
            # In case date or price (or both) are missing (i.e. empty SPARQL result), return Nones
            res = dict(zip(['price', 'date', 'property_iri'], (None,)*3))
        else:
            # Cast price to float
            try:
                res['price'] = float(res['price'])
            except:
                res['price'] = None
            # Extract relevant date (YYYY-MM) from string
            try:
                dt.datetime.strptime(res['date'], TIME_FORMAT_LONG).strftime(TIME_FORMAT_SHORT)
            except:
                res['date'] = None
        return res


    def get_floor_area_and_avg_price(self, floor_area_iri:str) -> dict:
        # Retrieve total floor area and property price index (representative for 
        # associated property) of given floor area IRI
        # Returns dictionary with keys: floor_area, avg_price, property_iri

        query = f"""
            SELECT ?floor_area ?avg_price ?property_iri
            WHERE {{   
            <{floor_area_iri}> <{OM_HAS_VALUE}>/<{OM_NUM_VALUE}> ?floor_area ; 
                               ^<{OBE_HAS_TOTAL_FLOOR_AREA}> ?property_iri . 
            ?property_iri <{OBE_HAS_ADDRESS}>/<{OBE_HAS_POSTALCODE}> ?postcode_iri . 
            ?avg_price_iri <{OBE_REPRESENTATIVE_FOR}> ?postcode_iri ; 
                           <{OM_HAS_VALUE}>/<{OM_NUM_VALUE}> ?avg_price . 
            }}
        """
        query = self.remove_unnecessary_whitespace(query)
        res = self.performQuery(query)
        if not res:
            # In case floor area or average price (or both) are missing (i.e. empty SPARQL result), return Nones
            res = dict(zip(['floor_area', 'avg_price', 'property_iri'], (None,)*3))
        else:
            # Cast floor area and average price to float
            for key in ['floor_area', 'avg_price']:
                try:
                    res[key] = float(res[key])
                except:
                    res[key] = None
        return res


    def instantiate_property_value(self, property_iri, property_value_iri, property_value) -> str:
        # Returns INSERT DATA query to instantiate/update property market value estimation
        # Create unique IRIs for new instances
        measure_iri = KB + 'Measure_' + str(uuid.uuid4())
        
        query = f"""
            <{property_iri}> <{OBE_HAS_MARKET_VALUE}> <{property_value_iri}> . 
            <{property_value_iri}> <{RDF_TYPE}> <{OM_AMOUNT_MONEY}> . 
            <{property_value_iri}> <{OM_HAS_VALUE}> <{measure_iri}> . 
            <{measure_iri}> <{RDF_TYPE}> <{OM_MEASURE}> . 
            <{measure_iri}> <{OM_NUM_VALUE}> \"{property_value}\"^^<{XSD_INTEGER}> . 
            <{measure_iri}> <{OM_HAS_UNIT}> <{OM_GBP}> . 
            <{OM_GBP}> <{OM_SYMBOL}> \"{GBP}\"^^<{XSD_STRING}> . 
        """
        #TODO: Triple with symbol potentially to be removed once OntoUOM contains
        #      all relevant units/symbols and is uploaded to the KB
        return self.remove_unnecessary_whitespace(query)


    def remove_unnecessary_whitespace(self, query: str) -> str:
        # Remove unnecessary whitespaces
        query = ' '.join(query.split())

        return query
