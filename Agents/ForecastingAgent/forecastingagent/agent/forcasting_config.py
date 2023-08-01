################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) # 
#          Magnus Mueller (mm2692@cam.ac.uk)   #
# Date: 01 Aug 2023                            #
################################################

# The purpose of this module is to create a model configuration dictionary for 
# the forecasting agent depending on the retrieved information from the KG

""" 

'load_covariates_func': The function which is used to load the covariates. If not provided, no covariates are loaded.
    Be aware, that the returned covariates must be available for the whole 'horizon'. 
    If the covariates are not long enough, Prophet is used, which does not require covariates.
    The function must return parameters:
    'covariates_iris': A list if iris, which are used.
    'covariates': A darts series object, which can be passed into model.predict()
    
    The function will receive the following parameters:
    'kgClient': The kgClient
    'tsClient': The tsClient
    'lowerbound': The lower bound of the time series data (can be None)
    'upperbound': The upper bound of the time series data (can be None)
    
"""

import datetime as dt

from py4jps import agentlogging

from forecastingagent.datamodel.iris import *
from forecastingagent.utils.baselib_gateway import jpsBaseLibGW
#from forecastingagent.utils.tools import get_covs_heat_supply


# Initialise logger instance (ensure consistent logger level`)
logger = agentlogging.get_logger('prod')


#
# Define some default values
#
# Time series value data types (default: Double)
jpsBaseLibView = jpsBaseLibGW.createModuleView()
DOUBLE = jpsBaseLibView.java.lang.Double.TYPE
INTEGER = jpsBaseLibView.java.lang.Integer.TYPE
BOOLEAN = jpsBaseLibView.java.lang.Boolean.TYPE
# Time series format
TIME_FORMAT = "%Y-%m-%dT%H:%M:%SZ"


def create_forecast_configuration(model:dict, frequency:dict, 
                                  duration:dict, ts_data_type=DOUBLE):
    """
    Returns a dictionary of parameters describing the forecast to create.

    Arguments:
        fcmodel {dict} -- forecast model details as retrieved from the KG
        frequency {dict} -- time series frequency details as retrieved
        duration {dict} -- historical data duration details as retrieved

    Returns a dictionary with a forecast configuration as follows:
    Required (always present):
        'ts_data_type': Java data type for time series values
        'frequency': The frequency of the time series data, as a datetime timedelta object 
                    (i.e. representing a duration of time)
        'data_length': The maximum length of the time series data, which is loaded before
                    the forecast_start_data.
                    The loaded data is used to:
                        1. Train the model if `train_again` is True or
                        2. Scale the data if `scale_data` is True
                        3. Create the forecast as Input to the model. 
                        Neural Methods have a fixed input length (same as during training),
                        therefore they use the last values of the time series data with
                        respect to their input length to create the forecast.
        'fc_model': The forecasting model which is used to create the forecast.
            It is a dict with the following keys:
            Required:
                'name': The name of the forecasting model ('prophet' as default)
                'scale_data': If True, the data is scaled before the forecast is created
                'train_again': If True, the model is trained again before the forecast is created
            Only required for pre-trained models, i.e. models other than Prophet:
                If 'model_path_ckpt_link' and 'model_path_pth_link' are give, the model 
                is loaded from the given paths
                'model_path_ckpt_link': The link to the darts checkpoint file of the model
                'model_path_pth_link': The link to the darts pth file of the model
            
    Optional:
        'resample_data': String specifying the resampling frequency for irregularly spaced time series
                         (for supported options see: 'resample_data': pandas.DataFrame.resample)
                         (this key is optional and helps to avoid issues with Darts TimeSeries handling
                         with irregular frequencies)
    """

    # Initialise forecast cfg with ts data type and model details
    cfg = {
        'ts_data_type': ts_data_type,
        'fc_model': create_model_dict(model)
    }
    
    # Add time series frequency details
    f = create_duration_entries(frequency)
    cfg['frequency'] = f.pop('duration')
    cfg.update(f)
    
    # Add historical data length
    dur = create_duration_entries(duration)
    # Get number of historical time steps to consider by dividing duration
    # of historical data to use by frequency of time series
    cfg['data_length'] = int(dur['duration'] / cfg['frequency'])

    return cfg


def create_model_dict(model:dict):
    """
    Create forecast model node of overarching forecasting configuration dictionary.

    Arguments:
        model {dict} -- forecast model details as retrieved from the KG
    """

    # Initialise config with forecast model name
    model_dict = {
        'name': model['label'].lower()
    }

    # Prophet is the default model
    if model_dict['name'] == 'prophet':
        # Use default settings (if not provided otherwise)
        model_dict['train_again'] = True
        model_dict['scale_data'] = False if model['scale_data'] is None else \
                                   model['scale_data']
    else:
        # Otherwise use pre-trained model as specified in the KG
        model_dict['train_again'] = False
        model_dict['scale_data'] = model['scale_data']
        model_dict['model_path_pth_link'] = model['model_url']
        model_dict['model_path_ckpt_link'] = model['chkpt_url']
        model_dict['covariate_iris'] = model['covariate_iris']

    # Throw exception if any required parameter is missing (None)
    if any(value is None for value in model_dict.values()):
        msg = 'Forecast model configuration is missing required parameters.'
        logger.error(msg)
        raise ValueError(msg)

    return model_dict


def create_duration_entries(freq:dict):
    """
    Create duration/frequency related entries of overarching forecasting 
    configuration dictionary.

    Arguments:
        freq {dict} -- duration/frequency details as retrieved from the KG
    """

    # Map units between time ontology and equivalents in pandas and datetime
    time_unit_mapping = {
        TIME_UNIT_DAY: ('D', 24*60*60),
        TIME_UNIT_HOUR: ('H', 60*60),
        TIME_UNIT_MINUTE: ('T', 60),
        TIME_UNIT_SECOND: ('S', 1),
    }

    unit = time_unit_mapping.get(freq['unit'])
    if not unit:
        msg = 'Retrieved time unit not supported by mapping dictionary.'
        logger.error(msg)
        raise ValueError(msg)
    else:
        freq_dict = {}
        # Create resampling string for pandas resample method
        if freq['resample_data']:
            freq_dict['resample_data'] = f"{int(freq['value'])}{unit[0]}"
        # Create timedelta object
        sec = unit[1] * int(freq['value'])
        freq_dict['duration'] = dt.timedelta(seconds=sec)

    return freq_dict
