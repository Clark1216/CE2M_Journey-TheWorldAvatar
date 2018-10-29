import rdflib
import re
import sys
import json
from datetime import datetime

from caresjpsutil import returnExceptionToJava, returnResultsToJava
from caresjpsutil import PythonLogger
from sparql_wrapper import sparqlQueryRead

class PowerplantSPARQLSync:

    def __init__(self, powerplant):
        self.powerplantIRI = powerplant
        self.graph = powerplant

        self.generationTechnologyMap = {
            'Cogeneration': 'cogeneration',
            'CombinedCycleGasTurbine': 'CCGT',
            'GasEngine': 'Engine',
            'OpenCycleGasTurbine': 'OCGT',
            'SubCriticalThermal': 'subcritical',
            'SuperCriticalThermal': 'supercritical',
            'UltraSuperCriticalThermal': 'ultrasupercritical'
        }

        self.primaryFuelToFuelUsedMap = {
            'natural_gas': 'natural_gas',
            'oil': 'oil',
            'coal': 'coal',
            'bituminous': 'coal',
            'subbituminous': 'coal',
            'lignite': 'coal',
            'anthracite': 'coal',
            'coal_biomass': 'coal'
        }

    def __del__(self):
        pass

    def getPowerplantInfo(self):
        queryString = """
            PREFIX j1: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX j6: <http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#>
            PREFIX j8: <http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#>
            PREFIX j5: <http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#>
            PREFIX j7: <http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#>
    
            SELECT ?country ?capacityValue ?year ?primaryFuel ?genTech ?annualGenValue ?genCostValue ?emissionRateValue ?emissionRateValIRI
            WHERE
            {{ 
                GRAPH <{1}>
            {{
                <{0}> j1:hasAddress ?country .
    
                <{0}> j6:designCapacity ?capacityIRI.
                    ?capacityIRI j1:hasValue ?capacity.
                        ?capacity j1:numericalValue ?capacityValue.
    
                <{0}> j8:hasYearOfBuilt ?yearOfBuilt.
                            ?yearOfBuilt j1:hasValue ?yearValue.
                                ?yearValue j1:numericalValue ?year.
    
                <{0}> j5:realizes ?generation.
                    ?generation j8:consumesPrimaryFuel ?primaryFuel.
                    ?generation j8:usesGenerationTechnology ?genTech.
                    ?generation j8:hasAnnualGeneration ?annualGenIRI.
                        ?annualGenIRI j1:hasValue ?annualGenValIRI.
                            ?annualGenValIRI j1:numericalValue ?annualGenValue.
                    ?generation j7:hasCosts ?genCostIRI.
                        ?genCostIRI j1:hasValue ?genCostValIRI.
                            ?genCostValIRI j1:numericalValue ?genCostValue.
                    ?generation j7:hasEmission ?emissionRateIRI.
                        ?emissionRateIRI j1:hasValue ?emissionRateValIRI.
                            ?emissionRateValIRI j1:numericalValue ?emissionRateValue
            }}
            }}
        """.format(self.powerplantIRI, self.graph)
    
        queryResults = sparqlQueryRead(queryString)['results']['bindings']
    
        # get country
        country = re.search(r'/([a-zA-Z_]+)$', str(queryResults[0]['country']['value'])).group(1)
    
        # get capacity value
        capacityValue = int(queryResults[0]['capacityValue']['value'])
    
        # get year
        year = int(queryResults[0]['year']['value'])
    
        # get primary fuel
        primaryFuel = re.search(r'#([a-zA-Z]+)$', str(queryResults[0]['primaryFuel']['value'])).group(1).lower()
        if primaryFuel == "naturalgas":
            primaryFuel = "natural_gas"
        elif primaryFuel == "coalbiomass":
            primaryFuel = "coal_biomass"
    
        # get generation
        genTechRegexResult = re.search(r'#([a-zA-Z]+)$', str(queryResults[0]['genTech']['value'])).group(1)
        genTech = self.generationTechnologyMap[genTechRegexResult]
    
        # get output_MWh (a.k.a. annual generation in knowledge base)
        annualGenValue = float(queryResults[0]['annualGenValue']['value'])
    
        # fuel_used
        fuelUsed = self.primaryFuelToFuelUsedMap[primaryFuel]
    
        # emission_rate
        emissionRate = float(queryResults[0]['emissionRateValue']['value'])
    
        dict = {}
        dict['country'] = country
        dict['capacity_MW'] = capacityValue
        dict['primary_fuel'] = primaryFuel
        dict['generation_technology'] = genTech
        dict['age'] = datetime.now().year - year
        dict['output_MWh'] = annualGenValue
        dict['fuel_used'] = fuelUsed
        dict['emission_rate'] = emissionRate
        
        return dict
    
if __name__ == "__main__":
    pythonLogger = PythonLogger('powerplant_sparql_read.py')
    pythonLogger.postInfoToLogServer('start of powerplant_sparql_read.py')
    
    try:
#         start_time = time.time()
#         plantIRI = "http://www.theworldavatar.com/kb/powerplants/Norocholai_Laskvijaya_Coal_Power_Plant_Sri_Lanka.owl#Norocholai_Laskvijaya_Coal_Power_Plant_Sri_Lanka"
        plantIRI = sys.argv[1]
        pSPARQL = PowerplantSPARQLSync(plantIRI)
        powerplantInfo = pSPARQL.getPowerplantInfo()
        returnResultsToJava(json.dumps(powerplantInfo))
        pythonLogger.postInfoToLogServer('end of powerplant_sparql_read.py')
#         print("{} seconds".format(time.time() - start_time))
    except Exception as e:
        returnExceptionToJava(e)
        pythonLogger.postInfoToLogServer('end of powerplant_sparql_read.py')