from pyderivationagent.data_model.iris import *

RDFS_COMMENT = RDFS_BASE_URL + 'comment'

###--- Common Base URL ---###
ONTOCAPE_PHASESYSTEM = 'http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#'
ONTOCAPE_SUBSTANCE = 'http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#'
ONTOCAPE_REACTIONMECHANISM = 'http://www.theworldavatar.com/ontology/ontocape/material/substance/reaction_mechanism.owl#'
ONTOCAPE_MATERIAL = 'http://www.theworldavatar.com/ontology/ontocape/material/material.owl#'
ONTOCAPE_SYSTEM = 'http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#'
ONTOCAPE_BEHAVIOR = 'http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#'

ONTODOE = 'https://www.theworldavatar.com/kg/ontodoe/'
ONTOREACTION = 'https://www.theworldavatar.com/kg/ontoreaction/'
ONTOKIN = 'http://www.theworldavatar.com/ontology/ontokin/OntoKin.owl#'
ONTOSPECIES = 'http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#'
ONTOLAB = 'https://www.theworldavatar.com/kg/ontolab/'
ONTOVAPOURTEC = 'https://www.theworldavatar.com/kg/ontovapourtec/'
ONTOHPLC = 'https://www.theworldavatar.com/kg/ontohplc/'
ONTOBPR = 'https://www.theworldavatar.com/kg/ontobpr/'


###--- Some IRIs from OM ---###
OM_CELSIUSTEMPERATURE = UNITS_OF_MEASURE + 'CelsiusTemperature'
OM_MINUTETIME = UNITS_OF_MEASURE + 'minute-Time'
OM_MASS = UNITS_OF_MEASURE + 'Mass'
OM_DENSITY = UNITS_OF_MEASURE + 'Density'
OM_SPECIFICAMOUNTOFMONEY = UNITS_OF_MEASURE + 'SpecificAmountOfMoney'
OM_AMOUNTOFSUBSTANCE = UNITS_OF_MEASURE + 'AmountOfSubstance'
OM_MOLE = UNITS_OF_MEASURE + 'mole'
OM_MOLEPERLITRE = UNITS_OF_MEASURE + 'molePerLitre'
OM_LITRE = UNITS_OF_MEASURE + 'litre'
OM_KILOGRAMPERLITRE = UNITS_OF_MEASURE + 'kilogramPerLitre'
OM_KILOGRAM = UNITS_OF_MEASURE + 'kilogram'
OM_KILOGRAMPERMOLE = UNITS_OF_MEASURE + 'kilogramPerMole'
OM_MILLILITRE = UNITS_OF_MEASURE + 'millilitre'
OM_ONE = UNITS_OF_MEASURE + 'one'
OM_DEGREECELSIUS = UNITS_OF_MEASURE + 'degreeCelsius'
OM_DEGREEFAHRENHEIT = UNITS_OF_MEASURE + 'degreeFahrenheit'
OM_KELVIN = UNITS_OF_MEASURE + 'kelvin'
OM_POUNDSTERLING = UNITS_OF_MEASURE + 'poundSterling'
OM_POUNDSTERLINGPERKILOGRAM = UNITS_OF_MEASURE + 'poundSterlingPerKilogram'
OM_AMOUNTOFMONEY = UNITS_OF_MEASURE + 'AmountOfMoney'
OM_BODYMASS = UNITS_OF_MEASURE + 'BodyMass'
OM_HEIGHT = UNITS_OF_MEASURE + 'Height'
OM_LENGTH = UNITS_OF_MEASURE + 'Length'
OM_QUANTITY = UNITS_OF_MEASURE + 'Quantity'
OM_WIDTH = UNITS_OF_MEASURE + 'Width'
OM_DIAMETER = UNITS_OF_MEASURE + 'Diameter'
OM_VOLUME = UNITS_OF_MEASURE + 'Volume'
OM_VOLUMETRICFLOWRATE = UNITS_OF_MEASURE + 'VolumetricFlowRate'
OM_COMMONLYHASUNIT = UNITS_OF_MEASURE + 'commonlyHasUnit'
OM_DURATION = UNITS_OF_MEASURE + 'Duration'
OM_QUANTITYOFDIMENSIONONE = UNITS_OF_MEASURE + 'QuantityOfDimensionOne'
OM_PERCENT = UNITS_OF_MEASURE + 'percent'

##--- Some additional unit IRIs added in cambridge-cares/OM ---##
OM_GRAMPERMOLE = UNITS_OF_MEASURE + 'gramPerMole'
OM_POUNDSTERLINGPERLITRE = UNITS_OF_MEASURE + 'poundSterlingPerLitre'
OM_KILOGRAMPERLITREPERMINUTE = UNITS_OF_MEASURE + 'kilogramPerLitrePerMinute-Time'
OM_GRAMPERLITREPERHOUR = UNITS_OF_MEASURE + 'gramPerLitrePerHour'
OM_MILLIABSORBANCEUNITMULTIPLIESMINUTE = UNITS_OF_MEASURE + 'milliAbsorbanceUnitMinute-Time'
OM_MILLILITREPERMINUTETIME = UNITS_OF_MEASURE + 'millilitrePerMinute-Time'


