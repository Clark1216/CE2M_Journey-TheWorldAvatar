###--- Common PREFIX for SPARQL query ---###
PREFIX_RDFS = 'PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> '
PREFIX_RDF = 'PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> '
PREFIX_XSD = 'PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#> '
PREFIX_OWL = 'PREFIX owl:    <http://www.w3.org/2002/07/owl#> '
PREFIX_OM = 'PREFIX om:     <http://www.ontology-of-units-of-measure.org/resource/om-2/> '

###--- Common IRI for units of measure ---###
OM_MEASURE = 'http://www.ontology-of-units-of-measure.org/resource/om-2/Measure'
OM_HASPHENOMENON = 'http://www.ontology-of-units-of-measure.org/resource/om-2/hasPhenomenon'
OM_HASVALUE = 'http://www.ontology-of-units-of-measure.org/resource/om-2/hasValue'
OM_HASNUMERICALVALUE = 'http://www.ontology-of-units-of-measure.org/resource/om-2/hasNumericalValue'
OM_HASUNIT = 'http://www.ontology-of-units-of-measure.org/resource/om-2/hasUnit'

###--- IRIs for OntoDoE ---###
ONTODOE_DESIGNOFEXPERIMENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#DesignOfExperiment'
ONTODOE_STRATEGY = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Strategy'
ONTODOE_TSEMO = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#TSEMO'
ONTODOE_LHS = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#LHS'
ONTODOE_CRITERION = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Criterion'
ONTODOE_CENTER = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Center'
ONTODOE_MAXIMUM = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Maximum'
ONTODOE_CENTERMAXIMUM = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#CenterMaximum'
ONTODOE_CORRELATION = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Correlation'
ONTODOE_DOMAIN = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#Domain'
ONTODOE_DESIGNVARIABLE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#DesignVariable'
ONTODOE_CONTINUOUSVARIABLE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#ContinuousVariable'
ONTODOE_CATEGORICALVARIABLE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#CategoricalVariable'
ONTODOE_SYSTEMRESPONSE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#SystemResponse'
ONTODOE_HISTORICALDATA = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#HistoricalData'
ONTODOE_NEWEXPERIMENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#NewExperiment'
ONTODOE_USESSTRATEGY = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#usesStrategy'
ONTODOE_HASDOMAIN = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#hasDomain'
ONTODOE_HASDESIGNVARIABLE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#hasDesignVariable'
ONTODOE_HASSYSTEMRESPONSE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#hasSystemResponse'
ONTODOE_UTILISESHISTORICALDATA = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#utilisesHistoricalData'
ONTODOE_PROPOSESNEWEXPERIMENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#proposesNewExperiment'
ONTODOE_REFERSTO = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#refersTo'
ONTODOE_UPPERLIMIT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#upperLimit'
ONTODOE_LOWERLIMIT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#lowerLimit'
ONTODOE_POSITIONALID = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#positionalID'
ONTODOE_MAXIMISE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#maximise'
ONTODOE_NRETRIES = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#nRetries'
ONTODOE_NSPECTRALPOINTS = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#nSpectralPoints'
ONTODOE_NGENERATIONS = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#nGenerations'
ONTODOE_POPULATIONSIZE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#populationSize'
ONTODOE_SEED = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontodoe/OntoDoE.owl#seed'

