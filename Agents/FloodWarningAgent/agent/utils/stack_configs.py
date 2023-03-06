################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 08 Sep 2022                            #
################################################

# The purpose of this module is to retrieve relevant properties and settings 
# (e.g. for the Time Series Client) from Stack clients

from agent.utils.env_configs import DATABASE, NAMESPACE
from agent.utils.javagateway import stackClientsGw

from py4jps import agentlogging

# Initialise logger
logger = agentlogging.get_logger("prod")


def retrieve_stack_configs():
    """
        Reads settings from Stack clients as global variables,
        i.e. only global within this sub-module
    """

    # Define global scope for global variables
    global DB_URL, DB_USER, DB_PASSWORD, \
           QUERY_ENDPOINT, UPDATE_ENDPOINT, ONTOP_URL
    
    # Create module views to relevant Stack clients
    stackClientsView = stackClientsGw.createModuleView()
    stackClientsGw.importPackages(stackClientsView, "com.cmclinnovations.stack.clients.docker.ContainerClient")
    stackClientsGw.importPackages(stackClientsView, "com.cmclinnovations.stack.clients.blazegraph.BlazegraphEndpointConfig")
    stackClientsGw.importPackages(stackClientsView, "com.cmclinnovations.stack.clients.postgis.PostGISEndpointConfig")
    stackClientsGw.importPackages(stackClientsView, "com.cmclinnovations.stack.clients.ontop.OntopEndpointConfig")

    # Retrieve endpoint configurations from Stack clients
    containerClient = stackClientsView.ContainerClient()
    # Blazegraph
    bg = stackClientsView.BlazegraphEndpointConfig("","","","","")
    bg_conf = containerClient.readEndpointConfig("blazegraph", bg.getClass())
    # PostgreSQL/PostGIS
    pg = stackClientsView.PostGISEndpointConfig("","","","","")
    pg_conf = containerClient.readEndpointConfig("postgis", pg.getClass())
    # Ontop
    ont = stackClientsView.OntopEndpointConfig("","","","","")
    ont_conf = containerClient.readEndpointConfig("ontop", ont.getClass())

    # Extract PostgreSQL/PostGIS database URL
    DB_URL = pg_conf.getJdbcURL(DATABASE)
    # Extract PostgreSQL database username and password
    DB_USER = pg_conf.getUsername()
    DB_PASSWORD = pg_conf.getPassword()

    # Extract SPARQL endpoints of KG 
    # (i.e. Query and Update endpoints are equivalent for Blazegraph)
    QUERY_ENDPOINT = bg_conf.getUrl(NAMESPACE)
    UPDATE_ENDPOINT = QUERY_ENDPOINT

    # Extract ONTOP endpoint
    ONTOP_URL = ont_conf.getUrl()


# Run when module is imported
retrieve_stack_configs()
