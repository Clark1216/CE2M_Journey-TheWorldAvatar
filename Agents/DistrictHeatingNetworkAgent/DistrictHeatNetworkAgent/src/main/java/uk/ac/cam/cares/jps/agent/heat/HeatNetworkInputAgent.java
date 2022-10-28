package uk.ac.cam.cares.jps.agent.heat;

import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesSparql;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.update.UpdateRequest;
import org.jooq.exception.IOException;
import org.apache.jena.graph.NodeFactory;
import org.apache.logging.log4j.Logger;
import org.jooq.exception.DataAccessException;
import uk.ac.cam.cares.jps.base.util.JSONKeyToIRIMapper;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;

public class HeatNetworkInputAgent {

    // Common Base URLs
    public static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public static String XSD = "http://www.w3.org/2001/XMLSchema#";
    public static String OM = "http://www.ontology-of-units-of-measure.org/resource/om-2/";
    public static String OHN = "https://www.theworldavatar.com/kg/ontoheatnetwork/";
    public static String EMS = "https://www.theworldavatar.com/kg/ontoems/";
    public static String TS = "https://www.theworldavatar.com/kg/ontotimeseries/";
    public static String KB = "https://www.theworldavatar.com/kb/ontoheatnetwork/";

    // IRIs for OntoHeatNet and others
    public static String OC_HAS_PART = "http://www.theworldavatar.com/ontology/meta_model/mereology/mereology.owl#hasPart";
    public static String OC_HAS_COST = "http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_performance/economic_performance.owl#hasCost";
    public static String OC_isOwnerOf = "http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#isOwnerOf";
    public static String OC_hasRevenue = "http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_performance/economic_performance.owl#hasRevenue";
    public static String OCP_hasCO2emission = "http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#hasIndividualCO2Emission";
    public static String OCP_hasFuelType = "http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#hasFuelType";
    public static String OCP_FuelType = "http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#FuelType";

    // OntoTimeSeries
    public static String TS_TIMESERIES = TS + "TimeSeries";
    public static String TS_HAS_TIMESERIES = TS + "hasTimeSeries";
    public static String TS_HAS_TIME_UNIT = TS + "hasTimeUnit";
    public static String TS_HAS_RDB = TS + "hasRDB";

    // For units of measure
    public static String OM_QUANTITY = OM + "Quantity";
    public static String OM_MEASURE = OM + "Measure";
    public static String OM_UNIT = OM + "Unit";
    public static String OM_HAS_VALUE = OM + "hasValue";
    public static String OM_HAS_UNIT = OM + "hasUnit";
    public static String OM_SYMBOL = OM + "symbol";
    public static String OM_Energy = OM + "Energy";
    public static String OM_POWER = OM + "Power";
    public static String OM_DURATION = OM + "Duration";
    public static String OM_VFR = OM + "VolumetricFlowRate";
    public static String OM_Has_NUMERICAL_VALUE = OM + "hasNumericalValue";
    public static String OM_DENSITY = OM + "Density";
    public static String OM_TEMPERATURE = OM + "Temperature";
    public static String OM_PRESSURE = OM + "Pressure";
    public static String OM_SHC = OM + "SpecificHeatCapacity";

    // Data types
    public static String RDF_TYPE = RDF + "type";
    public static String RDFS_COMMENT = RDFS + "comment";
    public static String RDFS_LABEL = RDFS + "label";
    public static String XSD_STRING = XSD + "string";
    public static String XSD_FLOAT = XSD + "float";
    public static String XSD_DATE = XSD + "date";
    public static String XSD_BOOLEAN = XSD + "Boolean";

    private TimeSeriesClient<OffsetDateTime> tsClient;
    private static List<JSONKeyToIRIMapper> mappings;
    public static final String timestampKey = "obsTimeUtc";
    public static final String generatedIRIPrefix = TimeSeriesSparql.ns_kb + "heat";
    public static final String timeUnit = OffsetDateTime.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(HeatNetworkInputAgentLauncher.class);

