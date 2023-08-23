package uk.ac.cam.cares.jps.agent.dashboard.utils;

/**
 * A class that provides methods to format strings.
 *
 * @author qhouyee
 */
public class StringHelper {
    public static String ASSET_KEY = "assets";
    public static String ROOM_KEY = "Rooms";

    /**
     * Formats the SPARQL variable name for a SPARQL query syntax. Note that a space will be appended beforehand.
     */
    public static String formatSparqlVarName(String variable) {return " ?" + variable;}

    /**
     * Formats the variable names to remove white spaces, dashes, and underscores and use only lower cases for Grafana syntax.
     */
    public static String formatVariableName(String variable) {return variable.toLowerCase().replaceAll("[\\s\\-_]", "");}

    /**
     * Add space between each word, which is defined by having the first letter be capital. For eg, MyTestCase will return My Test Case.
     * Note that abbreviations will not be separated.
     */
    public static String addSpaceBetweenCapitalWords(String input) {
        // Add a space before an uppercase letter followed by a lowercase letter
        String result = input.replaceAll("([a-z])([A-Z])", "$1 $2");
        // Add a space only if capital letters are followed by lower case.
        // Else keep the capital letters together as they are abbreviations: Example ExhaustVAV = Exhaust VAV
        // See test cases for more information
        result = result.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
        return result;
    }
}