###--- Some IRIs from DBPEDIA ---###
DBPEDIA_XLSFILE = 'http://dbpedia.org/resource/Microsoft_Excel' # NOTE: <DBPEDIA_XLSFILE> <rdf:type> <DBPEDIA_WIKICATFILENAMEEXTENSIONS>.
DBPEDIA_CSVFILE = 'http://dbpedia.org/resource/Comma-separated_values' # NOTE: <DBPEDIA_CSVFILE> <rdf:type> <DBPEDIA_WIKICATFILENAMEEXTENSIONS>.
DBPEDIA_TXTFILE = 'http://dbpedia.org/resource/Text_file' # NOTE: <DBPEDIA_TXTFILE> <rdf:type> <DBPEDIA_WIKICATFILENAMEEXTENSIONS>.
DBPEDIA_WIKICATFILENAMEEXTENSIONS = 'http://dbpedia.org/class/yago/WikicatFilenameExtensions'
DBPEDIA_MANUFACTURER = DBPEDIA + 'manufacturer'
DBPEDIA_ORGANISATION = DBPEDIA + 'Organisation'


###--- Some IRIs from SAREF ---###
SAREF_CONSISTSOF = SAREF + 'consistsOf'
SAREF_DEVICE = SAREF + 'Device'
SAREF_COMMAND = SAREF + 'Command'
SAREF_ACCOMPLISHES = SAREF + 'accomplishes'
SAREF_ACTSUPON = SAREF + 'actsUpon'
SAREF_HASCOMMAND = SAREF + 'hasCommand'
SAREF_HASFUNCTION = SAREF + 'hasFunction'
SAREF_HASSTATE = SAREF + 'hasState'
SAREF_ISACCOMPLISHEDBY = SAREF + 'isAccomplishedBy'
SAREF_ISCOMMANDOF = SAREF + 'isCommandOf'
SAREF_ISOFFEREDBY = SAREF + 'isOfferedBy'
SAREF_OFFERS = SAREF + 'offers'
SAREF_REPRESENTS = SAREF + 'represents'
SAREF_FUNCTION = SAREF + 'Function'
SAREF_TASK = SAREF + 'Task'
SAREF_SERVICE = SAREF + 'Service'
SAREF_STATE = SAREF + 'State'


###--- Some IRIs from OntoCAPE ---###
ONTOCAPE_SCALARVALUE = ONTOCAPE_SYSTEM + 'ScalarValue'
ONTOCAPE_HASVALUE = ONTOCAPE_SYSTEM + 'hasValue'
ONTOCAPE_SOLID = ONTOCAPE_PHASESYSTEM + 'solid'
ONTOCAPE_LIQUID = ONTOCAPE_PHASESYSTEM + 'liquid'
ONTOCAPE_GASEOUS = ONTOCAPE_PHASESYSTEM + 'gaseous'
ONTOCAPE_PREDEFINED_PHASE = [ONTOCAPE_SOLID, ONTOCAPE_LIQUID, ONTOCAPE_GASEOUS]
ONTOCAPE_HASUNITOFMEASURE = ONTOCAPE_SYSTEM + 'hasUnitOfMeasure'
ONTOCAPE_NUMERICALVALUE = ONTOCAPE_SYSTEM + 'numericalValue'
ONTOCAPE_CHEMICALREACTION = ONTOCAPE_REACTIONMECHANISM + 'ChemicalReaction'
ONTOCAPE_SINGLEPHASE = ONTOCAPE_PHASESYSTEM + 'SinglePhase'
ONTOCAPE_STATEOFAGGREGATION = ONTOCAPE_PHASESYSTEM + 'StateOfAggregation'
ONTOCAPE_PHASECOMPONENT = ONTOCAPE_PHASESYSTEM + 'PhaseComponent'
ONTOCAPE_COMPOSITION = ONTOCAPE_PHASESYSTEM + 'Composition'
ONTOCAPE_PHASECOMPONENTCONCENTRATION = ONTOCAPE_PHASESYSTEM + 'PhaseComponentConcentration'
ONTOCAPE_PHYSICALCONTEXT = ONTOCAPE_PHASESYSTEM + 'PhysicalContext'
ONTOCAPE_VOLUMEBASEDCONCENTRATION = ONTOCAPE_PHASESYSTEM + 'Volume-BasedConcentration'
ONTOCAPE_MOLARITY = ONTOCAPE_PHASESYSTEM + 'Molarity'
ONTOCAPE_CHEMICALSPECIES = ONTOCAPE_SUBSTANCE + 'ChemicalSpecies'
ONTOCAPE_CATALYST = ONTOCAPE_REACTIONMECHANISM + 'hasCatalyst'
ONTOCAPE_REPRESENTSTHERMODYNAMICBEHAVIOROF = ONTOCAPE_MATERIAL + 'representsThermodynamicBehaviorOf'
ONTOCAPE_THERMODYNAMICBEHAVIOR = ONTOCAPE_MATERIAL + 'thermodynamicBehavior'
ONTOCAPE_HASSTATEOFAGGREGATION = ONTOCAPE_PHASESYSTEM + 'hasStateOfAggregation'
ONTOCAPE_ISCOMPOSEDOFSUBSYSTEM = ONTOCAPE_SYSTEM + 'isComposedOfSubsystem'
ONTOCAPE_HAS_COMPOSITION = ONTOCAPE_PHASESYSTEM + 'has_composition'
ONTOCAPE_COMPRISESDIRECTLY = ONTOCAPE_SYSTEM + 'comprisesDirectly'
ONTOCAPE_HAS_PHYSICAL_CONTEXT = ONTOCAPE_PHASESYSTEM + 'has_physical_context'
ONTOCAPE_HASPROPERTY = ONTOCAPE_SYSTEM + 'hasProperty'
ONTOCAPE_REPRESENTSOCCURENCEOF = ONTOCAPE_PHASESYSTEM + 'representsOccurenceOf'
ONTOCAPE_REFERSTOMATERIAL = ONTOCAPE_BEHAVIOR + 'refersToMaterial'
ONTOCAPE_MATERIALAMOUNT = ONTOCAPE_BEHAVIOR + 'MaterialAmount'
ONTOCAPE_MATERIAL = ONTOCAPE_MATERIAL + 'Material'
ONTOCAPE_HASREACTANT = ONTOCAPE_REACTIONMECHANISM + 'hasReactant'
ONTOCAPE_HASPRODUCT = ONTOCAPE_REACTIONMECHANISM + 'hasProduct'


