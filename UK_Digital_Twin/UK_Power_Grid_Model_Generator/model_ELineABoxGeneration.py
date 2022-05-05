##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 04 May 2022          #
##########################################

"""This module is designed to generate and update the A-box of UK power grid model_ELine."""

import os
import owlready2
from rdflib.extras.infixowl import OWL_NS
from rdflib import Graph, URIRef, Literal, ConjunctiveGraph
from rdflib.namespace import RDF, RDFS
from rdflib.plugins.sleepycat import Sleepycat
from rdflib.store import NO_STORE, VALID_STORE
import sys
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, BASE)
from UK_Digital_Twin_Package import UKDigitalTwin as UKDT
from UK_Digital_Twin_Package import UKDigitalTwinTBox as T_BOX
from UK_Digital_Twin_Package import UKPowerGridModel as UK_PG
from UK_Digital_Twin_Package import UKPowerPlant as UKpp
from UK_Digital_Twin_Package import UKPowerGridTopology as UK_Topo
from UK_Digital_Twin_Package import TopologicalInformationProperty as TopoInfo
from UK_Digital_Twin_Package.OWLfileStorer import storeGeneratedOWLs, selectStoragePath, readFile, specifyValidFilePath
import UK_Power_Grid_Model_Generator.SPARQLQueryUsedInModel as query_model
from UK_Power_Grid_Model_Generator.AddModelVariables import AddModelVariable
from UK_Power_Grid_Topology_Generator.topologyABoxGeneration import createTopologicalInformationPropertyInstance
from UK_Digital_Twin_Package.GraphStore import LocalGraphStore
from UK_Digital_Twin_Package import EndPointConfigAndBlazegraphRepoLabel as endpointList
from UK_Digital_Twin_Package import BranchPropertyInitialisation as BPI

from UK_Digital_Twin_Package.derivationInterface import createMarkUpDerivation
from pyasyncagent.agent.async_agent import AsyncAgent
from pyasyncagent.kg_operations.sparql_client import PySparqlClient # the import of this agent will need a parckage name werkzeug, install `pip install Werkzeug==2.0.2`, otherwise it will report the error message
import uuid
from py4jps.resources import JpsBaseLib


"""Notation used in URI construction"""
HASH = '#'
SLASH = '/'
UNDERSCORE = '_'
OWL = '.owl'

"""Create an instance of Class UKDigitalTwin"""
dt = UKDT.UKDigitalTwin()

"""Create an object of Class UKDigitalTwinTBox"""
t_box = T_BOX.UKDigitalTwinTBox()

"""Create an object of Class UKPowerPlantDataProperty"""
ukpp = UKpp.UKPowerPlant()

"""Blazegraph UK digital tiwn"""
endpoint_label = endpointList.ukdigitaltwin['lable']
endpoint_iri = endpointList.ukdigitaltwin['queryendpoint_iri']

"""Sleepycat storage path"""
userSpecifiePath_Sleepycat = None # user specified path
userSpecified_Sleepycat = False # storage mode: False: default, True: user specified

"""T-Box URI"""
ontocape_upper_level_system     = owlready2.get_ontology(t_box.ontocape_upper_level_system).load()
ontocape_derived_SI_units       = owlready2.get_ontology(t_box.ontocape_derived_SI_units).load()
ontocape_mathematical_model     = owlready2.get_ontology(t_box.ontocape_mathematical_model).load()
ontopowsys_PowerSystemModel     = owlready2.get_ontology(t_box.ontopowsys_PowerSystemModel).load()

"""User specified folder path"""
filepath = None
userSpecified = False

"""EBus Conjunctive graph identifier"""
model_ELine_cg_id = "http://www.theworldavatar.com/kb/UK_Digital_Twin/UK_power_grid/10_bus_model/Model_ELine"

