################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #
# Date: 17 Oct 2022                            #
################################################

# The purpose of this module is to provide functionality to use
# the TimeSeriesClient from the JPS_BASE_LIB

from contextlib import contextmanager

from py4jps import agentlogging

from forecastingagent.errorhandling.exceptions import TSException
from forecastingagent.utils.baselib_gateway import jpsBaseLibGW
from forecastingagent.datamodel.data_mapping import INSTANT
from forecastingagent.utils.env_configs import DB_URL, DB_USER, DB_PASSWORD

# Initialise logger instance (ensure consistent logger level`)
logger = agentlogging.get_logger('prod')

class TSClient:

    # Create ONE JVM module view on class level and import all required java classes
    jpsBaseLibView = jpsBaseLibGW.createModuleView()
    jpsBaseLibGW.importPackages(jpsBaseLibView, "uk.ac.cam.cares.jps.base.query.*")
    jpsBaseLibGW.importPackages(jpsBaseLibView, "uk.ac.cam.cares.jps.base.timeseries.*")

    def __init__(self, kg_client, timeclass=INSTANT, rdb_url=DB_URL, 
                 rdb_user=DB_USER, rdb_password=DB_PASSWORD):
        """
        Initialise TimeSeriesClient (default properties taken from environment variables)
        
        Arguments:
            kg_client (KGClient): KGClient object (as per `kgclient.py`)
            timeclass: Java time class objects supported by PostgreSQL
                       (see: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html)
            rdb_url (str): URL of relational database
            rdb_user (str): Username for relational database
            rdb_password (str): Password for relational database
        """

        # 1) Create an instance of a RemoteStoreClient (to retrieve RDB connection)
        try:
            self.connection = TSClient.jpsBaseLibView.RemoteRDBStoreClient(rdb_url, rdb_user, rdb_password)
        except Exception as ex:
            logger.error("Unable to initialise TS Remote Store client.")
            raise TSException("Unable to initialise TS Remote Store client.") from ex

        # 2) Initiliase TimeSeriesClient
        try:
            self.tsclient = TSClient.jpsBaseLibView.TimeSeriesClient(kg_client.kg_client, timeclass)
        except Exception as ex:
            logger.error("Unable to initialise TS client.")
            raise TSException("Unable to initialise TS client.") from ex


    @contextmanager
    def connect(self):
        """
        Create context manager for RDB connection using getConnection method of Java
        TimeSeries client (i.e. to ensure connection is closed after use)
        """
        conn = None
        try:            
            conn = self.connection.getConnection()
            yield conn
        finally:
            if conn is not None:
                conn.close()


    @staticmethod
    def create_timeseries(times: list, dataIRIs: list, values: list):
        """
        Create Java TimeSeries object (i.e. to attach via TSClient)
        
        Arguments:
            times (list): List of time stamps
            dataIRIs (list): List of dataIRIs
            values (list): List of list of values per dataIRI     
        """
        try:
            timeseries = TSClient.jpsBaseLibView.TimeSeries(times, dataIRIs, values)
        except Exception as ex:
            logger.error("Unable to create TimeSeries object.")
            raise TSException("Unable to create timeseries.") from ex
        
        return timeseries


    def init_timeseries(self, dataIRI, times, values, ts_type, time_format):
        """
        This method instantiates a new time series and immediately adds data to it.
        
        Arguments:
            dataIRI (str): IRI of instance with hasTimeSeries relationship
            times (list): List of times/dates
            values (list): List of actual values
            tsClient (TSClient): TSClient object
            ts_type (Java class): Java class of time series values
            time_format (str): Time format (e.g. "%Y-%m-%dT%H:%M:%SZ")
        """

        with self.connect() as conn:
            self.tsclient.initTimeSeries([dataIRI], [ts_type], time_format, conn)
            ts = TSClient.create_timeseries(times, [dataIRI], [values])
            self.tsclient.addTimeSeriesData(ts, conn)
        logger.info(f"Time series initialised in KG: {dataIRI}")


    def retrieve_timeseries(self, dataIRI):
        """
        This method retrieves the time series data for a given dataIRI
        
        Arguments:
            dataIRI (str): IRI of instance with hasTimeSeries relationship
        """
        with self.connect() as conn:
            ts = self.tsclient.getTimeSeries([dataIRI], conn)
        times = ts.getTimes()
        values = ts.getValues(dataIRI)

        # Unwrap Java time objects
        times = [t.toString() for t in times]
        
        return times, values