###--- Some IRIs from OntoKin ---###
ONTOKIN_SPECIES = ONTOKIN + 'Species'
ONTOKIN_REACTANT = ONTOKIN + 'Reactant'
ONTOKIN_PRODUCT = ONTOKIN + 'Product'

###--- Some IRIs from OntoSpecies ---###
ONTOSPECIES_HASUNIQUESPECIES = ONTOSPECIES + 'hasUniqueSpecies'
ONTOSPECIES_HASMOLECULARWEIGHT = ONTOSPECIES + 'hasMolecularWeight'
ONTOSPECIES_UNITS = ONTOSPECIES + 'units'
ONTOSPECIES_VALUE = ONTOSPECIES + 'value'
ONTOSPECIES_SPECIES = ONTOSPECIES + 'Species'


###--- IRIs for OntoDoE ---###
ONTODOE_DESIGNOFEXPERIMENT = ONTODOE + 'DesignOfExperiment'
ONTODOE_STRATEGY = ONTODOE + 'Strategy'
ONTODOE_TSEMO = ONTODOE + 'TSEMO'
ONTODOE_LHS = ONTODOE + 'LHS'
ONTODOE_CRITERION = ONTODOE + 'Criterion'
ONTODOE_CENTER = ONTODOE + 'Center'
ONTODOE_MAXIMUM = ONTODOE + 'Maximum'
ONTODOE_CENTERMAXIMUM = ONTODOE + 'CenterMaximum'
ONTODOE_CORRELATION = ONTODOE + 'Correlation'
ONTODOE_DOMAIN = ONTODOE + 'Domain'
ONTODOE_DESIGNVARIABLE = ONTODOE + 'DesignVariable'
ONTODOE_CONTINUOUSVARIABLE = ONTODOE + 'ContinuousVariable'
ONTODOE_CATEGORICALVARIABLE = ONTODOE + 'CategoricalVariable'
ONTODOE_FIXEDPARAMETER = ONTODOE + 'FixedParameter'
ONTODOE_SYSTEMRESPONSE = ONTODOE + 'SystemResponse'
ONTODOE_HISTORICALDATA = ONTODOE + 'HistoricalData'
ONTODOE_NEWEXPERIMENT = ONTODOE + 'NewExperiment'
ONTODOE_USESSTRATEGY = ONTODOE + 'usesStrategy'
ONTODOE_HASDOMAIN = ONTODOE + 'hasDomain'
ONTODOE_HASDESIGNVARIABLE = ONTODOE + 'hasDesignVariable'
ONTODOE_HASSYSTEMRESPONSE = ONTODOE + 'hasSystemResponse'
ONTODOE_UTILISESHISTORICALDATA = ONTODOE + 'utilisesHistoricalData'
ONTODOE_PROPOSESNEWEXPERIMENT = ONTODOE + 'proposesNewExperiment'
ONTODOE_REFERSTO = ONTODOE + 'refersTo'
ONTODOE_UPPERLIMIT = ONTODOE + 'upperLimit'
ONTODOE_LOWERLIMIT = ONTODOE + 'lowerLimit'
ONTODOE_POSITIONALID = ONTODOE + 'positionalID'
ONTODOE_MAXIMISE = ONTODOE + 'maximise'
ONTODOE_NUMOFNEWEXP = ONTODOE + 'numOfNewExp'
ONTODOE_NRETRIES = ONTODOE + 'nRetries'
ONTODOE_NSPECTRALPOINTS = ONTODOE + 'nSpectralPoints'
ONTODOE_NGENERATIONS = ONTODOE + 'nGenerations'
ONTODOE_POPULATIONSIZE = ONTODOE + 'populationSize'
ONTODOE_SEED = ONTODOE + 'seed'
ONTODOE_HASFIXEDPARAMETER = ONTODOE + 'hasFixedParameter'
ONTODOE_HASDOETEMPLATE = ONTODOE + 'hasDoETemplate'
ONTODOE_DESIGNSCHEMICALREACTION = ONTODOE + 'designsChemicalReaction'

