package uk.ac.cam.cares.jps.agent.dashboard.json.templating;

import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.List;
import java.util.Map;

/**
 * A Java representation of a JSON-like model that encapsulates and enforces information
 * about PostgreSQL template variable syntax specific to Grafana dashboard. At the moment,
 * the PostgreSQL variables are used to link each asset to their corresponding time series column to perform correct SQL queries.
 *
 * @author qhouyee
 */
class PostgresVariable extends TemplateVariable {
    private final String LABEL;
    private final String DESCRIPTION;
    private final String DATABASE_CONNECTION_ID;
    private final StringBuilder QUERY_SYNTAX = new StringBuilder();

    /**
     * Constructor for a variable to filter the asset type or rooms associated with a specific facility.
     *
     * @param itemType            The asset type or room for this mapping.
     * @param facilityItemMapping A mapping that maps each item to its facility. Comes in the format of {facility1:[asset1, asset2], facility2:[asset3,asset4]}.
     * @param databaseId          The database connection ID generated by Grafana.
     */
    public PostgresVariable(String itemType, Map<String, List<String>> facilityItemMapping, String databaseId) {
        // Ensure that this variable can be viewed on the dashboard
        super(itemType, 0);
        // Add label for the type
        this.LABEL = itemType.equals(StringHelper.ROOM_KEY) ? "Rooms" :
                itemType.equals(StringHelper.SYSTEM_KEY) ? "Smart Meter" : StringHelper.addSpaceBetweenCapitalWords(itemType);
        // Description should follow the item type
        this.DESCRIPTION = "A filter for the items of " + this.LABEL.toLowerCase() + " type.";
        // Append each value in the list in the required format
        this.QUERY_SYNTAX.append("SELECT v AS \\\"__value\\\" FROM (values ");
        StringBuilder temp = new StringBuilder();
        this.DATABASE_CONNECTION_ID = databaseId;
        // For each facility, add a query to link the facility and its containing item as a key value pair
        for (String facility : facilityItemMapping.keySet()) {
            facilityItemMapping.get(facility).stream().forEach(itemName -> {
                // Only append a comma at the start if it is not the first value
                if (temp.length() != 0) temp.append(", ");
                temp.append("('").append(facility).append("', '")
                        .append(itemName).append("')");
            });
        }
        this.QUERY_SYNTAX.append(temp).append(") AS v(k,v)  WHERE k IN (${").append(StringHelper.FACILITY_KEY).append("});");
    }

    /**
     * Constructor for a variable that filters the measures associated with an asset or room.
     *
     * @param measure       The measure name for this variable.
     * @param item          The asset type or room for this measure.
     * @param databaseId    The database connection ID generated by Grafana.
     * @param assetMeasures A list of time series column and asset names for different assets to be included into the query component.
     */
    public PostgresVariable(String measure, String item, String databaseId, List<String[]> assetMeasures) {
        // Variable name will be a combination of measure name and item type to make it unique
        super(measure + item, 2);
        // Empty label as the label will not be displayed
        this.LABEL = "";
        // Description should follow the measure name and item type
        this.DESCRIPTION = "A hidden filter that displays the corresponding time series of " + StringHelper.addSpaceBetweenCapitalWords(measure).toLowerCase()
                + " for " + StringHelper.addSpaceBetweenCapitalWords(item).toLowerCase();
        // Append each value in the list in the required format
        this.QUERY_SYNTAX.append("SELECT k AS \\\"__text\\\", v AS \\\"__value\\\" FROM (values ");
        StringBuilder temp = new StringBuilder();
        this.DATABASE_CONNECTION_ID = databaseId;
        for (String[] assetMeasure : assetMeasures) {
            // Only append a comma at the start if it is not the first value
            if (temp.length() != 0) temp.append(", ");
            // Append the name and the corresponding column name
            temp.append("('").append(assetMeasure[0]).append("', '")
                    .append(assetMeasure[1]).append("')");
        }
        String itemVariable = StringHelper.formatVariableName(item); // Format variable name according to its formatted name
        this.QUERY_SYNTAX.append(temp).append(") AS v(k,v)  WHERE k IN (${").append(itemVariable).append("});");
    }

    /**
     * Construct the Postgres variable as a String.
     *
     * @return The Postgres variable syntax as a String.
     */
    @Override
    protected String construct() {
        // Construct the common elements
        StringBuilder builder = super.genCommonJson()
                // Variable display label
                .append("\"label\": \"").append(this.LABEL).append("\",")
                // Postgres datasource
                .append("\"datasource\": {\"type\": \"postgres\", \"uid\": \"").append(this.DATABASE_CONNECTION_ID).append("\"},")
                // Description for this variable
                .append("\"description\": \"").append(this.DESCRIPTION).append("\",")
                // Query values of this variable
                .append("\"definition\": \"").append(this.QUERY_SYNTAX).append("\",")
                .append("\"query\": \"").append(this.QUERY_SYNTAX).append("\",")
                // Default settings but unsure what they are for
                .append("\"regex\": \"\",")
                .append("\"sort\" : 0,")
                // Variable type must be set as query to work
                .append("\"type\": \"query\"")
                .append("}");
        return builder.toString();
    }
}
