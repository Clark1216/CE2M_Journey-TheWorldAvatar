################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 25 Jul 2023                            #
################################################

# The purpose of this module is to provide functionality to execute
# KG queries and updates using the StoreRouter from the JPS_BASE_LIB

import uuid
import pandas as pd

from py4jps import agentlogging

from pyderivationagent.kg_operations import PySparqlClient

from forecastingagent.datamodel.iris import *
from forecastingagent.kgutils.utils import *


# Initialise logger instance (ensure consistent logger level`)
logger = agentlogging.get_logger('prod')


class KGClient(PySparqlClient):
    
    #
    # SPARQL QUERIES
    #
    def get_time_series_details(self, iri_to_forecast:str):
        """
        Returns the dataIRI, tsIRI, RDB URL and time format of the time series 
        instance associated with the given instance IRI.
        NOTE: iri_to_forecast and dataIRI do not need to be equivalent, especially
              when OM representation for data is used, where a om:Measure concept 
              is used "in between":
              <iri_to_forecast> ts:hasForecast <...> ;
                                om:hasValue <dataIRI> .
              <dataIRI> a om:Measure ;
                        ts:hasTimeseries <tsIRI> .
              <tsIRI> ts:hasRDB <...> ;
                      ts:hasTimeUnit <...> .

        Arguments:
            iri_to_forecast (str) -- IRI of instance for which to create forecast
        Returns:
            ts (dict) -- dictionary with keys 'data_iri', 'ts_iri', 'fc_iri',
                         'rdb_url' and 'time_format'
        """
        query = f"""
            SELECT DISTINCT ?data_iri ?ts_iri ?fc_iri ?unit ?rdb_url ?time_format
            WHERE {{   
            VALUES ?iri {{ <{iri_to_forecast}> }} 
            ?iri <{OM_HASVALUE}>*/<{TS_HASTIMESERIES}> ?ts_iri .
            ?ts_iri ^<{TS_HASTIMESERIES}> ?data_iri ;
                     <{TS_HASRDB}> ?rdb_url .
            OPTIONAL {{ ?data_iri <{OM_HASUNIT}> ?unit . }}
            OPTIONAL {{ ?ts_iri <{TS_HASTIMEUNIT}> ?time_format . }}
            OPTIONAL {{ ?iri <{TS_HASFORECAST}> ?fc_iri . }}
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Extract relevant information from unique query result
        if len(res) == 1:
            ts = {'data_iri': get_unique_value(res, 'data_iri'),
                  'ts_iri': get_unique_value(res, 'ts_iri'),
                  'fc_iri': get_unique_value(res, 'fc_iri'),
                  'unit': get_unique_value(res, 'unit'),
                  'rdb_url': get_unique_value(res, 'rdb_url'),
                  'time_format': get_unique_value(res, 'time_format')
            }
            return ts
        
        else:
            # Throw exception if no or multiple time series are found
            if len(res) == 0:
                msg = f"No time series associated with data IRI: {iri_to_forecast}."
            else:
                msg = f"Multiple time series associated with data IRI: {iri_to_forecast}."
            logger.error(msg)
            raise ValueError(msg)
        

    def get_fcmodel_details(self, fcmodelIRI:str):
        """
        Returns relevant forecasting model details for given forecasting model IRI.

        Returns:
            fcmodel (dict) -- dictionary with keys 'fcmodel_iri', 'label', 'scale_data',
                              'model_url', 'chkpt_url' and 'covariate_iris'
        """
        query = f"""
            SELECT DISTINCT ?fcmodel_iri ?label ?scale_data ?model_url ?chkpt_url ?covariate_iri
            WHERE {{   
            VALUES ?fcmodel_iri {{ <{fcmodelIRI}> }} 
            ?fcmodel_iri <{RDFS_LABEL}> ?label .
            OPTIONAL {{ ?fcmodel_iri <{TS_SCALE_DATA}> ?scale_data . }}
            OPTIONAL {{ ?fcmodel_iri <{TS_HAS_MODEL_URL}> ?model_url ;
                                     <{TS_HAS_CHKPT_URL}> ?chkpt_url . }}
            OPTIONAL {{ ?fcmodel_iri <{TS_HASCOVARIATE}> ?covariate_iri . }}
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Extract relevant information from unique query result
        if len(res) >= 1:
            fcmodel = {'fcmodel_iri': get_unique_value(res, 'fcmodel_iri'),
                       'label': get_unique_value(res, 'label'),
                       'scale_data': get_unique_value(res, 'scale_data', bool),
                       'model_url': get_unique_value(res, 'model_url'),
                       'chkpt_url': get_unique_value(res, 'chkpt_url'),
                       #TODO: covariates should become dictionary with type and IRI
                       'covariate_iris': get_list_of_unique_values(res, 'covariate_iri')
            }
            return fcmodel
        
        else:
            msg = "No forecasting model details could be retrieved from KG."
            logger.error(msg)
            raise ValueError(msg)
    

    def get_duration_details(self, durationIRI:str):
        """
        Returns relevant duration details (i.e., time value and unit) for given 
        duration or frequency (i.e., a subclass of time:duration) IRI.

        Returns:
            duration (dict) -- dictionary with keys 'iri', 'unit', 'value', 
                               and 'resample_data' (only relevant for frequency)
        """
        query = f"""
            SELECT DISTINCT ?iri ?resample_data ?unit ?value
            WHERE {{   
            VALUES ?iri {{ <{durationIRI}> }} 
            ?iri <{TIME_UNIT_TYPE}> ?unit ;
                 <{TIME_NUMERICDURATION}> ?value .
            OPTIONAL {{ ?iri <{TS_RESAMPLE_DATA}> ?resample_data . }}
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Extract relevant information from unique query result
        if len(res) == 1:
            duration = {'iri': get_unique_value(res, 'iri'),
                        'unit': get_unique_value(res, 'unit'),
                        'value': get_unique_value(res, 'value', float),
                        'resample_data': get_unique_value(res, 'resample_data', bool)
            }
            return duration
        
        else:
            msg = "No unique duration/frequency details could be retrieved from KG."
            logger.error(msg)
            raise ValueError(msg)
        

    def get_interval_details(self, intervalIRI:str):
        """
        Returns relevant time interval (i.e. forecast horizon) details for 
        given time interval IRI.

        Returns:
            interval (dict) -- dictionary with keys 'interval_iri', 'start_iri',
                               'end_iri', 'start_unix' and 'end_unix'
        """

        def _get_instant_details(var_instant, var_timepos):
            query = f"""
                VALUES ?trs {{ <{UNIX_TIME}> }} 
                ?{var_instant} <{TIME_INTIMEPOSITION}>/<{TIME_HASTRS}> ?trs ;
                            <{TIME_INTIMEPOSITION}>/<{TIME_NUMERICPOSITION}> ?{var_timepos} . 
            """
            return query

        query = f"""
            SELECT DISTINCT ?interval_iri ?start_iri ?end_iri ?start_unix ?end_unix
            WHERE {{
            VALUES ?interval_iri {{ <{intervalIRI}> }} 
            ?interval_iri <{TIME_HASBEGINNING}> ?start_iri ;
                          <{TIME_HASEND}> ?end_iri .
            {_get_instant_details('start_iri', 'start_unix')} 
            {_get_instant_details('end_iri', 'end_unix')}        
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Extract relevant information from unique query result
        if len(res) == 1:
            interval = {'interval_iri': get_unique_value(res, 'interval_iri'),
                        'start_iri': get_unique_value(res, 'start_iri'),
                        'end_iri': get_unique_value(res, 'end_iri'),
                        'start_unix': get_unique_value(res, 'start_unix', int),
                        'end_unix': get_unique_value(res, 'end_unix', int),
            }
            # Check validity of retrieved interval
            if interval['start_unix'] >= interval['end_unix']:
                msg = "Interval start time is not before end time."
                logger.error(msg)
                raise ValueError(msg)

            return interval
        
        else:
            msg = "No unique interval details could be retrieved from KG."
            logger.error(msg)
            raise ValueError(msg)


    def get_dataIRI(self, tsIRI:str):
        """
        Returns dataIRI associated with given tsIRI.

        Returns:
            dataIRI {str} -- dataIRI associated with given tsIRI
        """
        query = f"""
            SELECT DISTINCT ?dataIRI
            WHERE {{   
            ?dataIRI <{TS_HASTIMESERIES}> <{tsIRI}> .
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Return unique query result (otherwise exception is thrown)
        return get_unique_value(res, 'dataIRI')


    def get_all_tsIRIs(self):
        """
        Returns list of all instantiated time series IRIs.
        """
        query = f"""
            SELECT DISTINCT ?tsIRI
            WHERE {{   
            ?tsIRI <{RDF_TYPE}> <{TS_TIMESERIES}> .
            }}
        """
        query = remove_unnecessary_whitespace(query)
        res = self.performQuery(query)

        # Return unique query result (otherwise exception is thrown)
        return get_list_of_unique_values(res, 'tsIRI')


    #
    # SPARQL UPDATES
    # 
    def instantiate_forecast(self, forecast, config:dict):
        """
        Takes a model configuration and returns the forecast SPARQL update query to instantiate the forecast in the KG

        Arguments:
            forecast {darts.TimeSeries} - forecast time series for which to instantiate triples
            config: a dictionary with meta information about the forecast
                'fc_model': the configuration of the forecasting model
                'iri_to_forecast': the iri of the instance to be forecasted
                'fc_iri': the iri of the forecast instance
                'unit': the unit of the (original) time series data
        """
        
        # Create forecast input and output intervals
        inp_start = forecast.start_time() - forecast.freq * \
                                          (config['fc_model']['input_length'])
        inp_end = forecast.start_time() - forecast.freq
        config['model_input_interval'] = [inp_start, inp_end]
        config['model_output_interval'] = [forecast.start_time(), forecast.end_time()]

        # Create new forecast iri if not exists
        if not config.get('fc_iri'):
            config['fc_iri'] = KB + 'Forecast_' + str(uuid.uuid4())
            update = self.instantiate_new_forecast(config)
        else:
            update = self.update_existing_forecast(config)

        update = remove_unnecessary_whitespace(update)
        self.performUpdate(update)

        return config.get('fc_iri')


    def instantiate_new_forecast(self, cfg:dict):
        """
        Returns SPARQL INSERT DATA query to instantiate a new forecast
        """
        
        # Create IRIs for new instances
        outputTimeInterval_iri = KB + 'Interval_' + str(uuid.uuid4())
        inputTimeInterval_iri = KB + 'Interval_' + str(uuid.uuid4())

        # Initialise SPARQL update body witth forecast related triples
        body = ''
        body += create_properties_for_subj(subj=cfg['iri_to_forecast'], pred_obj={
                    TS_HASFORECAST: cfg['fc_iri']})
        body += create_properties_for_subj(subj=cfg['iri_to_forecast'], pred_obj={
                    TS_HASFORECASTINGMODEL: cfg['fc_model']['model_iri']})
        # Add unit to forecast if one could be retrieved from original ts
        unit = {OM_HASUNIT: cfg['unit']} if 'unit' in cfg else {}
        body += create_properties_for_subj(subj=cfg['fc_iri'], pred_obj={
                    RDF_TYPE: TS_FORECAST,
                    **unit,
                    TS_HASOUTPUTTIMEINTERVAL: outputTimeInterval_iri,
                    TS_HASINPUTTIMEINTERVAL: inputTimeInterval_iri})

        # Add triples for time intervals/instants
        inputBeginning_iri, q = create_time_instant(cfg['model_input_interval'][0])
        body += q
        inputEnd_iri, q = create_time_instant(cfg['model_input_interval'][1])
        body += q
        outputBeginning_iri, q = create_time_instant(cfg['model_output_interval'][0])
        body += q
        outputEnd_iri, q = create_time_instant(cfg['model_output_interval'][1])
        body += q
        body += create_properties_for_subj(subj=inputTimeInterval_iri, pred_obj={
                    RDF_TYPE: TIME_INTERVAL,
                    TIME_HASBEGINNING: inputBeginning_iri,
                    TIME_HASEND: inputEnd_iri})
        body += create_properties_for_subj(subj=outputTimeInterval_iri, pred_obj={
                    RDF_TYPE: TIME_INTERVAL,
                    TIME_HASBEGINNING: outputBeginning_iri,
                    TIME_HASEND: outputEnd_iri})

        update = f'INSERT DATA {{ {body} }} '
        return update


    def update_existing_forecast(self, cfg:dict):
        """
        Returns SPARQL UPDATE query to update an existing forecast
        """
        
        # Get UNIX timestamp (in s) of interval start/end times
        inp_start = convert_time_to_timestamp(cfg['model_input_interval'][0])
        inp_end = convert_time_to_timestamp(cfg['model_input_interval'][1])
        out_start = convert_time_to_timestamp(cfg['model_output_interval'][0])
        out_end = convert_time_to_timestamp(cfg['model_output_interval'][1])

        # Only delete/update old unit if new one is available
        if cfg.get('unit'):
            unit_insert = "?fc_iri <{OM_HASUNIT}> <{cfg['unit']}> . "
            unit_delete = "?fc_iri <{OM_HASUNIT}> ?unit . "
        else:
            unit_insert = ""
            unit_delete = ""

        # Create Delete-Insert query
        update = f"""
            DELETE {{
                {unit_delete}
                ?start1 <{TIME_NUMERICPOSITION}> ?t1 .
                ?end1 <{TIME_NUMERICPOSITION}> ?t2 .
                ?start2 <{TIME_NUMERICPOSITION}> ?t3 .
                ?end2 <{TIME_NUMERICPOSITION}> ?t4 .
            }}
            INSERT {{
                {unit_insert}
                ?start1 <{TIME_NUMERICPOSITION}> "{inp_start}"^^<{XSD_DECIMAL}> .
                ?end1 <{TIME_NUMERICPOSITION}> "{inp_end}"^^<{XSD_DECIMAL}> .
                ?start2 <{TIME_NUMERICPOSITION}> "{out_start}"^^<{XSD_DECIMAL}> .
                ?end2 <{TIME_NUMERICPOSITION}> "{out_end}"^^<{XSD_DECIMAL}> .
            }} 
            WHERE {{
                VALUES ?fc_iri {{ <{cfg['fc_iri']}> }}
                OPTIONAL {{ ?fc_iri <{OM_HASUNIT}> ?unit }}
                ?fc_iri <{TS_HASINPUTTIMEINTERVAL}> ?int1 ;
                        <{TS_HASOUTPUTTIMEINTERVAL}> ?int2 .
                ?int1 <{TIME_HASBEGINNING}>/<{TIME_INTIMEPOSITION}> ?start1 ;
                      <{TIME_HASEND}>/<{TIME_INTIMEPOSITION}> ?end1 .
                ?int2 <{TIME_HASBEGINNING}>/<{TIME_INTIMEPOSITION}> ?start2 ;
                      <{TIME_HASEND}>/<{TIME_INTIMEPOSITION}> ?end2 .
                ?start1 <{TIME_NUMERICPOSITION}> ?t1 .
                ?end1 <{TIME_NUMERICPOSITION}> ?t2 .
                ?start2 <{TIME_NUMERICPOSITION}> ?t3 .
                ?end2 <{TIME_NUMERICPOSITION}> ?t4 .
            }}
        """

        return update