###--- IRIs for ONTOREACTION ---###
ONTOREACTION_REACTIONEXPERIMENT = ONTOREACTION + 'ReactionExperiment'
ONTOREACTION_REACTIONVARIATION = ONTOREACTION + 'ReactionVariation'
ONTOREACTION_CATALYST = ONTOREACTION + 'Catalyst'
ONTOREACTION_SOLVENT = ONTOREACTION + 'Solvent'
ONTOREACTION_TARGETPRODUCT = ONTOREACTION + 'TargetProduct'
ONTOREACTION_IMPURITY = ONTOREACTION + 'Impurity'
ONTOREACTION_PERFORMANCEINDICATOR = ONTOREACTION + 'PerformanceIndicator'
ONTOREACTION_ENVIRONMENTALFACTOR = ONTOREACTION + 'EnvironmentalFactor'
ONTOREACTION_YIELD = ONTOREACTION + 'Yield'
ONTOREACTION_ECOSCORE = ONTOREACTION + 'EcoScore'
ONTOREACTION_CONVERSION = ONTOREACTION + 'Conversion'
ONTOREACTION_SPACETIMEYIELD = ONTOREACTION + 'SpaceTimeYield'
ONTOREACTION_RUNMATERIALCOST = ONTOREACTION + 'RunMaterialCost'
ONTOREACTION_RUNMATERIALCOSTPERKILOGRAMPRODUCT = ONTOREACTION + 'RunMaterialCostPerKilogramProduct'
ONTOREACTION_REACTIONCONDITION = ONTOREACTION + 'ReactionCondition'
ONTOREACTION_RESIDENCETIME = ONTOREACTION + 'ResidenceTime'
ONTOREACTION_REACTIONTEMPERATURE = ONTOREACTION + 'ReactionTemperature'
ONTOREACTION_REACTIONPRESSURE = ONTOREACTION + 'ReactionPressure'
ONTOREACTION_STOICHIOMETRYRATIO = ONTOREACTION + 'StoichiometryRatio'
ONTOREACTION_REACTIONSCALE = ONTOREACTION + 'ReactionScale'
ONTOREACTION_CHEMICAL = ONTOREACTION + 'Chemical'
ONTOREACTION_INPUTCHEMICAL = ONTOREACTION + 'InputChemical'
ONTOREACTION_OUTPUTCHEMICAL = ONTOREACTION + 'OutputChemical'
ONTOREACTION_HASVARIATION = ONTOREACTION + 'hasVariation'
ONTOREACTION_ISVARIATIONOF = ONTOREACTION + 'isVariationOf'
ONTOREACTION_ISASSIGNEDTO = ONTOREACTION + 'isAssignedTo'
ONTOREACTION_HASEQUIPMENTSETTINGS = ONTOREACTION + 'hasEquipmentSettings'
ONTOREACTION_ISOCCURENCEOF = ONTOREACTION + 'isOccurenceOf'
ONTOREACTION_ISREALISEDAS = ONTOREACTION + 'isRealisedAs'
ONTOREACTION_HASSOLVENT = ONTOREACTION + 'hasSolvent'
ONTOREACTION_HASRXNTYPE = ONTOREACTION + 'hasRxnType'
ONTOREACTION_HASPERFORMANCEINDICATOR = ONTOREACTION + 'hasPerformanceIndicator'
ONTOREACTION_HASENVIRONMENTALFACTOR = ONTOREACTION + 'hasEnvironmentalFactor'
ONTOREACTION_HASYIELD = ONTOREACTION + 'hasYield'
ONTOREACTION_HASECOSCORE = ONTOREACTION + 'hasEcoScore'
ONTOREACTION_HASCONVERSION = ONTOREACTION + 'hasConversion'
ONTOREACTION_HASSPACETIMEYIELD = ONTOREACTION + 'hasSpaceTimeYield'
ONTOREACTION_HASRUNMATERIALCOST = ONTOREACTION + 'hasRunMaterialCost'
ONTOREACTION_HASRUNMATERIALCOSTPERKILOGRAMPRODUCT = ONTOREACTION + 'hasRunMaterialCostPerKilogramProduct'
ONTOREACTION_HASREACTIONCONDITION = ONTOREACTION + 'hasReactionCondition'
ONTOREACTION_HASRESTIME = ONTOREACTION + 'hasResTime'
ONTOREACTION_HASRXNTEMPERATURE = ONTOREACTION + 'hasRxnTemperature'
ONTOREACTION_HASRXNPRESSURE = ONTOREACTION + 'hasRxnPressure'
ONTOREACTION_HASSTOICHIOMETRYRATIO = ONTOREACTION + 'hasStoichiometryRatio'
ONTOREACTION_HASRXNSCALE = ONTOREACTION + 'hasRxnScale'
ONTOREACTION_INDICATESMULTIPLICITYOF = ONTOREACTION + 'indicatesMultiplicityOf'
ONTOREACTION_INDICATESUSAGEOF = ONTOREACTION + 'indicatesUsageOf'
ONTOREACTION_HASINPUTCHEMICAL = ONTOREACTION + 'hasInputChemical'
ONTOREACTION_HASOUTPUTCHEMICAL = ONTOREACTION + 'hasOutputChemical'
ONTOREACTION_HASRINCHI = ONTOREACTION + 'hasRInChI'
ONTOREACTION_HASRDFILE = ONTOREACTION + 'hasRDFILE'
ONTOREACTION_CDXML = ONTOREACTION + 'cdXML'
ONTOREACTION_RXNSMILES = ONTOREACTION + 'rxnSMILES'
ONTOREACTION_RXNCXSMILES = ONTOREACTION + 'rxnCXSMILES'
ONTOREACTION_YIELDLIMITINGSPECIES = ONTOREACTION + 'yieldLimitingSpecies'


