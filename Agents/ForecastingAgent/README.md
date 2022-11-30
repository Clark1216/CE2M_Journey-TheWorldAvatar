# Forecasting Agent

This agent reads a time series from a KG and forecasts the time series using pre-trained models or Prophet. Finally, the forecast is reinstantiated into the KG.  The agent can be accessed via Flask requests.

# 1. Setup

This section specifies the minimum requirements to run the agent. 

&nbsp;
## 1.1 Prerequisites


### **1) Setting up a virtual environment setup**

It is highly recommended to use a virtual environment for this project. The virtual environment can be created as follows (`python` command might need to be replaced with `py` depending on whether Python is specified in system's `PATH` variables):

`(Windows)`
```cmd
$ python -m venv forecasting_venv
$ forecasting_venv\Scripts\activate.bat
(forecasting_venv) $
```
The above commands will create and activate the virtual environment `forecasting_venv` in the current directory.

### **2) Installation of required packages**

Install the `forecasting` project including all required packages listed in `setup.py`:

`(Windows)`
```
python -m pip install --upgrade pip  
python -m pip install -e . 

```
Or  to enable running tests:

`(Windows)`
```
python -m pip install -e .[dev]
```
If you use later a model pretrained with 'darts', conflicts can occur while loading the model, if your version differs from the version with which the model was trained.

### **3) Instantiated knowledge graph with time series**

In order to forecast a time series, this series has to be instantiated using  [the Time Series Client](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/JPS_BASE_LIB/src/main/java/uk/ac/cam/cares/jps/base/timeseries) beforehand. 
[The district heating instantiation module](https://github.com/cambridge-cares/pirmasens) provides an example how that can look like. In case you do not have access you might reach out to sm453@cam.ac.uk to get access.

### **4) Endpoints**

Set your postgres database and blazegraph endpoints in your properties [file](./resources/timeseries.properties). 


&nbsp;
# 2. Using the Agent
## General workflow
- [Agent uml](https://lucid.app/lucidchart/def34dba-537c-48c7-9fa4-89bda55b4dc5/edit?viewport_loc=-3263%2C-197%2C3677%2C1765%2C0_0&invitationId=inv_1ed2a56a-16f0-4884-a5cb-a5aa69daba1e) provides an overview how the agent works.


In this section describes the workflow and most important steps to access and extent the agent.

The `Forecasting Agent` forecasts an existing time series in an KG using its `iri`.

After verifying the received HTTP request, the agent loads a model configuration from the [mapping file]. This is either the `DEFAULT` one (this will use the Prophet model) or else must be specified with the `use_model_configuration` parameter in the HTTP request to use a pre-trained model other than Prophet.

Next the agent loads the time series (+ covariates if `load_covariates_func` is given in the loaded configuration) with the TSClient. 

Then, it loads the model. This is either a pretrained model specified in the model configuration with the model link `model_path_pth_link` and the checkpoint link `model_path_ckpt_link` or else a new Prophet model is fitted to predict the data. The forecast starts from the optional parameter `forecast start date` in the request or if not specifed the last available date is taken. The forecast lasts over the number of specified time steps (`horizon`).
Finally the forecasted time series is instantiated. For that purpose a new forecast iri is created and attached to the iri specified in the request.

## Starting the agent
Buy running  
```
python forecasting\flaskapp\wsgi.py
```
or [main in wsgi.py](./forecasting/flaskapp/wsgi.py) the flask app with the agent starts. To check if the agent works, open the port on which the agent is running, e.g. `http://127.0.0.1:5000` in your browser. 


&nbsp;
## Send http requests
[HTTPRequest_forecast](./resources/HTTP_request_forecast.http) shows a sample request to forecast an `iri`. 

### Input parameters
- **iri** is the `iri` of the object which has a time series attached to it. This iri will receive the hasForecastedValue instantiation.
- **horizon** the time steps the agent forecasts autorecursively into the future.
- **forecast_start_date** is the start dateTime of the forecast, if not specified, simple the last value is taken as a starting point. The series is split here and future available data is used to calculate the forecasting error.
- **data_length** determines the number of values loaded before `forecast_start_date`. This data is used directly as input to fit prophet or to scale the input for the pre-trained neural network.
If not set the default value from the [mapping file] is used.
- **use_model_configuration** if specified this model configuration from the [mapping file] is used.  


## Custom model configurations and new models
Specify your custom configurations following the example of the `TFT_HEAT_SUPPLY` model configuration in the [mapping file]. 

If you need covariates define es well the a function like `get_covs_heat_supply` for the `load_covariates_func` parameter in your configuration. To use your own pretrained model with darts, extend the [agent file] where `load_pretrained_model` is called. You can use as well `load_pretrained_model`, simply specify your model class and set the `input_length` like for `TFT_HEAT_SUPPLY`. 

# 3. How to run tests
 <span style="color:red"> Be aware: The test will clear your blazegraph namespace!! </span> Therefore, you should create an new blazegraph test namespace. Follow those steps:

1. Activate the virtual environment
`(Windows)`
```cmd
$ python -m venv forecasting_venv
$ forecasting_venv\Scripts\activate.bat
(forecasting_venv) $
```
2. Install required packages with
```
python -m pip install -e .[dev]
```
3. A [docker-compose.test.yml](./docker-compose.test.yml) file is provided to spin up a stack with a Blazegraph and a PostgreSQL container. Both PostgreSQL and Blazegraph use volumes to ensure data persistence and the respective data can be found under `\\wsl$\docker-desktop-data\version-pack-data\community\docker` in the local file system (Windows). To spin up the stack, run the following command from the same directory where this README is located:
```bash
# Spin up container stack
docker-compose -f "docker-compose.test.yml" up -d
```

4. Initialise a new Blazegraph namespace and PostgreSQL database: Both the Blazegraph namespace and the PostgreSQL database need to be (manually) created after spinning up the Docker stack. For Blazegraph, simply open the Blazegraph workbench (e.g. `http://localhost:<port number from docker-compose_stack>/blazegraph`) in any browser and create the needed namespace. For postgreSQL, pgAdmin can be used to connect to the database running within Docker by adding a new server with `localhost` and `port number` as defined in the [docker-compose.test.yml](./docker-compose.test.yml) file. The new database can be created afterwards.

5.  Set your postgres database and blazegraph endpoints in your properties [file](./resources/timeseries.properties). 

6. To start all tests run in your console:
```
pytest tests/
```
&nbsp;
<span style="color:red"> Be aware: The test will clear your blazegraph namespace, you should make sure to have a new one for testing purpose. </span> 


&nbsp;
# Authors #
Magnus Mueller (mm2692@cam.ac.uk), November 2022

Markus Hofmeister (mh807@cam.ac.uk), October 2022


<!-- Links -->
<!-- websites -->
[agent file]: /forecasting/forecasting_agent/agent.py
[mapping file]: /forecasting/datamodel/data_mapping.py
[allows you to publish and install packages]: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages
[Create SSH key]: https://docs.digitalocean.com/products/droplets/how-to/add-ssh-keys/create-with-openssh/
[Container registry on Github]: https://ghcr.io
[Github package repository]: https://github.com/cambridge-cares/TheWorldAvatar/wiki/Packages
[http://localhost:5000/]: http://localhost:5000/
[Java Runtime Environment version >=11]: https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot
[JDBC driver]: https://jdbc.postgresql.org/download/ 
[OntoBuiltEnv]: http://www.theworldavatar.com/ontology/ontobuiltenv/OntoBuiltEnv.owl
[personal access token]: https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token
[py4jps]: https://pypi.org/project/py4jps/#description
[Upload SSH key]: https://docs.digitalocean.com/products/droplets/how-to/add-ssh-keys/to-existing-droplet/
[VSCode via SSH]: https://code.visualstudio.com/docs/remote/ssh
[HM Land Registry Open Data]: https://landregistry.data.gov.uk/
[Price Paid Linked Data]: https://landregistry.data.gov.uk/app/root/doc/ppd
[UK House Price Index Linked Data]: https://landregistry.data.gov.uk/app/ukhpi/doc
[HM Land Registry SPARQL endpoint]: http://landregistry.data.gov.uk/landregistry/query

<!-- github -->
[Common stack scripts]: https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Deploy/stacks/dynamic/common-scripts
[credentials]: https://github.com/cambridge-cares/TheWorldAvatar/tree/1376-dev-building-matching-agent/Agents/BuildingMatchingAgent/credentials
[JPS_BASE_LIB]: https://github.com/cambridge-cares/TheWorldAvatar/tree/main/JPS_BASE_LIB
[spin up the stack]: https://github.com/cambridge-cares/TheWorldAvatar/blob/main/Deploy/stacks/dynamic/stack-manager/README.md
[Stack-Clients]: https://github.com/cambridge-cares/TheWorldAvatar/tree/dev-MetOfficeAgent-withinStack/Deploy/stacks/dynamic/stack-clients
[TheWorldAvatar]: https://github.com/cambridge-cares/TheWorldAvatar
[EPC Agent]: https://github.com/cambridge-cares/TheWorldAvatar/tree/dev-EPCInstantiationAgent/Agents/EnergyPerformanceCertificateAgent

<!-- files -->
[Dockerfile]: ./Dockerfile
[docker compose file]: ./docker-compose.yml
[resources]: ./resources
[stack.sh]: ./stack.sh
[stack_configs]: ./landregistry/utils/stack_configs.py