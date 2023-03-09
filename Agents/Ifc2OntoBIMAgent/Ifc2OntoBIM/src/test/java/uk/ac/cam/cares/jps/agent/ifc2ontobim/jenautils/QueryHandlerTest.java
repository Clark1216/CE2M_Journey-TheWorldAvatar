package uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.JunitTestUtils;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.geom.ModelRepresentation3D;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone.IfcRoomRepresentation;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.zone.IfcStoreyRepresentation;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.SpatialZoneStorage;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryHandlerTest {
    private static final String inst = "Storey_514";
    private static final String secondInst = "Storey_654";
    private static final String testClass = "Storey";
    private static final String testConstructClass = "Building";
    private static final String testVar = "name";
    private static final String testLiteral = "Building1";
    private static final String testHeightVar = "height";
    private static final String testDoubleLiteral = "102";
    private static final String testIriVar = "IRI";
    private static final String testBaseUri = "http://www.example.org/";
    private static final String testIri = testBaseUri + "Test_124";
    private static final String testParentZoneVar = "subzone";
    private static final String testParentStoreyIri = testBaseUri + "IfcBuildingStorey_51076";
    private static final String testParentRoomIri = testBaseUri + "IfcRoom_51076";
    private static final String testShapeRepVar = "instshaperep";
    private static final String testShapeRepIri = testBaseUri + "IfcShapeRepresentation_51076";
    private static final String testSubContextVar = "subcontext";
    private static final String testSubContextIri = testBaseUri + "GeometricRepresentationSubContext_5151";
    private static final String testGeomVar = "geometry";
    private static final String testGeomIri = testBaseUri + "FacetedBrep_32516";
    private static final String testGeomType = JunitTestUtils.bimUri + "FacetedBrep";
    private static final String testShapeRepTypeVar = "shapereptype";
    private static final String testShapeRepType = "Faceted Brep";
    private static final String testSourcePlacementVar = "geomaxisplacement";
    private static final String testSourcePlacementIri = testBaseUri + "LocalPlacement_33352";
    private static final String testTargetPlacementVar = "cartesiantransformer";
    private static final String testTargetPlacementIri = testBaseUri + "CartesianTransformationOperator_610618";
    private static SpatialZoneStorage zoneMappings;
    private static IfcStoreyRepresentation storey;
    private static IfcRoomRepresentation room;
    @BeforeAll
    static void addTestZoneMappings() {
        // Create a new storey and room instance, which does not require any values except for the IRI
        // This IRI is necessary to generate the respective zone IRI within the class
        storey = new IfcStoreyRepresentation(testParentStoreyIri, null, null, null, null, null);
        room = new IfcRoomRepresentation(testParentRoomIri, null, null, null, null);
        // Add the storey and room to the singleton
        zoneMappings = SpatialZoneStorage.Singleton();
        zoneMappings.add(testParentStoreyIri, storey);
        zoneMappings.add(testParentRoomIri, room);
    }

    @Test
    void testInitSelectQueryBuilder() {
        SelectBuilder builder = QueryHandler.initSelectQueryBuilder();
        List<String> expected = this.genInitQuery();
        expected.forEach(line -> assertTrue(builder.buildString().contains(line)));
    }

    @Test
    void testExecSelectQuery() {
        Model sampleModel = this.genSampleModel();
        List<String> expected = new ArrayList<>();
        ResultSet results = QueryHandler.execSelectQuery(this.genSampleSelectQuery(), sampleModel);
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            expected.add(soln.get("storey").toString());
        }
        assertTrue(expected.contains(JunitTestUtils.bimUri + inst));
        assertTrue(expected.contains(JunitTestUtils.bimUri + secondInst));
    }

    @Test
    void testQueryConstructStatementsAsSet() {
        Model sampleModel = this.genSampleModel();
        LinkedHashSet<Statement> results = new LinkedHashSet<>();
        QueryHandler.queryConstructStatementsAsSet(this.genSampleConstructQuery(), sampleModel, results);
        // Store the results as string to make it easier to compare
        List<String> strResults = new ArrayList<>();
        results.forEach(statement -> strResults.add(statement.toString()));
        // Generate the expected statements
        String firstStatement = "[" + JunitTestUtils.bimUri + inst + ", " + RDF.type + ", " + JunitTestUtils.bimUri + testConstructClass + "]";
        String secondStatement = "[" + JunitTestUtils.bimUri + secondInst + ", " + RDF.type + ", " + JunitTestUtils.bimUri + testConstructClass + "]";
        assertTrue(strResults.contains(firstStatement));
        assertTrue(strResults.contains(secondStatement));
    }

    @Test
    void testRetrieveIri() {
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testIriVar, ResourceFactory.createResource(testIri));
        // Execute the method and ensure results are string
        assertEquals(testIri, QueryHandler.retrieveIri(solution, testIriVar));
        // If the variable does not exist, ensure that null is return
        assertNull(QueryHandler.retrieveIri(solution, "nonExisting"));
    }

    @Test
    void testRetrieveLiteral() {
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testVar, ResourceFactory.createPlainLiteral(testLiteral));
        solution.add(testHeightVar, ResourceFactory.createTypedLiteral(testDoubleLiteral));
        // Execute the method and ensure results are string
        assertEquals(testLiteral, QueryHandler.retrieveLiteral(solution, testVar));
        assertEquals(testDoubleLiteral, QueryHandler.retrieveLiteral(solution, testHeightVar));
        // If the variable does not exist, ensure that null is return
        assertNull(QueryHandler.retrieveLiteral(solution, "nonExisting"));
    }

    @Test
    void testRetrieveHostZoneForStorey() {
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testParentZoneVar, ResourceFactory.createResource(testParentStoreyIri));
        // Execute the method and ensure the expected host zone IRI belongs to the sample storey instance
        assertEquals(storey.getBotStoreyIRI(), QueryHandler.retrieveHostZone(solution, zoneMappings));
    }

    @Test
    void testRetrieveHostZoneForRoom() {
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testParentZoneVar, ResourceFactory.createResource(testParentRoomIri));
        // Execute the method and ensure the expected host zone IRI belongs to the sample room instance
        assertEquals(room.getBimRoomIRI(), QueryHandler.retrieveHostZone(solution, zoneMappings));
    }

    @Test
    void testRetrieveModelRepresentation3D() {
        // Set up a sampleSet
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testShapeRepVar, ResourceFactory.createResource(testShapeRepIri));
        solution.add(testSubContextVar, ResourceFactory.createResource(testSubContextIri));
        solution.add(testGeomVar, ResourceFactory.createResource(testGeomIri));
        solution.add(testShapeRepTypeVar, ResourceFactory.createPlainLiteral(testShapeRepType));
        solution.add(testSourcePlacementVar, ResourceFactory.createResource(testSourcePlacementIri));
        solution.add(testTargetPlacementVar, ResourceFactory.createResource(testTargetPlacementIri));
        // Execute the method and extract the result statements into a string
        ModelRepresentation3D resultModel = QueryHandler.retrieveModelRepresentation3D(solution);
        resultModel.addModelRepresentation3DStatements(sampleSet);
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedCommonModelRep3DStatements(), result);
        JunitTestUtils.doesExpectedListExist(genExpectedOptionalModelRep3DStatements(), result);
    }

    @Test
    void testRetrieveModelRepresentation3DNoOptionalValues() {
        // Set up a sampleSet
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        // Create a sample query solution for testing
        QuerySolutionMap solution = new QuerySolutionMap();
        solution.add(testShapeRepVar, ResourceFactory.createResource(testShapeRepIri));
        solution.add(testSubContextVar, ResourceFactory.createResource(testSubContextIri));
        solution.add(testGeomVar, ResourceFactory.createResource(testGeomIri));
        // Execute the method and extract the result statements into a string
        ModelRepresentation3D resultModel = QueryHandler.retrieveModelRepresentation3D(solution);
        resultModel.addModelRepresentation3DStatements(sampleSet);
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedCommonModelRep3DStatements(), result);
        // Verify that the following statements do not exist
        JunitTestUtils.doesExpectedListNotExist(genExpectedOptionalModelRep3DStatements(), result);
    }

    private List<String> genInitQuery() {
        List<String> expected = new ArrayList<>();
        expected.add("PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
        expected.add("PREFIX  bot:  <https://w3id.org/bot#>");
        expected.add("PREFIX  bim:  <http://www.theworldavatar.com/kg/ontobim/>");
        expected.add("PREFIX  ifc:  <http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#>");
        expected.add("PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
        expected.add("PREFIX  express: <https://w3id.org/express#>");
        expected.add("PREFIX  list: <https://w3id.org/list#>");
        expected.add("PREFIX  om:   <http://www.ontology-of-units-of-measure.org/resource/om-2/>");
        expected.add("SELECT DISTINCT  *");
        expected.add("WHERE");
        return expected;
    }

    private Model genSampleModel() {
        Model sampleModel = ModelFactory.createDefaultModel();
        sampleModel.createResource(JunitTestUtils.bimUri + inst)
                .addProperty(RDF.type,
                        sampleModel.createResource(JunitTestUtils.bimUri + testClass));
        sampleModel.createResource(JunitTestUtils.bimUri + secondInst)
                .addProperty(RDF.type,
                        sampleModel.createResource(JunitTestUtils.bimUri + testClass));
        return sampleModel;
    }

    private String genSampleSelectQuery() {
        SelectBuilder builder = new SelectBuilder();
        builder.addPrefix("bim", JunitTestUtils.bimUri);
        builder.addVar("?storey").addWhere("?storey", RDF.type, "bim:" + testClass);
        return builder.buildString();
    }

    private String genSampleConstructQuery() {
        ConstructBuilder builder = new ConstructBuilder();
        builder.addPrefix("bim", JunitTestUtils.bimUri);
        builder.addConstruct("?storey", RDF.type, "bim:" + testConstructClass)
                .addWhere("?storey", RDF.type, "bim:" + testClass);
        return builder.buildString();
    }

    private List<String> genExpectedCommonModelRep3DStatements() {
        List<String> expected = new ArrayList<>();
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/ModelRepresentation3D");
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasSubContext, " + testSubContextIri);
        expected.add(testSubContextIri + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/GeometricRepresentationSubContext");
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasRepresentationItem, " + testGeomIri);
        expected.add(testGeomIri + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " + testGeomType);
        return expected;
    }

    private List<String> genExpectedOptionalModelRep3DStatements() {
        List<String> expected = new ArrayList<>();
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasRepresentationType, \"" + testShapeRepType);
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasSourcePlacement, " + testSourcePlacementIri);
        expected.add(testSourcePlacementIri + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/LocalPlacement");
        expected.add(testBaseUri + "ModelRepresentation3D_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasTargetPlacement, " + testTargetPlacementIri);
        expected.add(testTargetPlacementIri + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/CartesianTransformationOperator");
        return expected;
    }
}