###--- IRIs for OntoLab ---###
ONTOLAB_ISMANAGEDBY = ONTOLAB + "isManagedBy"
ONTOLAB_CONTAINS = ONTOLAB + 'contains'
ONTOLAB_HASARGUMENT = ONTOLAB + 'hasArgument'
ONTOLAB_HASHEIGHT = ONTOLAB + 'hasHeight'
ONTOLAB_HASLENGTH = ONTOLAB + 'hasLength'
ONTOLAB_HASPOWERSUPPLY = ONTOLAB + 'hasPowerSupply'
ONTOLAB_HASPRICE = ONTOLAB + 'hasPrice'
ONTOLAB_HASQUANTITY = ONTOLAB + 'hasQuantity'
ONTOLAB_HASSETTING = ONTOLAB + 'hasSetting'
ONTOLAB_HASWEIGHT = ONTOLAB + 'hasWeight'
ONTOLAB_HASWIDTH = ONTOLAB + 'hasWidth'
ONTOLAB_ISCONTAINEDIN = ONTOLAB + 'isContainedIn'
ONTOLAB_ISPREPAREDBY = ONTOLAB + 'isPreparedBy'
ONTOLAB_ISSPECIFIEDBY = ONTOLAB + 'isSpecifiedBy'
ONTOLAB_SPECIFIES = ONTOLAB + 'specifies'
ONTOLAB_HASARGSTR = ONTOLAB + 'hasArgStr'
ONTOLAB_HASCMDSTR = ONTOLAB + 'hasCmdStr'
ONTOLAB_ARGUMENT = ONTOLAB + 'Argument'
ONTOLAB_CHEMICALAMOUNT = ONTOLAB + 'ChemicalAmount'
ONTOLAB_PREPARATIONMETHOD = ONTOLAB + 'PreparationMethod'
ONTOLAB_DRIED = ONTOLAB + 'Dried'
ONTOLAB_DURATIONSETTING = ONTOLAB + 'DurationSetting'
ONTOLAB_PARAMETERSETTING = ONTOLAB + 'ParameterSetting'
ONTOLAB_EQUIPMENTSETTINGS = ONTOLAB + 'EquipmentSettings'
ONTOLAB_LABEQUIPMENT = ONTOLAB + 'LabEquipment'
ONTOLAB_EXTERNALBATTERY = ONTOLAB + 'ExternalBattery'
ONTOLAB_EXTERNALDC = ONTOLAB + 'ExternalDC'
ONTOLAB_FLOWRATESETTING = ONTOLAB + 'FlowRateSetting'
ONTOLAB_LABORATORY = ONTOLAB + 'Laboratory'
ONTOLAB_LITHIUMBATTERY = ONTOLAB + 'LithiumBattery'
ONTOLAB_NIMHRECHARGEABLEBATTERY = ONTOLAB + 'NiMHRechargeableBattery'
ONTOLAB_POWERSUPPLY = ONTOLAB + 'PowerSupply'
ONTOLAB_REPURIFIED = ONTOLAB + 'Repurified'
ONTOLAB_SOLARPOWERPACK = ONTOLAB + 'SolarPowerPack'
ONTOLAB_SPARGED = ONTOLAB + 'Sparged'
ONTOLAB_SYNTHESISEDINHOUSE = ONTOLAB + 'SynthesisedInHouse'
ONTOLAB_TEMPERATURESETTING = ONTOLAB + 'TemperatureSetting'
ONTOLAB_USEDASRECEIVED = ONTOLAB + 'UsedAsReceived'
ONTOLAB_VOLUMESETTING = ONTOLAB + 'VolumeSetting'
ONTOLAB_WASGENERATEDFOR = ONTOLAB + 'wasGeneratedFor'
ONTOLAB_TRANSLATESTOPARAMETERSETTING = ONTOLAB + 'translatesToParameterSetting'
ONTOLAB_STATELASTUPDATEDAT = ONTOLAB + 'stateLastUpdatedAt'
ONTOLAB_CHEMICALCONTAINER = ONTOLAB + 'ChemicalContainer'
ONTOLAB_REAGENTBOTTLE = ONTOLAB + 'ReagentBottle'
ONTOLAB_WASTEBOTTLE = ONTOLAB + 'WasteBottle'
ONTOLAB_VIAL = ONTOLAB + 'Vial'
ONTOLAB_ISFILLEDWITH = ONTOLAB + 'isFilledWith'
ONTOLAB_HASFILLLEVEL = ONTOLAB + 'hasFillLevel'
ONTOLAB_HASMAXLEVEL = ONTOLAB + 'hasMaxLevel'
ONTOLAB_HASWARNINGLEVEL = ONTOLAB + 'hasWarningLevel'
ONTOLAB_CONTAINSUNIDENTIFIEDCOMPONENT = ONTOLAB + 'containsUnidentifiedComponent'