    public void dataInstantiation() {

        String staticData = System.getenv("CSVPATH");
        String[] numericValue = ReadColInput(1,staticData,",");

        // static data
        float Value_VolumetricFlowRate = Float.parseFloat(numericValue[0]);
        float Value_RatedElectricalPower = Float.parseFloat(numericValue[1]);
        float Value_MinimumThermalLoad = Float.parseFloat(numericValue[2]);
        float Value_MinimumIdleTime = Float.parseFloat(numericValue[3]);
        float Value_MinPurchase = Float.parseFloat(numericValue[4]);
        float Value_MaxPurchase = Float.parseFloat(numericValue[5]);
        float Value_TierOneCap = Float.parseFloat(numericValue[6]);
        float Value_TierTwoCap = Float.parseFloat(numericValue[7]);
        float Value_TierOneUnitPrice = Float.parseFloat(numericValue[8]);
        float Value_TierTwoUnitPrice = Float.parseFloat(numericValue[9]);
        float Value_ThermalLoad_GT = Float.parseFloat(numericValue[10]);
        float Value_HCV_GT = Float.parseFloat(numericValue[11]);
        float Value_LCV_GT = Float.parseFloat(numericValue[12]);
        float Value_CO2Factor_GT = Float.parseFloat(numericValue[13]);
        float Value_ThermalLoad_HB4 = Float.parseFloat(numericValue[14]);
        float Value_HCV_HB4 = Float.parseFloat(numericValue[15]);
        float Value_LCV_HB4 = Float.parseFloat(numericValue[16]);
        float Value_CO2Factor_HB4 = Float.parseFloat(numericValue[17]);
        float Value_ThermalLoad_HB5 = Float.parseFloat(numericValue[18]);
        float Value_HCV_HB5 = Float.parseFloat(numericValue[19]);
        float Value_LCV_HB5 = Float.parseFloat(numericValue[20]);
        float Value_CO2Factor_HB5 = Float.parseFloat(numericValue[21]);
        float Value_ThermalLoad_HB6 = Float.parseFloat(numericValue[22]);
        float Value_HCV_HB6 = Float.parseFloat(numericValue[23]);
        float Value_LCV_HB6 = Float.parseFloat(numericValue[24]);
        float Value_CO2Factor_HB6 = Float.parseFloat(numericValue[25]);


        // For the Consumer part
        UpdateBuilder Consumer_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "Consumer"), NodeFactory.createURI(OHN + "hasHeatDemand"), NodeFactory.createURI(KB + "EnergyInTimeInterval" + "Consumer"))
                        .addInsert(NodeFactory.createURI(KB + "Consumer"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "Consumer"))
                        .addInsert(NodeFactory.createURI(KB + "EnergyInTimeInterval" + "Consumer"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EnergyInTimeInterva"));
        UpdateRequest Consumer_ur = Consumer_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", Consumer_ur.toString());
        omHasValueTS("EnergyInTimeInterval" + "Consumer", "MegaWattHour");


        // For the Heating Network part
        UpdateBuilder HeatingNetwork_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OHN + "suppliesHeatTo"), NodeFactory.createURI(KB + "Consumer"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatingNetwork"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(RDFS_LABEL), "HeatingNetwork")
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OC_HAS_PART), NodeFactory.createURI(KB + "GridConnectionUPMunicipal"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OC_HAS_PART), NodeFactory.createURI(KB + "GridConnectionDownMunicipal"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OC_HAS_PART), NodeFactory.createURI(KB + "GridConnectionUPEfW"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OC_HAS_PART), NodeFactory.createURI(KB + "GridConnectionDownEfW"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPMunicipal"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GridConnection"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownMunicipal"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GridConnection"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPEfW"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GridConnection"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownEfW"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GridConnection"))
                        .addInsert(NodeFactory.createURI(KB + "HeatingNetwork"), NodeFactory.createURI(OHN + "hasMinFlowRate"), NodeFactory.createURI(KB + "VolumetricFlowRate"))
                        .addInsert(NodeFactory.createURI(KB + "VolumetricFlowRate"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_VFR));
        UpdateRequest HeatingNetwork_ur = HeatingNetwork_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", Consumer_ur.toString());
        omHasValueNonTS("VolumetricFlowRate", "CubicMeterPerHour", Value_VolumetricFlowRate);


        // For GridConnection part
        UpdateBuilder GridConnection_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPMunicipal" + "SHC"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownMunicipal" + "SHC"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPEfW" + "SHC"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownEfW" + "SHC"))
                        .addInsert(NodeFactory.createURI(KB + "UPMunicipal" + "SHC"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_SHC))
                        .addInsert(NodeFactory.createURI(KB + "DownMunicipal" + "SHC"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_SHC))
                        .addInsert(NodeFactory.createURI(KB + "UPEfW" + "SHC"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_SHC))
                        .addInsert(NodeFactory.createURI(KB + "DownEfW" + "SHC"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_SHC))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPMunicipal" + "Density"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownMunicipal" + "Density"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPEfW" + "Density"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownEfW" + "Density"))
                        .addInsert(NodeFactory.createURI(KB + "UPMunicipal" + "Density"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DENSITY))
                        .addInsert(NodeFactory.createURI(KB + "DownMunicipal" + "Density"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DENSITY))
                        .addInsert(NodeFactory.createURI(KB + "UPEfW" + "Density"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DENSITY))
                        .addInsert(NodeFactory.createURI(KB + "DownEfW" + "Density"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DENSITY))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPMunicipal" + "Pressure"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownMunicipal" + "Pressure"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPEfW" + "Pressure"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownEfW" + "Pressure"))
                        .addInsert(NodeFactory.createURI(KB + "UPMunicipal" + "Pressure"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_PRESSURE))
                        .addInsert(NodeFactory.createURI(KB + "DownMunicipal" + "Pressure"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_PRESSURE))
                        .addInsert(NodeFactory.createURI(KB + "UPEfW" + "Pressure"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_PRESSURE))
                        .addInsert(NodeFactory.createURI(KB + "DownEfW" + "Pressure"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_PRESSURE))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPMunicipal" + "Temperature"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownMunicipal"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownMunicipal" + "Temperature"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionUPEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "UPEfW" + "Temperature"))
                        .addInsert(NodeFactory.createURI(KB + "GridConnectionDownEfW"), NodeFactory.createURI(OHN + "hasObservableProperties"), NodeFactory.createURI(KB + "DownEfW" + "Temperature"))
                        .addInsert(NodeFactory.createURI(KB + "UPMunicipal" + "Temperature"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_TEMPERATURE))
                        .addInsert(NodeFactory.createURI(KB + "DownMunicipal" + "Temperature"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_TEMPERATURE))
                        .addInsert(NodeFactory.createURI(KB + "UPEfW" + "Temperature"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_TEMPERATURE))
                        .addInsert(NodeFactory.createURI(KB + "DownEfW" + "Temperature"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_TEMPERATURE));
        UpdateRequest GridConnection_ur = GridConnection_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", GridConnection_ur.toString());
        omHasValueTS("UPMunicipal" + "SHC", "JoulePerKelvinPerKilogram");
        omHasValueTS("DownMunicipal" + "SHC", "JoulePerKelvinPerKilogram");
        omHasValueTS("UPEfW" + "SHC", "JoulePerKelvinPerKilogram");
        omHasValueTS("DownEfW" + "SHC", "JoulePerKelvinPerKilogram");
        omHasValueTS("UPMunicipal" + "Density", "KilogramPerCubicMeter");
        omHasValueTS("DownMunicipal" + "Density", "KilogramPerCubicMeter");
        omHasValueTS("UPEfW" + "Density", "KilogramPerCubicMeter");
        omHasValueTS("DownEfW" + "Density", "KilogramPerCubicMeter");
        omHasValueTS("UPMunicipal" + "Pressure", "Bar");
        omHasValueTS("DownMunicipal" + "Pressure", "Bar");
        omHasValueTS("UPEfW" + "Pressure", "Bar");
        omHasValueTS("DownEfW" + "Pressure", "Bar");
        omHasValueTS("UPMunicipal" + "Temperature", "DegreeCelsius");
        omHasValueTS("DownMunicipal" + "Temperature", "DegreeCelsius");
        omHasValueTS("UPEfW" + "Temperature", "DegreeCelsius");
        omHasValueTS("DownEfW" + "Temperature", "DegreeCelsius");


        // For the HeatProvider part
        UpdateBuilder HeatProvider_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(OHN + "providesHeatTo"), NodeFactory.createURI(KB + "HeatingNetwork"))
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatProvider"))
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(OHN + "hasUpstreamGridConnection"), NodeFactory.createURI(KB + "GridConnectionUPMunicipal"))
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(OHN + "hasUpstreamGridConnection"), NodeFactory.createURI(KB + "GridConnectionUPEfW"))
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(OHN + "hasDownstreamGridConnection"), NodeFactory.createURI(KB + "GridConnectionDownMunicipal"))
                        .addInsert(NodeFactory.createURI(KB + "HeatProvider"), NodeFactory.createURI(OHN + "hasDownstreamGridConnection"), NodeFactory.createURI(KB + "GridConnectionDownEfW"));
        UpdateRequest HeatProvider_ur = HeatProvider_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", HeatProvider_ur.toString());


        // For the MunicipalUtility part
        UpdateBuilder MunicipalUtility_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OHN + "operates"), NodeFactory.createURI(KB + "HeatingNetwork"))
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "MunicipalUtility"))
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(RDFS_LABEL), "MunicipalUtility")
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OC_isOwnerOf), NodeFactory.createURI(KB + "ThermalEnergyStorage"))
                        .addInsert(NodeFactory.createURI(KB + "ThermalEnergyStorage"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI("http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#ThermalEnergyStorage"))
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OHN + "hasPurchaseAgreement"), NodeFactory.createURI(KB + "Contract"))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI("https://spec.edmcouncil.org/fibo/ontology/FND/Agreements/Contracts/Contract"))
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OC_isOwnerOf), NodeFactory.createURI(KB + "HeatGeneratorGT"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GasTurbine"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(RDFS_LABEL), "GasTurbine")
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OC_isOwnerOf), NodeFactory.createURI(KB + "HeatGeneratorBoil4"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil4"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatBoiler"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil4"), NodeFactory.createURI(RDFS_LABEL), "Boil4")
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OC_isOwnerOf), NodeFactory.createURI(KB + "HeatGeneratorBoil5"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil5"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatBoiler"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil5"), NodeFactory.createURI(RDFS_LABEL), "Boil5")
                        .addInsert(NodeFactory.createURI(KB + "MunicipalUtility"), NodeFactory.createURI(OC_isOwnerOf), NodeFactory.createURI(KB + "HeatGeneratorBoil6"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil6"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatBoiler"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorBoil6"), NodeFactory.createURI(RDFS_LABEL), "Boil6");
        UpdateRequest MunicipalUtility_ur = MunicipalUtility_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", MunicipalUtility_ur.toString());


        // For HeatGenerator Part
        HeatHeneratorUpdate("HeatGeneratorGT", Value_ThermalLoad_GT, Value_HCV_GT, Value_LCV_GT, Value_CO2Factor_GT);
        HeatHeneratorUpdate("HeatGeneratorBoil4", Value_ThermalLoad_HB4, Value_HCV_HB4, Value_LCV_HB4, Value_CO2Factor_HB4);
        HeatHeneratorUpdate("HeatGeneratorBoil5", Value_ThermalLoad_HB5, Value_HCV_HB5, Value_LCV_HB5, Value_CO2Factor_HB5);
        HeatHeneratorUpdate("HeatGeneratorBoil6", Value_ThermalLoad_HB6, Value_HCV_HB6, Value_LCV_HB6, Value_CO2Factor_HB6);


        // For the IncinerationPlant part
        UpdateBuilder IncinerationPlant_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "IncinerationPlant"), NodeFactory.createURI(OHN + "hasOperatingAvailability"), NodeFactory.createURI(KB + "Availability" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "Availability" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "Availability"))
                        .addInsert(NodeFactory.createURI(KB + "IncinerationPlant"), NodeFactory.createURI(OHN + "hasProvidedHeatAmount"), NodeFactory.createURI(KB + "EnergyInTimeInterval" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "EnergyInTimeInterval" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EnergyInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + "IncinerationPlant"), NodeFactory.createURI(OHN + "hasMaxHourlySupply"), NodeFactory.createURI(KB + "MaxPower" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "IncinerationPlant"), NodeFactory.createURI(OHN + "hasMinHourlySupply"), NodeFactory.createURI(KB + "MinPower" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "MaxPower" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_POWER))
                        .addInsert(NodeFactory.createURI(KB + "MinPower" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_POWER));
        UpdateRequest IncinerationPlant_ur = IncinerationPlant_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", IncinerationPlant_ur.toString());
        omHasValueTS("EnergyInTimeInterval" + "IncinerationPlant", "MegaWattHour");
        omHasValueTS("MaxPower" + "IncinerationPlant", "MegaWattHour");
        omHasValueTS("MinPower" + "IncinerationPlant", "MegaWattHour");


        // For GasTurbine part
        UpdateBuilder GasTurbine_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(OHN + "hasRatedElectricalPower"), NodeFactory.createURI(KB + "ElectricalPower" + "HeatGeneratorGT"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(OHN + "hasMinimumThermalLoad"), NodeFactory.createURI(KB + "ThermalLoad" + "HeatGeneratorGT"))
                        .addInsert(NodeFactory.createURI(KB + "ElectricalPower" + "HeatGeneratorGT"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_POWER))
                        .addInsert(NodeFactory.createURI(KB + "ThermalLoad" + "HeatGeneratorGT"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_POWER))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(OHN + "hasMinimumIdleTime"), NodeFactory.createURI(KB + "IdleTime" + "HeatGeneratorGT"))
                        .addInsert(NodeFactory.createURI(KB + "IdleTime" + "HeatGeneratorGT"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_DURATION))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(OHN + "hasCoGenElectricityAmount"), NodeFactory.createURI(KB + "EnergyInTimeInterval" + "HeatGeneratorGT"))
                        .addInsert(NodeFactory.createURI(KB + "EnergyInTimeInterval" + "HeatGeneratorGT"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EnergyInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + "HeatGeneratorGT"), NodeFactory.createURI(OC_hasRevenue), NodeFactory.createURI(KB + "CoGenRevenueInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + "CoGenRevenueInTimeInterval"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CoGenRevenueInTimeInterval"));
        UpdateRequest GasTurbine_ur = GasTurbine_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", GasTurbine_ur.toString());
        omHasValueNonTS("ElectricalPower" + "HeatGeneratorGT", "MegaWatt", Value_RatedElectricalPower);
        omHasValueNonTS("ThermalLoad" + "HeatGeneratorGT", "MegaWatt", Value_MinimumThermalLoad);
        omHasValueNonTS("IdleTime" + "HeatGeneratorGT", "Hour", Value_MinimumIdleTime);
        omHasValueTS("EnergyInTimeInterval" + "HeatGeneratorGT", "MegaWattHour");
        omHasValueTS("CoGenRevenueInTimeInterval", "Euro");


        // For Contract part
        UpdateBuilder Contract_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(RDFS_LABEL), "Contract")
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "isFullfilledBy"), NodeFactory.createURI(KB + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "hasMinAnnualPurchaseVolume"), NodeFactory.createURI(KB + "MinPurchase" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "hasMaxAnnualPurchaseVolume"), NodeFactory.createURI(KB + "MaxPurchase" + "IncinerationPlant"))
                        .addInsert(NodeFactory.createURI(KB + "MinPurchase" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_Energy))
                        .addInsert(NodeFactory.createURI(KB + "MaxPurchase" + "IncinerationPlant"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_Energy))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "hasTieredUnitPrice"), NodeFactory.createURI(KB + "TieredUnitPrice"))
                        .addInsert(NodeFactory.createURI(KB + "TieredUnitPrice"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "TieredUnitPrice"))
                        .addInsert(NodeFactory.createURI(KB + "TieredUnitPrice"), NodeFactory.createURI(OHN + "hasTier"), NodeFactory.createURI(KB + "Tier_1"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_1"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "Tier"))
                        .addInsert(NodeFactory.createURI(KB + "TieredUnitPrice"), NodeFactory.createURI(OHN + "hasTier"), NodeFactory.createURI(KB + "Tier_2"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_2"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "Tier"))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "hasCurrentUnitPrice"), NodeFactory.createURI(KB + "HeatUnitPrice_1"))
                        .addInsert(NodeFactory.createURI(KB + "HeatUnitPrice_1"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatUnitPrice"))
                        .addInsert(NodeFactory.createURI(KB + "Contract"), NodeFactory.createURI(OHN + "hasCurrentUnitPrice"), NodeFactory.createURI(KB + "HeatUnitPrice_2"))
                        .addInsert(NodeFactory.createURI(KB + "HeatUnitPrice_2"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HeatUnitPrice"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_1"), NodeFactory.createURI(OHN + "hasUnitPrice"), NodeFactory.createURI(KB + "HeatUnitPrice_1"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_2"), NodeFactory.createURI(OHN + "hasUnitPrice"), NodeFactory.createURI(KB + "HeatUnitPrice_2"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_1"), NodeFactory.createURI(OHN + "hasCumulativeEnergyCap"), NodeFactory.createURI(KB + "CumulativeEnergyCap_1"))
                        .addInsert(NodeFactory.createURI(KB + "Tier_2"), NodeFactory.createURI(OHN + "hasCumulativeEnergyCap"), NodeFactory.createURI(KB + "CumulativeEnergyCap_2"))
                        .addInsert(NodeFactory.createURI(KB + "CumulativeEnergyCap_1"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_Energy))
                        .addInsert(NodeFactory.createURI(KB + "CumulativeEnergyCap_2"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_Energy));
        UpdateRequest Contract_ur = Contract_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", Contract_ur.toString());
        omHasValueNonTS("CumulativeEnergyCap_1", "GigaWattHourPerYear", Value_TierOneCap);
        omHasValueNonTS("CumulativeEnergyCap_2", "GigaWattHourPerYear", Value_TierTwoCap);
        omHasValueNonTS("HeatUnitPrice_1", "EuroPerMegaWattHour", Value_TierOneUnitPrice);
        omHasValueNonTS("HeatUnitPrice_2", "EuroPerMegaWattHour", Value_TierTwoUnitPrice);
        omHasValueNonTS("MinPurchase" + "IncinerationPlant", "MegaWattHourPerYear", Value_MinPurchase);
        omHasValueNonTS("MaxPurchase" + "IncinerationPlant", "MegaWattHourPerYear", Value_MaxPurchase);


        // For the CalendarEffect part
        UpdateBuilder CalendarEffect_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "CalendarEffect"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CalendarEffect"))
                        .addInsert(NodeFactory.createURI(KB + "CalendarEffect"), NodeFactory.createURI("http://nomisma.org/ontology#hasDate"), NodeFactory.createURI(XSD_DATE))
                        .addInsert(NodeFactory.createURI(KB + "CalendarEffect"), NodeFactory.createURI(OHN + "isPublicHoliday"), NodeFactory.createURI(XSD_BOOLEAN))
                        .addInsert(NodeFactory.createURI(KB + "CalendarEffect"), NodeFactory.createURI(OHN + "isVacation"), NodeFactory.createURI(XSD_BOOLEAN))
                        .addInsert(NodeFactory.createURI(KB + "AirTemperature"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(EMS + "AirTemperature"));
        UpdateRequest CalendarEffect_ur = CalendarEffect_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", CalendarEffect_ur.toString());


        // For the UnitRate part
        UpdateBuilder UnitRate_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + "GridCharges"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GridCharges"))
                        .addInsert(NodeFactory.createURI(KB + "ElectricitySpotPrice"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "ElectricitySpotPrice"))
                        .addInsert(NodeFactory.createURI(KB + "CHPBonus"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CHPBonus"))
                        .addInsert(NodeFactory.createURI(KB + "CO2CertificatePrice"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CO2CertificatePrice"))
                        .addInsert(NodeFactory.createURI(KB + "GasUnitCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "GasUnitCost"))
                        .addInsert(NodeFactory.createURI(KB + "HourlyLabourCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HourlyLabourCost"))
                        .addInsert(NodeFactory.createURI(KB + "HourlyWearCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HourlyWearCost"))
                        .addInsert(NodeFactory.createURI(KB + "DemandDrivenWearCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "emandDrivenWearCost"));
        UpdateRequest UnitRate_ur = UnitRate_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", UnitRate_ur.toString());
        omHasValueTS("GridCharges", "EuroPerMegaWattHour");
        omHasValueTS("ElectricitySpotPrice", "EuroPerMegaWattHour");
        omHasValueTS("CHPBonus", "EuroPerMegaWattHour");
        omHasValueTS("CO2CertificatePrice", "EuroPerTon");
        omHasValueTS("GasUnitCost", "EuroPerMegaWattHour");
        omHasValueTS("HourlyLabourCost", "EuroPerHour");
        omHasValueTS("HourlyWearCost", "EuroPerHour");
        omHasValueTS("DemandDrivenWearCost", "EuroPerMegaWattHour");
    }


    public void HeatHeneratorUpdate(String HeatGenerator_instance, float Value_ThermalLoad, float Value_HCV, float Value_LCV, float Value_CO2Factor) {
        UpdateBuilder HeatGenerator_instance_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasOperatingAvailability"), NodeFactory.createURI(KB + "Availability" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "Availability" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "Availability"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasGeneratedHeatAmount"), NodeFactory.createURI(KB + "EnergyInTimeIntervalHA" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "EnergyInTimeIntervalHA" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EnergyInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasConsumedGasAmount"), NodeFactory.createURI(KB + "EnergyInTimeIntervalGA" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "EnergyInTimeIntervalGA" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EnergyInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasOperatingTime"), NodeFactory.createURI(KB + "DurationInTimeInterval" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "DurationInTimeInterval" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "DurationInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OCP_hasCO2emission), NodeFactory.createURI(KB + "CO2EmissionInTimeInterval" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "CO2EmissionInTimeInterval" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CO2EmissionInTimeInterval"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasRatedThermalPower"), NodeFactory.createURI(KB + "ThermalLoad" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "ThermalLoad" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_POWER))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OCP_hasFuelType), NodeFactory.createURI(KB + "NaturalGas" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "NaturalGas" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OCP_FuelType))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasHigherCalorificValue"), NodeFactory.createURI(KB + "HigherCalorificValue" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "HigherCalorificValue" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "HigherCalorificValue"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasLowerCalorificValue"), NodeFactory.createURI(KB + "LowerCalorificValue" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "LowerCalorificValue" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "LowerCalorificValue"))
                        .addInsert(NodeFactory.createURI(KB + "NaturalGas" + HeatGenerator_instance), NodeFactory.createURI(OHN + "hasCO2Factor"), NodeFactory.createURI(KB + "NaturalGasCO2Factor" + HeatGenerator_instance))
                        .addInsert(NodeFactory.createURI(KB + "NaturalGasCO2Factor" + HeatGenerator_instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "CO2Factor"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "StartUpCost"))
                        .addInsert(NodeFactory.createURI(KB + "StartUpCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "StartUpCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "ShutDownCost"))
                        .addInsert(NodeFactory.createURI(KB + "ShutDownCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "ShutDownCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "VariableWearCost"))
                        .addInsert(NodeFactory.createURI(KB + "VariableWearCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "VariableWearCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "FuelCost"))
                        .addInsert(NodeFactory.createURI(KB + "FuelCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "FuelCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "EmissionCost"))
                        .addInsert(NodeFactory.createURI(KB + "EmissionCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "EmissionCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "FixedWearCost"))
                        .addInsert(NodeFactory.createURI(KB + "FixedWearCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "FixedWearCost"))
                        .addInsert(NodeFactory.createURI(KB + HeatGenerator_instance), NodeFactory.createURI(OC_HAS_COST), NodeFactory.createURI(KB + "LabourCost"))
                        .addInsert(NodeFactory.createURI(KB + "LabourCost"), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OHN + "LabourCost"));
        UpdateRequest HeatGenerator_instance_ur = HeatGenerator_instance_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", HeatGenerator_instance_ur.toString());
        omHasValueTS("DurationInTimeInterval" + HeatGenerator_instance, "Hour");
        omHasValueTS("EnergyInTimeIntervalGA" + HeatGenerator_instance, "MegaWattHour");
        omHasValueTS("EnergyInTimeIntervalHA" + HeatGenerator_instance, "MegaWattHour");
        omHasValueTS("CO2EmissionInTimeInterval" + HeatGenerator_instance, "Ton");
        omHasValueNonTS("ThermalLoad" + HeatGenerator_instance, "MegaWatt", Value_ThermalLoad);
        omHasValueNonTS("HigherCalorificValue" + HeatGenerator_instance, "KiloWattHourPerCubicMeter", Value_HCV);
        omHasValueNonTS("LowerCalorificValue" + HeatGenerator_instance, "KiloWattHourPerCubicMeter", Value_LCV);
        omHasValueNonTS("NaturalGasCO2Factor" + HeatGenerator_instance, "TonPerMegaWattHour", Value_CO2Factor);
        omHasValueTS("StartUpCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("ShutDownCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("VariableWearCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("FuelCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("EmissionCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("FixedWearCost" + HeatGenerator_instance, "Euro");
        omHasValueTS("LabourCost" + HeatGenerator_instance, "Euro");
    }


    // Update the non time-series triples part
    public void omHasValueNonTS(String Instance, String Unit, float NumericalValue) {
        UpdateBuilder omHasValueNonTS_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + Instance), NodeFactory.createURI(OM_HAS_VALUE), NodeFactory.createURI(KB + "Measure" + Instance))
                        .addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_MEASURE))
                        .addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(OM_HAS_UNIT), NodeFactory.createURI(KB + Unit))
                        .addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_UNIT))
                        .addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(OM_SYMBOL), Unit)
                        .addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI("OM_Has_NUMERICAL_VALUE"), NumericalValue);
        UpdateRequest omHasValueNonTS_ur = omHasValueNonTS_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", omHasValueNonTS_ur.toString());
    }


    // Update the time-series triples part
    public void omHasValueTS(String Instance, String Unit) {
        UpdateBuilder omHasValueTS_ub =
                new UpdateBuilder()
                        .addInsert(NodeFactory.createURI(KB + Instance), NodeFactory.createURI(OM_HAS_VALUE), NodeFactory.createURI(KB + "Measure" + Instance))
                        .addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_MEASURE))
                        .addInsert(NodeFactory.createURI(KB + "Measure" + Instance), NodeFactory.createURI(OM_HAS_UNIT), NodeFactory.createURI(KB + Unit))
                        .addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(RDF_TYPE), NodeFactory.createURI(OM_UNIT))
                        .addInsert(NodeFactory.createURI(KB + Unit), NodeFactory.createURI(OM_SYMBOL), Unit);
        UpdateRequest omHasValueTS_ur = omHasValueTS_ub.buildRequest();
        AccessAgentCaller.updateStore("http://host.docker.internal:48888/ontoheatnet", omHasValueTS_ur.toString());
    }


    public void setTsClient(TimeSeriesClient<OffsetDateTime> tsClient) {
        this.tsClient = tsClient;
    }


    public void updateTSData(Map<String, List<?>> HeatingNetworkReadings) throws IllegalArgumentException {
        if (!HeatingNetworkReadings.isEmpty()) {
            List<TimeSeries<OffsetDateTime>> timeSeries;
            try {
                timeSeries = convertReadingsToTimeSeries(HeatingNetworkReadings);
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Readings can not be converted to proper time series!", e);
            }
            for (TimeSeries<OffsetDateTime> ts : timeSeries) {
                if (!ts.getTimes().isEmpty()) {
                    try {
                        tsClient.addTimeSeriesData(ts);
                    } catch (Exception e) {
                        e.getMessage();
                       throw new JPSRuntimeException("Cannot not add timeseries!");
                    } finally {
                        tsClient.disconnectRDB();
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Readings can not be empty!");
        }
    }


    private List<TimeSeries<OffsetDateTime>> convertReadingsToTimeSeries(Map<String, List<?>> HeatingNetworkReadings)
            throws NoSuchElementException, IOException {
        List<OffsetDateTime> HeatingNetworkTimestamps = HeatingNetworkReadings.get(HeatNetworkInputAgent.timestampKey).stream()
                .map(timestamp -> (convertStringToOffsetDateTime(timestamp.toString()))).collect(Collectors.toList());
        List<TimeSeries<OffsetDateTime>> timeSeries = new ArrayList<>();
        //String mappingFolder = "/Users/HXUE01/Documents/GitHub/TheWorldAvatar/Agents/DistrictHeatingNetworkAgent/config/mappings";
        String mappingFolder = System.getenv("MAPPINGSPATH");
        try {
            readMappings(mappingFolder);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        for (JSONKeyToIRIMapper mapping : mappings) {
            List<String> iris = new ArrayList<>();
            List<List<?>> values = new ArrayList<>();
            for (String key : mapping.getAllJSONKeys()) {
                iris.add(mapping.getIRI(key));
                if (HeatingNetworkReadings.containsKey(key)) {
                    values.add(HeatingNetworkReadings.get(key));
                } else {
                    throw new NoSuchElementException("The key " + key + " is not contained in the readings!");
                }
            }
            List<OffsetDateTime> times = HeatingNetworkTimestamps;
            TimeSeries<OffsetDateTime> currentTimeSeries = new TimeSeries<>(times, iris, values);
            timeSeries.add(currentTimeSeries);
        }
        return timeSeries;
    }

    private OffsetDateTime convertStringToOffsetDateTime(String timestamp) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime localTime = LocalDateTime.parse(timestamp, dtf);
        return OffsetDateTime.of(localTime, ZoneOffset.UTC);
    }

    private static void readMappings(String mappingFolder) throws IOException, java.io.IOException {
        mappings = new ArrayList<>();
        File folder = new File(mappingFolder);
        File[] mappingFiles = folder.listFiles();
        if (mappingFiles == null) {
            throw new IOException("Folder does not exist: " + mappingFolder, null);
        }
        if (mappingFiles.length == 0) {
            throw new IOException("No files in the folder: " + mappingFolder, null);
        } else {
            for (File mappingFile : mappingFiles) {
                JSONKeyToIRIMapper mapper = new JSONKeyToIRIMapper(HeatNetworkInputAgent.generatedIRIPrefix, mappingFile.getAbsolutePath());
                mappings.add(mapper);
                mapper.saveToFile(mappingFile.getAbsolutePath());
            }
        }
    }


    public void initializeTimeSeriesIfNotExist() {
        //String mappingFolder = "/Users/HXUE01/Documents/GitHub/TheWorldAvatar/Agents/DistrictHeatingNetworkAgent/config/mappings";
        String mappingFolder = System.getenv("MAPPINGSPATH");
        try {
            readMappings(mappingFolder);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        for (JSONKeyToIRIMapper mapping : mappings) {
            List<String> iris = mapping.getAllIRIs();
            if (!timeSeriesExist(iris)) {
                List<Class<?>> classes = iris.stream().map(this::getClassFromJSONKey).collect(Collectors.toList());
                try {
                    tsClient.initTimeSeries(iris, classes, timeUnit);
                    LOGGER.info(String.format("Initialized time series with the following IRIs: %s", String.join(", ", iris)));
                } catch (Exception e) {
                    throw new JPSRuntimeException("Could not initialize timeseries!");
                } finally {
                    tsClient.disconnectRDB();
                }
            }
        }
    }

    private boolean timeSeriesExist(List<String> iris) {
        for (String iri : iris) {
            try {
                if (!tsClient.checkDataHasTimeSeries(iri)) {
                    return false;
                }
            } catch (DataAccessException e) {
                if (e.getMessage().contains("ERROR: relation \"dbTable\" does not exist")) {
                    return false;
                } else {
                    throw e;
                }
            } finally {
                tsClient.disconnectRDB();
            }
        }
        return true;
    }

    private Class<?> getClassFromJSONKey(String jsonKey) {
        if (jsonKey.contains("sample1") || jsonKey.contains("sample2") || jsonKey.contains("sample3")) {
            return Double.class;
        } else if (jsonKey.contains(timestampKey)) {
            return String.class;
        } else {
            return Double.class;
        }
    }

    public static String[] ReadColInput(int col, String filepath, String delimiter) {
        String currentLine;
        String[] data;
        ArrayList<String> colData = new ArrayList<String>();

        try {
            FileReader fr = new FileReader(filepath);
            BufferedReader br = new BufferedReader(fr);
            while ((currentLine = br.readLine()) != null) {
                data = currentLine.split(delimiter);
                colData.add(data[col]);
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return colData.toArray(new String[0]);
    }
}