###--- IRIs for OntoRxn ---###
ONTORXN_REACTIONEXPERIMENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ReactionExperiment'
ONTORXN_REACTIONVARIATION = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ReactionVariation'
ONTORXN_CHEMICALREACTION = 'http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#ChemicalReaction'
ONTORXN_CATALYST = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#Catalyst'
ONTORXN_SOLVENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#Solvent'
ONTOKIN_PRODUCT = 'http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#Product'
ONTORXN_TARGETPRODUCT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#TargetProduct'
ONTORXN_IMPURITY = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#Impurity'
ONTORXN_PERFORMANCEINDICATOR = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#PerformanceIndicator'
ONTORXN_ENVIRONMENTALFACTOR = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#EnvironmentalFactor'
ONTORXN_YIELD = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#Yield'
ONTORXN_ECOSCORE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#EcoScore'
ONTORXN_CONVERSION = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#Conversion'
ONTORXN_SPACETIMEYIELD = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#SpaceTimeYield'
ONTORXN_RUNMATERIALCOST = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#RunMaterialCost'
ONTORXN_RESIDENCETIME = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ResidenceTime'
ONTORXN_REACTIONTEMPERATURE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ReactionTemperature'
ONTORXN_REACTIONPRESSURE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ReactionPressure'
ONTORXN_STOICHIOMETRYRATIO = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#StoichiometryRatio'
ONTORXN_REACTIONSCALE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#ReactionScale'
ONTORXN_INPUTCHEMICAL = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#InputChemical'
ONTORXN_OUTPUTCHEMICAL = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#OutputChemical'
ONTOCAPE_SINGLEPHASE = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#SinglePhase'
ONTOCAPE_STATEOFAGGREGATION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#StateOfAggregation'
ONTOCAPE_PHASECOMPONENT = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#PhaseComponent'
ONTOCAPE_COMPOSITION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#Composition'
ONTOCAPE_PHASECOMPONENTCONCENTRATION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#PhaseComponentConcentration'
ONTOCAPE_PHYSICALCONTEXT = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#PhysicalContext'
ONTOCAPE_VOLUMEBASEDCONCENTRATION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#Volume-BasedConcentration'
ONTOCAPE_MOLARITY = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#Molarity'
ONTOCAPE_CHEMICALSPECIES = 'http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#ChemicalSpecies'
ONTORXN_HASVARIATION = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasVariation'
ONTORXN_ISVARIATIONOF = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#isVariationOf'
ONTORXN_CONDUCTEDIN = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#conductedIn'
ONTORXN_ISOCCURENCEOF = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#isOccurenceOf'
ONTORXN_ISREALISEDAS = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#isRealisedAs'
ONTOCAPE_CATALYST = 'http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#hasCatalyst'
ONTORXN_HASSOLVENT = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasSolvent'
ONTORXN_HASRXNTYPE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRxnType'
ONTORXN_HASPERFORMANCEINDICATOR = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasPerformanceIndicator'
ONTORXN_HASRESTIME = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasResTime'
ONTORXN_HASRXNTEMPERATURE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRxnTemperature'
ONTORXN_HASRXNPRESSURE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRxnPressure'
ONTORXN_HASSTOICHIOMETRYRATIO = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasStoichiometryRatio'
ONTORXN_HASRXNSCALE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRxnScale'
ONTORXN_INDICATESMULTIPLICITYOF = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#indicatesMultiplicityOf'
ONTORXN_INDICATESUSAGEOF = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#indicatesUsageOf'
ONTORXN_HASINPUTCHEMICAL = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasInputChemical'
ONTORXN_HASOUTPUTCHEMICAL = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasOutputChemical'
ONTOCAPE_REPRESENTSTHERMODYNAMICBEHAVIOROF = 'http://www.theworldavatar.com/ontology/ontocape/material/material.owl#representsThermodynamicBehaviorOf'
ONTOCAPE_THERMODYNAMICBEHAVIOR = 'http://www.theworldavatar.com/ontology/ontocape/material/material.owl#thermodynamicBehavior'
ONTORXN_ISREALISATIONOF = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#isRealisationOf'
ONTOCAPE_HASSTATEOFAGGREGATION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#hasStateOfAggregation'
ONTOCAPE_ISCOMPOSEDOFSUBSYSTEM = 'http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#isComposedOfSubsystem'
ONTOCAPE_HAS_COMPOSITION = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#has_composition'
ONTOCAPE_COMPRISESDIRECTLY = 'http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#comprisesDirectly'
ONTOCAPE_HAS_PHYSICAL_CONTEXT = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#has_physical_context'
ONTOCAPE_HASPROPERTY = 'http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasProperty'
ONTOCAPE_REPRESENTSOCCURENCEOF = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#representsOccurenceOf'
ONTORXN_HASRINCHI = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRInChI'
ONTORXN_HASRDFILE = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#hasRDFILE'
ONTORXN_CDXML = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#cdXML'
ONTORXN_RXNSMILES = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#rxnSMILES'
ONTORXN_RXNCXSMILES = 'https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/JPS_Ontology/ontology/ontorxn/OntoRxn.owl#rxnCXSMILES'