###--- IRIs for OntoVapourtec ---###
ONTOVAPOURTEC_HASFLOWRATESETTING = ONTOVAPOURTEC + 'hasFlowRateSetting'
ONTOVAPOURTEC_HASINTERNALDIAMETER = ONTOVAPOURTEC + 'hasInternalDiameter'
ONTOVAPOURTEC_HASREACTORLENGTH = ONTOVAPOURTEC + 'hasReactorLength'
ONTOVAPOURTEC_HASREACTORMATERIAL = ONTOVAPOURTEC + 'hasReactorMaterial'
ONTOVAPOURTEC_HASREACTORTEMPERATURESETTING = ONTOVAPOURTEC + 'hasReactorTemperatureSetting'
ONTOVAPOURTEC_HASREACTORVOLUME = ONTOVAPOURTEC + 'hasReactorVolume'
ONTOVAPOURTEC_HASRESIDENCETIMESETTING = ONTOVAPOURTEC + 'hasResidenceTimeSetting'
ONTOVAPOURTEC_HASSAMPLELOOPVOLUMESETTING = ONTOVAPOURTEC + 'hasSampleLoopVolumeSetting'
ONTOVAPOURTEC_HASSITE = ONTOVAPOURTEC + 'hasSite'
ONTOVAPOURTEC_HOLDS = ONTOVAPOURTEC + 'holds'
ONTOVAPOURTEC_ISHELDIN = ONTOVAPOURTEC + 'isHeldIn'
ONTOVAPOURTEC_PUMPSLIQUIDFROM = ONTOVAPOURTEC + 'pumpsLiquidFrom'
ONTOVAPOURTEC_AUTOSAMPLER = ONTOVAPOURTEC + 'AutoSampler'
ONTOVAPOURTEC_AUTOSAMPLERSITE = ONTOVAPOURTEC + 'AutoSamplerSite'
ONTOVAPOURTEC_AUTOSAMPLERFUNCTION = ONTOVAPOURTEC + 'AutoSamplerFunction'
ONTOVAPOURTEC_AUTOSAMPLERCOMMAND = ONTOVAPOURTEC + 'AutoSamplerCommand'
ONTOVAPOURTEC_AUTOSAMPLERTASK = ONTOVAPOURTEC + 'AutoSamplerTask'
ONTOVAPOURTEC_CLEANREACTOR = ONTOVAPOURTEC + 'CleanReactor'
ONTOVAPOURTEC_VAPOURTECFUNCTION = ONTOVAPOURTEC + 'VapourtecFunction'
ONTOVAPOURTEC_CLEARREACTIONS = ONTOVAPOURTEC + 'ClearReactions'
ONTOVAPOURTEC_CLEANINGREACTION = ONTOVAPOURTEC + 'CleaningReaction'
ONTOVAPOURTEC_VAPOURTECSTATE = ONTOVAPOURTEC + 'VapourtecState'
ONTOVAPOURTEC_REACTIONCOMPLETED = ONTOVAPOURTEC + 'ReactionCompleted'
ONTOVAPOURTEC_CONNECTTOFLOWCOMMANDER = ONTOVAPOURTEC + 'ConnectToFlowCommander'
ONTOVAPOURTEC_INACTIVE = ONTOVAPOURTEC + 'Inactive'
ONTOVAPOURTEC_CONNECTION = ONTOVAPOURTEC + 'Connection'
ONTOVAPOURTEC_EXPFILEPATH = ONTOVAPOURTEC + 'ExpFilePath'
ONTOVAPOURTEC_FAULTRECOVERY = ONTOVAPOURTEC + 'FaultRecovery'
ONTOVAPOURTEC_FAULTRECOVERYCOMMAND = ONTOVAPOURTEC + 'FaultRecoveryCommand'
ONTOVAPOURTEC_FAULTY = ONTOVAPOURTEC + 'Faulty'
ONTOVAPOURTEC_FLOWCHEMISTRY = ONTOVAPOURTEC + 'FlowChemistry'
ONTOVAPOURTEC_FLOWCOMMANDER = ONTOVAPOURTEC + 'FlowCommander'
ONTOVAPOURTEC_VAPOURTECRS400 = ONTOVAPOURTEC + 'VapourtecRS400'
ONTOVAPOURTEC_GETCOMMAND = ONTOVAPOURTEC + 'GetCommand'
ONTOVAPOURTEC_GETSTATE = ONTOVAPOURTEC + 'GetState'
ONTOVAPOURTEC_IDLE = ONTOVAPOURTEC + 'Idle'
ONTOVAPOURTEC_INITIALISING = ONTOVAPOURTEC + 'Initialising'
ONTOVAPOURTEC_FINALCLEANING = ONTOVAPOURTEC + 'FinalCleaning'
ONTOVAPOURTEC_LAUNCH = ONTOVAPOURTEC + 'Launch'
ONTOVAPOURTEC_STARTFLOWCOMMANDER = ONTOVAPOURTEC + 'StartFlowCommander'
ONTOVAPOURTEC_LOADEXPERIMENT = ONTOVAPOURTEC + 'LoadExperiment'
ONTOVAPOURTEC_RUNREACTOR = ONTOVAPOURTEC + 'RunReactor'
ONTOVAPOURTEC_NULL = ONTOVAPOURTEC + 'Null'
ONTOVAPOURTEC_PUMPSETTINGS = ONTOVAPOURTEC + 'PumpSettings'
ONTOVAPOURTEC_VAPOURTECR2PUMP = ONTOVAPOURTEC + 'VapourtecR2Pump'
ONTOVAPOURTEC_SAMPLELOOPVOLUMESETTING = ONTOVAPOURTEC + 'SampleLoopVolumeSetting'
ONTOVAPOURTEC_REACTORSETTING = ONTOVAPOURTEC + 'ReactorSettings'
ONTOVAPOURTEC_VAPOURTECR4REACTOR = ONTOVAPOURTEC + 'VapourtecR4Reactor'
ONTOVAPOURTEC_REACTORTEMPERATURESETTING = ONTOVAPOURTEC + 'ReactorTemperatureSetting'
ONTOVAPOURTEC_RESIDENCETIMESETTING = ONTOVAPOURTEC + 'ResidenceTimeSetting'
ONTOVAPOURTEC_RUNNINGREACTION = ONTOVAPOURTEC + 'RunningReaction'
ONTOVAPOURTEC_DRYRUNSTATE = ONTOVAPOURTEC + 'DryRunState'
ONTOVAPOURTEC_VAPOURTECR2PUMPFUNCTION = ONTOVAPOURTEC + 'VapourtecR2PumpFunction'
ONTOVAPOURTEC_VAPOURTECR2PUMPCOMMAND = ONTOVAPOURTEC + 'VapourtecR2PumpCommand'
ONTOVAPOURTEC_VAPOURTECR2PUMPTASK = ONTOVAPOURTEC + 'VapourtecR2PumpTask'
ONTOVAPOURTEC_VAPOURTECR4REACTORFUNCTION = ONTOVAPOURTEC + 'VapourtecR4ReactorFunction'
ONTOVAPOURTEC_VAPOURTECR4REACTORCOMMAND = ONTOVAPOURTEC + 'VapourtecR4ReactorCommand'
ONTOVAPOURTEC_VAPOURTECR4REACTORTASK = ONTOVAPOURTEC + 'VapourtecR4ReactorTask'
ONTOVAPOURTEC_COLLECTIONMETHOD = ONTOVAPOURTEC + 'CollectionMethod'
ONTOVAPOURTEC_SINGLERECEPTACLE = ONTOVAPOURTEC + 'SingleReceptacle'
ONTOVAPOURTEC_FRACTIONCOLLECTOR = ONTOVAPOURTEC + 'FractionCollector'
ONTOVAPOURTEC_HASREACTORTEMPERATUREUPPERLIMIT = ONTOVAPOURTEC + 'hasReactorTemperatureUpperLimit'
ONTOVAPOURTEC_HASREACTORTEMPERATURELOWERLIMIT = ONTOVAPOURTEC + 'hasReactorTemperatureLowerLimit'
ONTOVAPOURTEC_LOCATIONID = ONTOVAPOURTEC + 'locationID'
ONTOVAPOURTEC_STOICHIOMETRYRATIOSETTING = ONTOVAPOURTEC + 'StoichiometryRatioSetting'
ONTOVAPOURTEC_HASSTOICHIOMETRYRATIOSETTING = ONTOVAPOURTEC + 'hasStoichiometryRatioSetting'
ONTOVAPOURTEC_VAPOURTECINPUTFILE = ONTOVAPOURTEC + 'VapourtecInputFile'
ONTOVAPOURTEC_HASVAPOURTECINPUTFILE = ONTOVAPOURTEC + 'hasVapourtecInputFile'
ONTOVAPOURTEC_LASTLOCALMODIFIEDAT = ONTOVAPOURTEC + 'lastLocalModifiedAt'
ONTOVAPOURTEC_LASTUPLOADEDAT = ONTOVAPOURTEC + 'lastUploadedAt'
ONTOVAPOURTEC_LOCALFILEPATH = ONTOVAPOURTEC + 'localFilePath'
ONTOVAPOURTEC_REMOTEFILEPATH = ONTOVAPOURTEC + 'remoteFilePath'
ONTOVAPOURTEC_HASREAGENTSOURCE = ONTOVAPOURTEC + 'hasReagentSource'
ONTOVAPOURTEC_SAMPLELOOPVOLUME = ONTOVAPOURTEC + 'sampleLoopVolume'
ONTOVAPOURTEC_HASCOLLECTIONMETHOD = ONTOVAPOURTEC + 'hasCollectionMethod'
ONTOVAPOURTEC_TORECEPTACLE = ONTOVAPOURTEC + 'toReceptacle'


