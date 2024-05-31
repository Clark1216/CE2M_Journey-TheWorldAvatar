################################################
# Authors: Jiying Chen (jc2341@cam.ac.uk)      #    
# Date: 11 March 2024                            #
################################################


from agent.datainstantiation.jpsSingletons import jpsBaseLibGW

#TIME_FORMAT = '%Y-%m-%dT%H:%M:%S.%sZ'
FORMAT = '%Y-%m-%dT%H:%M:%SZ'

### Create required JAVA classes ###

# Create ONE JVM module view
jpsBaseLibView = jpsBaseLibGW.createModuleView()

# Create data class for time entries (LocalDate)
# PostgreSQL supported data types: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html
# LocalDate = jpsBaseLibView.java.time.LocalDate
# TIMECLASS = LocalDate.now().getClass()

Instant = jpsBaseLibView.java.time.Instant
TIMECLASS = Instant.now().getClass()

# Create data class for all time series data (i.e. all data as double)
DATATYPE = jpsBaseLibView.java.lang.Double.TYPE