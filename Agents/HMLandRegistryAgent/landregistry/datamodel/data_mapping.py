################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 16 Oct 2022                            #
################################################

# The purpose of this module is to provide a mapping between retrieved 
# HM Land Registry's Price Paid Data property types and instantiated property
# types according to OntoBuiltEnv

# For details on PPD data, see:
# https://www.gov.uk/guidance/about-the-price-paid-data#explanations-of-column-headers-in-the-ppd

from landregistry.datamodel.iris import *

from landregistry.kgutils.javagateway import jpsBaseLibGW


OTHER_PROPERTY_TYPE = 'OTHER'
PPD_PROPERTY_TYPES = {
    'SEMI-DETACHED': OBE_BUILDING,
    'TERRACED': OBE_BUILDING,
    'DETACHED': OBE_BUILDING,
    'FLAT-MAISONETTE': OBE_FLAT,
    'OTHER': OTHER_PROPERTY_TYPE
}

# Times are reported in xsd:gYearMonth, i.e. ISO 8601 YYYY-MM
TIME_FORMAT = 'YYYY-MM'

### Create required JAVA classes ###

# Create data class for all time series data (i.e. all data as double)
jpsBaseLibView = jpsBaseLibGW.createModuleView()
DATACLASS = jpsBaseLibView.java.lang.Double.TYPE

# Create data class for time entries (LocalDate)
# Dates from HM Land Registry actually xsd:gYearMonth, but YearMonth not 
# supported by TimeSeriesCLient RDB implementation
# PostgreSQL supported data types: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html
LocalDate = jpsBaseLibView.java.time.LocalDate
TIMECLASS = LocalDate.now().getClass()