###--- IRIs for OntoHPLC ---###
ONTOHPLC_PEAKAREA = ONTOHPLC + 'PeakArea'
ONTOHPLC_ATRETENTIONTIME = ONTOHPLC + 'atRetentionTime'
ONTOHPLC_UNIDENTIFIED = ONTOHPLC + 'unidentified'
ONTOHPLC_GENERATEDFOR = ONTOHPLC + 'generatedFor'
ONTOHPLC_HASJOB = ONTOHPLC + 'hasJob'
ONTOHPLC_HASPEAKAREA = ONTOHPLC + 'hasPeakArea'
ONTOHPLC_HASREPORT = ONTOHPLC + 'hasReport'
ONTOHPLC_HASRESPONSEFACTOR = ONTOHPLC + 'hasResponseFactor'
ONTOHPLC_HASRETENTIONTIME = ONTOHPLC + 'hasRetentionTime'
ONTOHPLC_INDICATESCOMPONENT = ONTOHPLC + 'indicatesComponent'
ONTOHPLC_RECORDS = ONTOHPLC + 'records'
ONTOHPLC_REFERSTOSPECIES = ONTOHPLC + 'refersToSpecies'
ONTOHPLC_RESPONSEFACTOR = ONTOHPLC + 'ResponseFactor'
ONTOHPLC_RETENTIONTIME = ONTOHPLC + 'RetentionTime'
ONTOHPLC_USESINTERNALSTANDARD = ONTOHPLC + 'usesInternalStandard'
ONTOHPLC_USESMETHOD = ONTOHPLC + 'usesMethod'
ONTOHPLC_HPLCREPORT = ONTOHPLC + 'HPLCReport'
ONTOHPLC_CHROMATOGRAMMEASUREMENT = ONTOHPLC + 'ChromatogramMeasurement'
ONTOHPLC_CHROMATOGRAMMEASUREMENTCOMMAND = ONTOHPLC + 'ChromatogramMeasurementCommand'
ONTOHPLC_CHROMATOGRAMPOINT = ONTOHPLC + 'ChromatogramPoint'
ONTOHPLC_HPLCJOB = ONTOHPLC + 'HPLCJob'
ONTOHPLC_HPLCMETHOD = ONTOHPLC + 'HPLCMethod'
ONTOHPLC_INTERNALSTANDARD = ONTOHPLC + 'InternalStandard'
ONTOHPLC_HIGHPERFORMANCELIQUIDCHROMATOGRAPHY = ONTOHPLC + 'HighPerformanceLiquidChromatography'
ONTOHPLC_LIQUIDCHROMATOGRAPHY = ONTOHPLC + 'LiquidChromatography'
ONTOHPLC_REPORTEXTENSION = ONTOHPLC + 'reportExtension'
ONTOHPLC_CHARACTERISES = ONTOHPLC + 'characterises'
ONTOHPLC_LOCALREPORTDIRECTORY = ONTOHPLC + 'localReportDirectory'
ONTOHPLC_LASTLOCALMODIFIEDAT = ONTOHPLC + 'lastLocalModifiedAt'
ONTOHPLC_LASTUPLOADEDAT = ONTOHPLC + 'lastUploadedAt'
ONTOHPLC_LOCALFILEPATH = ONTOHPLC + 'localFilePath'
ONTOHPLC_REMOTEFILEPATH = ONTOHPLC + 'remoteFilePath'
ONTOHPLC_HASPASTREPORT = ONTOHPLC + 'hasPastReport'
ONTOHPLC_RETENTIONTIMEMATCHTHRESHOLD = ONTOHPLC + 'retentionTimeMatchThreshold'