### Functions ###
"""Main function: create the named graph Model_EBus and their sub graphs each ELine"""
## TODO: numOfBus should only be queried once and should be queried in the model initialiser, Bus and Gen should be changed 
## TODO: rerun the topologyCreator and reupload the test1 repo 
def createModel_ELine(numOfBus, topologyNodeIRI, powerSystemModelIRI, AgentIRI, derivationClient, OrderedBusNodeIRIList, initialiserMethod, OWLFileStoragePath, updateLocalOWLFile = True, storeType = "default"): 
    # Query the eline topological information and geometry information, the return is a dictionary 
    ##TODO: delete the ORDER BY ASC(?ELineNode)
    ELineTopoAndGeometryInfo, branchVoltageLevel = query_model.queryELineTopologicalInformation(topologyNodeIRI, endpoint_label)
    if len(ELineTopoAndGeometryInfo) == 0:
        raise Exception('ELineTopoAndGeometryInfo is empty, please check the return from queryELineTopologicalInformation.')
    uk_eline_model = UK_PG.UKElineModel(numOfBus, initialiserMethod)
    uk_topo = UK_Topo.UKPowerGridTopology(numOfBus)

    ## set up the storage path and Sleepycat
    defaultPath_Sleepycat = uk_eline_model.SleepycatStoragePath
    topoAndConsumpPath_Sleepycat = uk_topo.SleepycatStoragePath
    defaultStoredPath = uk_eline_model.StoreGeneratedOWLs
    filepath = specifyValidFilePath(defaultStoredPath, OWLFileStoragePath, updateLocalOWLFile)
    if filepath == None:
        return    
    store = LocalGraphStore(storeType)
    # topo_info, busInfoArrays, branchTopoInfoArrays, branchPropertyArrays = createTopologicalInformationPropertyInstance(numOfBus, numOfBranch)
    global userSpecifiePath_Sleepycat, userSpecified_Sleepycat 
    # create conjunctive graph storing the generated graphs in a specified Sleepycat on-disc graph store
    if isinstance(store, Sleepycat): 
        print('The store is Sleepycat')
        cg_model_ELine = ConjunctiveGraph(store=store, identifier = model_ELine_cg_id)
        if userSpecifiePath_Sleepycat == None and userSpecified_Sleepycat:
            print('****Needs user to specify a Sleepycat storage path****')
            userSpecifiePath_Sleepycat = selectStoragePath()
            userSpecifiePath_Sleepycat_ = userSpecifiePath_Sleepycat + '\\' + 'ConjunctiveGraph_UKPowerGrid_ELine'
            sl = cg_model_ELine.open(userSpecifiePath_Sleepycat_, create = False) 
            
        elif os.path.exists(defaultPath_Sleepycat) and not userSpecified_Sleepycat:
            print('****Non user specified Sleepycat storage path, will use the default storage path****')
            sl = cg_model_ELine.open(defaultPath_Sleepycat, create = False)        
        else:
            print('****Create Sleepycat store with its default path****')
            sl = cg_model_ELine.open(defaultPath_Sleepycat, create = True)   
        
        if sl == NO_STORE:
        # There is no underlying Sleepycat infrastructure, so create it
            cg_model_ELine.open(defaultPath_Sleepycat, create = True)
        else:
            assert sl == VALID_STORE, "The underlying sleepycat store is corrupt"
    else:
        print('Store is IOMemery')
     
    print('################START createModel_ELine#################')
    ontologyIRI = dt.baseURL + SLASH + dt.topNode + SLASH + str(uuid.uuid4())
    namespace = UK_PG.ontopowsys_namespace  
    ## ElectricalBusModel node IRI 
    ElectricalELineModelIRI = namespace + uk_eline_model.ModelELineKey + str(uuid.uuid4()) # root node
    ## create a named graph
    g = Graph(store = store, identifier = URIRef(ontologyIRI))
    ## Import T-boxes
    g.set((g.identifier, RDF.type, OWL_NS['Ontology']))
    g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_mathematical_model)))
    g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_upper_level_system)))  
    g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontopowsys_PowerSystemModel))) 
    g.set((g.identifier, RDFS.comment, Literal('This ontology represents mathematical model of the electricity branch of the UK energy system.'))) 
    ## Link topologyNodeIRI with PowerSystemModel and ElectricalBusModelIRI
    g.add((URIRef(powerSystemModelIRI), URIRef(ontopowsys_PowerSystemModel.hasModelingPrinciple.iri), URIRef(topologyNodeIRI)))
    g.add((URIRef(ElectricalELineModelIRI), URIRef(ontocape_upper_level_system.isExclusivelySubsystemOf.iri), URIRef(powerSystemModelIRI)))
    g.add((URIRef(ElectricalELineModelIRI), RDF.type, URIRef(t_box.ontopowsys_PowerSystemModel + 'ElectricalBranchModel')))
    g.add((URIRef(powerSystemModelIRI), RDF.type, URIRef(ontopowsys_PowerSystemModel.PowerSystemModel.iri)))
    
    counter = 1
    for eline in ELineTopoAndGeometryInfo:         
    # if ELineTopoAndGeometryInfo[0] != None: # test
    #     eline = ELineTopoAndGeometryInfo[0] # test
        
        ELineNodeIRI = eline['ELineNode']

        # root_uri = eline['ELine'].split('#')[0]
        # namespace = root_uri + HASH
        # node_locator = eline['ELine'].split('#')[1]
        # root_node = namespace + 'Model_' + node_locator
        # father_node = UKDT.nodeURIGenerator(4, dt.powerGridModel, numOfBus, "ELine")
        
        # # create a named graph
        # g = Graph(store = store, identifier = URIRef(root_uri))
        # # Import T-boxes
        # g.set((g.identifier, RDF.type, OWL_NS['Ontology']))
        # g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_mathematical_model)))
        # g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_upper_level_system)))  
        # g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontopowsys_PowerSystemModel))) 
        # # Add root node type and the connection between root node and its father node   
        # g.add((URIRef(root_node), URIRef(ontocape_upper_level_system.isExclusivelySubsystemOf.iri), URIRef(father_node)))
        # g.add((URIRef(father_node), RDFS.label, Literal("UK_Electrical_Grid_" + str(numOfBus) + "_Bus_" + str(numOfBranch) + "_Branch_Model")))
        # g.add((URIRef(father_node), RDF.type, URIRef(ontocape_mathematical_model.Submodel.iri)))
        # g.add((URIRef(root_node), RDF.type, URIRef(ontopowsys_PowerSystemModel.PowerFlowModelAgent.iri)))
        # g.add((URIRef(root_node), RDF.type, URIRef(t_box.ontopowsys_PowerSystemModel + 'ElectricalBranchModel'))) # undefined T-box class, the sub-class of PowerFlowModelAgent
        # g.add((URIRef(father_node), URIRef(ontocape_upper_level_system.isComposedOfSubsystem.iri), URIRef(root_node)))
        # # link with ELine node in topology
        # g.add((URIRef(root_node), URIRef(ontocape_upper_level_system.models.iri), URIRef(eline['ELine'])))
        # g.add((URIRef(eline['ELine']), URIRef(ontocape_upper_level_system.isModeledBy.iri), URIRef(root_node)))
        
        ## specify the initialisation method for each branch instance of branch model
        ###1. create an instance of the BranchPropertyInitialisation class and get the initialiser method by applying the 'getattr' function 
        initialisation = BPI.BranchPropertyInitialisation()
        initialiser = getattr(initialisation, initialiserMethod)
        ###2. execute the initialiser with the branch model instance as the function argument  
        #TODO: modify the initialiser
        uk_eline_model = initialiser(uk_eline_model, eline, branchVoltageLevel, OrderedBusNodeIRIList, counter) 

        ModelInputVariableIRIList = []
        # AddModelVariable to Eline entity
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.FROMBUSKey, int(uk_eline_model.FROMBUS), None, ontopowsys_PowerSystemModel.BusFrom.iri)
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.TOBUSKey, int(uk_eline_model.TOBUS), None, ontopowsys_PowerSystemModel.BusTo.iri)  
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.R_Key, float(uk_eline_model.R), ontocape_derived_SI_units.ohm.iri, ontopowsys_PowerSystemModel.R.iri)     
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.X_Key, float(uk_eline_model.X), ontocape_derived_SI_units.ohm.iri, ontopowsys_PowerSystemModel.X.iri) 
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.B_Key, float(uk_eline_model.B), (t_box.ontocape_derived_SI_units + 'siemens'), ontopowsys_PowerSystemModel.B.iri)    
        ModelInputVariableIRIList.append(varNode)

        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.RateAKey, float(uk_eline_model.RateA), (t_box.ontocape_derived_SI_units + 'MVA'), ontopowsys_PowerSystemModel.RateA.iri)       
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.RateBKey, float(uk_eline_model.RateB), (t_box.ontocape_derived_SI_units + 'MVA'), ontopowsys_PowerSystemModel.RateB.iri)
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.RateCKey, float(uk_eline_model.RateB), (t_box.ontocape_derived_SI_units + 'MVA'), ontopowsys_PowerSystemModel.RateC.iri)
        ModelInputVariableIRIList.append(varNode)
       
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.RATIOKey, float(uk_eline_model.RATIO), None, ontopowsys_PowerSystemModel.RatioCoefficient.iri)
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.ANGLEKey, float(uk_eline_model.ANGLE), ontocape_derived_SI_units.degree.iri, ontopowsys_PowerSystemModel.Angle.iri)    
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.STATUSKey, int(uk_eline_model.STATUS), None, ontopowsys_PowerSystemModel.BranchStatus.iri)    
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.ANGMINKey, float(uk_eline_model.ANGMIN), ontocape_derived_SI_units.degree.iri, ontopowsys_PowerSystemModel.AngleMin.iri)   
        ModelInputVariableIRIList.append(varNode)
        
        g, varNode = AddModelVariable(g, ELineNodeIRI, namespace, uk_eline_model.ANGMAXKey, float(uk_eline_model.ANGMAX), ontocape_derived_SI_units.degree.iri, ontopowsys_PowerSystemModel.AngleMax.iri) 
        ModelInputVariableIRIList.append(varNode)            
        
        print(g.serialize(format="pretty-xml").decode("utf-8"))

        #TODO: add derivation and storeclient       
        # generate/update OWL files
        if updateLocalOWLFile == True:    
            # Store/update the generated owl files      
            if filepath[-2:] != "\\": 
                filepath_ = filepath + '\\' + 'Model_' + str(numOfBus) + '_Bus_Grid_'  + OWL
            else:
                filepath_ = filepath + 'Model_' + str(numOfBus) + '_Bus_Grid_'  + OWL
            storeGeneratedOWLs(g, filepath_)
        counter += 1
    if isinstance(store, Sleepycat):  
        cg_model_ELine.close()       
    return

if __name__ == '__main__':    
    # createModel_ELine('default', False, 10, 14, 2019, 'defaultBranchInitialiser', None, True)    
    createModel_ELine('default', False, 29, 99, 2019, 'preSpecifiedBranchInitialiser', None, True)  
    print('Terminated')