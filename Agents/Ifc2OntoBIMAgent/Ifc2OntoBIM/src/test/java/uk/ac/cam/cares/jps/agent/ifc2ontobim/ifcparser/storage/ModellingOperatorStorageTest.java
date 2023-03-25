package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.storage;

import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.JunitTestUtils;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.model.CartesianPoint;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.model.CartesianTransformationOperator;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.model.DirectionVector;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.model.LocalPlacement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.NamespaceMapper;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModellingOperatorStorageTest {
    private static ModellingOperatorStorage testMappings;
    private static final String TEST_BASE_URI = "http://www.example.org/";
    private static final String TEST_POINT_CLASS = "CartesianPoint";
    private static final String TEST_FIRST_POINT_IRI = TEST_BASE_URI + TEST_POINT_CLASS + "_12";
    private static final String TEST_SECOND_POINT_IRI = TEST_BASE_URI + TEST_POINT_CLASS + "_5613";
    private static final String TEST_THIRD_POINT_IRI = TEST_BASE_URI + TEST_POINT_CLASS + "_10358";
    private static final String TEST_FOURTH_POINT_IRI = TEST_BASE_URI + TEST_POINT_CLASS + "_99212";
    private static final Double TEST_X_COORD = 3.0;
    private static final Double TEST_Y_COORD = 6.1;
    private static final Double TEST_Z_COORD = 0.5;
    private static final Double TEST_X_COORD2 = 5.5;
    private static final Double TEST_Y_COORD2 = 1.23;
    private static final Double TEST_Z_COORD2 = 0.42;
    private static final String TEST_DIR_VECTOR_CLASS = "DirectionVector";
    private static final String TEST_FIRST_DIR_VECTOR_IRI = TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_9178";
    private static final String TEST_SECOND_DIR_VECTOR_IRI = TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_1758";
    private static final String TEST_THIRD_DIR_VECTOR_IRI = TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_8762";
    private static final String TEST_FOURTH_DIR_VECTOR_IRI = TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_7725";
    private static final Double TEST_X_DIR_RATIO = 5.6;
    private static final Double TEST_Y_DIR_RATIO = 3.19;
    private static final Double TEST_Z_DIR_RATIO = 0.55;
    private static final Double TEST_X_DIR_RATIO2 = 9.35;
    private static final Double TEST_Y_DIR_RATIO2 = 8.23;
    private static final Double TEST_Z_DIR_RATIO2 = 2.32;
    private static final String TEST_PLACEMENT_IRI = TEST_BASE_URI + JunitTestUtils.IFC_PLACEMENT_CLASS + "_93492";
    private static final String TEST_PLACEMENT_BIM_IRI = TEST_BASE_URI + JunitTestUtils.BIM_PLACEMENT_CLASS + "_93492";
    private static final String TEST_TRANSFORMATION_OPERATOR_IRI = TEST_BASE_URI + JunitTestUtils.IFC_TRANSFORMATION_OPERATOR_CLASS + "_67547";
    private static final String TEST_TRANSFORMATION_OPERATOR_BIM_IRI = TEST_BASE_URI + JunitTestUtils.BIM_TRANSFORMATION_OPERATOR_CLASS + "_67547";
    private static final String NON_EXISTENT_IRI = TEST_BASE_URI + "DOES/NOT/EXIST_1223";
    private static final String NON_EXISTING_ERROR = NON_EXISTENT_IRI + " does not exist in mappings!";


    @BeforeAll
    static void setUp() {
        testMappings = ModellingOperatorStorage.Singleton();
        NamespaceMapper.setBaseNameSpace(TEST_BASE_URI);
    }

    @BeforeEach
    void resetSingleton() {
        ModellingOperatorStorage.resetSingleton();
    }

    @AfterAll
    static void reset() {
        ModellingOperatorStorage.resetSingleton();
        NamespaceMapper.setBaseNameSpace("");
    }

    @Test
    void testGetPointFail() {
        JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> testMappings.getPoint(NON_EXISTENT_IRI));
        assertEquals(NON_EXISTING_ERROR, thrownError.getMessage());
    }

    @Test
    void testGetDirectionVectorFail() {
        JPSRuntimeException thrownError = assertThrows(JPSRuntimeException.class, () -> testMappings.getDirectionVector(NON_EXISTENT_IRI));
        assertEquals(NON_EXISTING_ERROR, thrownError.getMessage());
    }

    @Test
    void testResetSingleton() {
        // Add a sample point and verify if it is added
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), null);
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        assertTrue(testMappings.containsIri(TEST_FIRST_POINT_IRI));
        // Add a sample direction vector and verify if it is added
        DirectionVector sampleDirection = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), null);
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirection);
        assertTrue(testMappings.containsIri(TEST_FIRST_DIR_VECTOR_IRI));
        // Execute method
        ModellingOperatorStorage.resetSingleton();
        // Verify if the mappings have been reset with none of the previously added IRIs
        assertFalse(testMappings.containsIri(TEST_FIRST_POINT_IRI));
        assertFalse(testMappings.containsIri(TEST_FIRST_DIR_VECTOR_IRI));
    }

    @Test
    void testAddAndGetPoint() {
        // Create a new sample
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), null);
        // Execute method
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        // Assert if they are equals
        assertTrue(testMappings.containsIri(TEST_FIRST_POINT_IRI));
        assertEquals(samplePoint, testMappings.getPoint(TEST_FIRST_POINT_IRI));
    }

    @Test
    void testAddAndGetDirectionVector() {
        // Create a new sample
        DirectionVector sampleDirection = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), null);
        // Execute method
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirection);
        // Assert if they are equals
        assertTrue(testMappings.containsIri(TEST_FIRST_DIR_VECTOR_IRI));
        assertEquals(sampleDirection, testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI));
    }

    @Test
    void testAddLocalPlacementAndStatement() {
        // Create a new sample
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        LocalPlacement samplePlacement = new LocalPlacement(TEST_PLACEMENT_IRI, TEST_FIRST_POINT_IRI, null, null, null);
        // Execute method
        testMappings.add(samplePlacement);
        testMappings.constructAllStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Assert if they are equals
        assertTrue(result.contains(TEST_PLACEMENT_BIM_IRI + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/LocalPlacement"));
        assertTrue(result.contains(TEST_PLACEMENT_BIM_IRI + ", http://www.theworldavatar.com/kg/ontobim/hasRefPoint, " + TEST_FIRST_POINT_IRI));
    }

    @Test
    void testAddCartesianTransformationOperatorAndStatement() {
        // Create a new sample
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        CartesianTransformationOperator samplePlacement = new CartesianTransformationOperator(TEST_TRANSFORMATION_OPERATOR_IRI, TEST_FIRST_POINT_IRI, null, null, null);
        // Verify that no statements have been generated
        testMappings.constructAllStatements(sampleSet);
        assertEquals(0, sampleSet.size());
        // Execute method
        testMappings.add(samplePlacement);
        testMappings.constructAllStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Assert if they are equals
        assertTrue(result.contains(TEST_TRANSFORMATION_OPERATOR_BIM_IRI + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/CartesianTransformationOperator"));
        assertTrue(result.contains(TEST_TRANSFORMATION_OPERATOR_BIM_IRI + ", http://www.theworldavatar.com/kg/ontobim/hasLocalOrigin, " + TEST_FIRST_POINT_IRI));
    }

    @Test
    void testReplaceDuplicates() {
        // Create new samples and add them into the mappings
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint2 = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint3 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), TEST_Z_COORD2.toString());
        CartesianPoint samplePoint4 = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        testMappings.add(TEST_SECOND_POINT_IRI, samplePoint2);
        testMappings.add(TEST_THIRD_POINT_IRI, samplePoint3);
        testMappings.add(TEST_FOURTH_POINT_IRI, samplePoint4);
        DirectionVector sampleDirVector = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        DirectionVector sampleDirVector2 = new DirectionVector(TEST_X_DIR_RATIO2.toString(), TEST_Y_DIR_RATIO2.toString(), TEST_Z_DIR_RATIO2.toString());
        DirectionVector sampleDirVector3 = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        DirectionVector sampleDirVector4 = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirVector);
        testMappings.add(TEST_SECOND_DIR_VECTOR_IRI, sampleDirVector2);
        testMappings.add(TEST_THIRD_DIR_VECTOR_IRI, sampleDirVector3);
        testMappings.add(TEST_FOURTH_DIR_VECTOR_IRI, sampleDirVector4);
        // Execute method
        testMappings.replaceDuplicates();
        // Assert that the duplicate have been properly replaced
        assertEquals(samplePoint, testMappings.getPoint(TEST_FIRST_POINT_IRI));
        assertEquals(samplePoint, testMappings.getPoint(TEST_SECOND_POINT_IRI));
        assertEquals(samplePoint3, testMappings.getPoint(TEST_THIRD_POINT_IRI));
        assertEquals(samplePoint, testMappings.getPoint(TEST_FOURTH_POINT_IRI));
        assertNotEquals(testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI), testMappings.getDirectionVector(TEST_SECOND_DIR_VECTOR_IRI));
        assertEquals(testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI), testMappings.getDirectionVector(TEST_THIRD_DIR_VECTOR_IRI));
        assertEquals(testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI), testMappings.getDirectionVector(TEST_FOURTH_DIR_VECTOR_IRI));
    }


    @Test
    void testReplaceDuplicatesForPoint() {
        // Create a new sample and add them into the mappings
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint2 = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint3 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), TEST_Z_COORD2.toString());
        CartesianPoint samplePoint4 = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        testMappings.add(TEST_SECOND_POINT_IRI, samplePoint2);
        testMappings.add(TEST_THIRD_POINT_IRI, samplePoint3);
        testMappings.add(TEST_FOURTH_POINT_IRI, samplePoint4);
        // Execute method
        testMappings.replaceDuplicates();
        // Assert that the duplicate points have been properly replaced
        assertEquals(samplePoint, testMappings.getPoint(TEST_FIRST_POINT_IRI));
        assertEquals(samplePoint, testMappings.getPoint(TEST_SECOND_POINT_IRI));
        assertEquals(samplePoint3, testMappings.getPoint(TEST_THIRD_POINT_IRI));
        assertEquals(samplePoint, testMappings.getPoint(TEST_FOURTH_POINT_IRI));
    }

    @Test
    void testReplaceDuplicatesForNoDuplicatePoints() {
        // Create a new sample and add them into the mappings
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint2 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), TEST_Z_COORD2.toString());
        CartesianPoint samplePoint3 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), null);
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        testMappings.add(TEST_SECOND_POINT_IRI, samplePoint2);
        testMappings.add(TEST_THIRD_POINT_IRI, samplePoint3);
        // Execute method
        testMappings.replaceDuplicates();
        // Assert that the non-duplicates are not replaced
        assertEquals(samplePoint, testMappings.getPoint(TEST_FIRST_POINT_IRI));
        assertEquals(samplePoint2, testMappings.getPoint(TEST_SECOND_POINT_IRI));
        assertEquals(samplePoint3, testMappings.getPoint(TEST_THIRD_POINT_IRI));
    }

    @Test
    void testReplaceDuplicatesForNoDuplicateDirections() {
        DirectionVector sampleDirVector = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        DirectionVector sampleDirVector2 = new DirectionVector(TEST_X_DIR_RATIO2.toString(), TEST_Y_DIR_RATIO2.toString(), TEST_Z_DIR_RATIO2.toString());
        DirectionVector sampleDirVector3 = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), null);
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirVector);
        testMappings.add(TEST_SECOND_DIR_VECTOR_IRI, sampleDirVector2);
        testMappings.add(TEST_THIRD_DIR_VECTOR_IRI, sampleDirVector3);
        // Execute method
        testMappings.replaceDuplicates();
        // Assert that the non-duplicates are not replaced
        assertNotEquals(testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI), testMappings.getDirectionVector(TEST_SECOND_DIR_VECTOR_IRI));
        assertNotEquals(testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI), testMappings.getDirectionVector(TEST_THIRD_DIR_VECTOR_IRI));
    }

    @Test
    void testConstructStatementsPartialRetrieveForCartesianPoint() {
        // Set up sample set
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        // Create a new sample and add them into the mappings
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint2 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), TEST_Z_COORD2.toString());
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        testMappings.add(TEST_SECOND_POINT_IRI, samplePoint2);
        // Retrieve the points if you wish to generate their statement
        testMappings.getPoint(TEST_FIRST_POINT_IRI);
        // Execute method
        testMappings.constructAllStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedPointStatement(), result);
        JunitTestUtils.doesExpectedListExist(genExpectedPointCoordinateStatements(TEST_X_COORD, TEST_Y_COORD, TEST_Z_COORD), result);
        // Second point will not be generated as it was not retrieve and used
        JunitTestUtils.doesExpectedListNotExist(genExpectedPointCoordinateStatements(TEST_X_COORD2, TEST_Y_COORD2, TEST_Z_COORD2), result);
    }


    @Test
    void testConstructStatementsPartialRetrieveForDirectionVector() {
        // Set up sample set
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        // Create a new sample and add them into the mappings
        DirectionVector sampleDirection = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        DirectionVector sampleDirection2 = new DirectionVector(TEST_X_DIR_RATIO2.toString(), TEST_Y_DIR_RATIO2.toString(), TEST_Z_DIR_RATIO2.toString());
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirection);
        testMappings.add(TEST_SECOND_DIR_VECTOR_IRI, sampleDirection2);
        // Retrieve the direction if you wish to generate their statement
        testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI);
        // Execute method
        testMappings.constructAllStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedDirectionStatement(), result);
        JunitTestUtils.doesExpectedListExist(genExpectedDirectionRatioStatements(TEST_X_DIR_RATIO, TEST_Y_DIR_RATIO, TEST_Z_DIR_RATIO), result);
        // Second point will not be generated as it was not retrieve and used
        JunitTestUtils.doesExpectedListNotExist(genExpectedDirectionRatioStatements(TEST_X_DIR_RATIO2, TEST_Y_DIR_RATIO2, TEST_Z_DIR_RATIO2), result);
    }

    @Test
    void testConstructStatements() {
        // Set up sample set
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        // Create a new sample and add them into the mappings
        CartesianPoint samplePoint = new CartesianPoint(TEST_X_COORD.toString(), TEST_Y_COORD.toString(), TEST_Z_COORD.toString());
        CartesianPoint samplePoint2 = new CartesianPoint(TEST_X_COORD2.toString(), TEST_Y_COORD2.toString(), TEST_Z_COORD2.toString());
        testMappings.add(TEST_FIRST_POINT_IRI, samplePoint);
        testMappings.add(TEST_SECOND_POINT_IRI, samplePoint2);
        DirectionVector sampleDirection = new DirectionVector(TEST_X_DIR_RATIO.toString(), TEST_Y_DIR_RATIO.toString(), TEST_Z_DIR_RATIO.toString());
        DirectionVector sampleDirection2 = new DirectionVector(TEST_X_DIR_RATIO2.toString(), TEST_Y_DIR_RATIO2.toString(), TEST_Z_DIR_RATIO2.toString());
        testMappings.add(TEST_FIRST_DIR_VECTOR_IRI, sampleDirection);
        testMappings.add(TEST_SECOND_DIR_VECTOR_IRI, sampleDirection2);
        // Retrieve the modelling operator if you wish to generate their statement
        testMappings.getPoint(TEST_FIRST_POINT_IRI);
        testMappings.getPoint(TEST_SECOND_POINT_IRI);
        testMappings.getDirectionVector(TEST_FIRST_DIR_VECTOR_IRI);
        testMappings.getDirectionVector(TEST_SECOND_DIR_VECTOR_IRI);
        // Execute method
        testMappings.constructAllStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedPointStatement(), result);
        JunitTestUtils.doesExpectedListExist(genExpectedPointCoordinateStatements(TEST_X_COORD, TEST_Y_COORD, TEST_Z_COORD), result);
        JunitTestUtils.doesExpectedListExist(genExpectedPointCoordinateStatements(TEST_X_COORD2, TEST_Y_COORD2, TEST_Z_COORD2), result);
        JunitTestUtils.doesExpectedListExist(genExpectedDirectionStatement(), result);
        JunitTestUtils.doesExpectedListExist(genExpectedDirectionRatioStatements(TEST_X_DIR_RATIO, TEST_Y_DIR_RATIO, TEST_Z_DIR_RATIO), result);
        JunitTestUtils.doesExpectedListExist(genExpectedDirectionRatioStatements(TEST_X_DIR_RATIO2, TEST_Y_DIR_RATIO2, TEST_Z_DIR_RATIO2), result);
    }

    private List<String> genExpectedPointStatement() {
        List<String> expected = new ArrayList<>();
        expected.add(TEST_BASE_URI + TEST_POINT_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/" + TEST_POINT_CLASS);
        return expected;
    }

    private List<String> genExpectedPointCoordinateStatements(Double xCoord, Double yCoord, Double zCoord) {
        List<String> expected = new ArrayList<>();
        expected.add(TEST_BASE_URI + TEST_POINT_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasXCoordinate, \"" + xCoord);
        expected.add(TEST_BASE_URI + TEST_POINT_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasYCoordinate, \"" + yCoord);
        expected.add(TEST_BASE_URI + TEST_POINT_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasZCoordinate, \"" + zCoord);
        return expected;
    }

    private List<String> genExpectedDirectionStatement() {
        List<String> expected = new ArrayList<>();
        expected.add(TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/" + TEST_DIR_VECTOR_CLASS);
        return expected;
    }

    private List<String> genExpectedDirectionRatioStatements(Double xRatio, Double yRatio, Double zRatio) {
        List<String> expected = new ArrayList<>();
        expected.add(TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasXDirectionRatio, \"" + xRatio);
        expected.add(TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasYDirectionRatio, \"" + yRatio);
        expected.add(TEST_BASE_URI + TEST_DIR_VECTOR_CLASS + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasZDirectionRatio, \"" + zRatio);
        return expected;
    }
}