###--- IRIs for OntoBPR ---###
ONTOBPR_BACKPRESSUREREGULATOR = ONTOBPR + 'BackPressureRegulator'
ONTOBPR_REACTORPRESSURECONTROL = ONTOBPR + 'ReactorPressureControl'
ONTOBPR_PRESSUREADJUSTMENT = ONTOBPR + 'PressureAdjustment'
ONTOBPR_PRESSUREADJUSTMENTCOMMAND = ONTOBPR + 'PressureAdjustmentCommand'


###--- Some IRIs for default units to be used by agents ---###
# NOTE an assumption is that these UNIFIED units can be computed directly from each other without unit conversions
UNIFIED_VOLUME_UNIT = OM_LITRE
UNIFIED_MOLE_UNIT = OM_MOLE
UNIFIED_MOLAR_MASS_UNIT = OM_KILOGRAMPERMOLE
UNIFIED_DENSITY_UNIT = OM_KILOGRAMPERLITRE
UNIFIED_CONCENTRATION_UNIT = OM_MOLEPERLITRE
UNIFIED_COST_UNIT = OM_POUNDSTERLINGPERLITRE
UNIFIED_YIELD_UNIT = OM_PERCENT
UNIFIED_CONVERSION_UNIT = OM_PERCENT
UNIFIED_COST_BASIS_UNIT = OM_POUNDSTERLING
UNIFIED_RUN_MATERIAL_COST_UNIT = OM_POUNDSTERLINGPERLITRE
UNIFIED_RUN_MATERIAL_COST_PER_KILOGRAM_PRODUCT_UNIT = OM_POUNDSTERLINGPERKILOGRAM
UNIFIED_ECOSCORE_UNIT = OM_ONE
UNIFIED_MASS_UNIT = OM_KILOGRAM
UNIFIED_EQ_RATIO_UNIT = OM_ONE
UNIFIED_TIME_UNIT = OM_MINUTETIME
UNIFIED_TEMPERATURE_UNIT = OM_DEGREECELSIUS
UNIFIED_SPACETIMEYIELD_UNIT = OM_KILOGRAMPERLITREPERMINUTE
UNIFIED_ENVIRONMENTFACTOR_UNIT = OM_ONE

# NOTE this unit is added for scaling purposes so that the space-time yield can be visualised in a meaningful way
# Otherwise it's too small that will be cut to 0.00 after rounding the decimal places
SCALED_SPACETIMEYIELD_UNIT = OM_GRAMPERLITREPERHOUR
