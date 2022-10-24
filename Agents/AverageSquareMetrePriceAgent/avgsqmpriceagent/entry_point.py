# To avoid unnecessary logging information from py4j package, set logger level before 
# first creation of JPS_BASE_LIB module view (i.e. jpsBaseLibView = jpsBaseLibGW.createModuleView())
import logging
logging.getLogger("py4j").setLevel(logging.ERROR)

from pyderivationagent.conf import config_derivation_agent
from avgsqmpriceagent.utils.stack_configs import QUERY_ENDPOINT, UPDATE_ENDPOINT

from avgsqmpriceagent.agent import AvgSqmPriceAgent
from avgsqmpriceagent.agent import default


def create_app():
    # Depending on the deployment environment, different ways to retrieve/set the 
    # environment variables for Derivation Agent are required:
    # 1) For deployment as Flask app within Docker container as part of the Stack
    #       a) Define environment variables in `environment` node of the `docker-compose.yml` file
    #       b) Retrieve environment variables using
    #               agent_config = config_derivation_agent()
    #
    # 2) For deployment as Flask app within Docker container, but outside the Stack
    #    (i.e. using 'docker compose up' with docker-compose.yml and avgsqmpriceagent.env files)
    #       a) Create `avgsqmpriceagent.env` file (based on `avgsqmpriceagent.env.example`)
    #       b) Include `env_file` node in `docker-compose.yml` with path to the `avgsqmpriceagent.env` file
    #       c) Retrieve environment variables here using:
    #               agent_config = config_derivation_agent()
    #
    # 3) For deployment as Flask app outside Docker container 
    #       a) Create `avgsqmpriceagent.env` file (based on `avgsqmpriceagent.env.example`)
    #       b) Retrieve environment variables here using local path to .env file:
    #               agent_config = config_derivation_agent('./avgsqmpriceagent.env')
    agent_config = config_derivation_agent()

    agent = AvgSqmPriceAgent(
        # Settings read from environment variables (.env file, docker-compose)
        register_agent=agent_config.REGISTER_AGENT,
        agent_iri=agent_config.ONTOAGENT_SERVICE_IRI, 
        time_interval=agent_config.DERIVATION_PERIODIC_TIMESCALE,
        derivation_instance_base_url=agent_config.DERIVATION_INSTANCE_BASE_URL,
        agent_endpoint=agent_config.ONTOAGENT_OPERATION_HTTP_URL,
        # Settings read from Stack Clients
        kg_url=QUERY_ENDPOINT,
        kg_update_url=UPDATE_ENDPOINT,
        # Miscellaneous settings
        logger_name='prod',
        max_thread_monitor_async_derivations=1
    )

    agent.add_url_pattern('/', 'root', default, methods=['GET'])

    agent.start_all_periodical_job()

    # Expose flask app of agent
    return agent.app
