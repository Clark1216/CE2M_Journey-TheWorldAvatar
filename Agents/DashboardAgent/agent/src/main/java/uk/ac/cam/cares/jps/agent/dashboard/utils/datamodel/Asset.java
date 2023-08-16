package uk.ac.cam.cares.jps.agent.dashboard.utils.datamodel;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * A class holding the required information to support the enforcement of the Facility data model.
 * This class cannot be accessed outside the subpackage, and is intended to be a data model for holding asset information.
 *
 * @author qhouyee
 */
public class Asset {
    private final String ASSET_NAME;
    private final String ASSET_TYPE;
    private final Map<String, String[]> MEASURES = new HashMap<>();

    /**
     * Standard Constructor. This will store the metadata retrieved from the SPARQL query.
     *
     * @param assetType     The asset type.
     * @param measureName   Name of the measure associated with the asset.
     * @param measureIri    Corresponding dataIRI of the measure associated with the asset.
     * @param timeSeriesIri Corresponding time series IRI of the measure.
     */
    protected Asset(String assetName, String assetType, String measureName, String measureIri, String timeSeriesIri) {
        this.ASSET_NAME = assetName;
        this.ASSET_TYPE = assetType;
        this.addMeasure(assetType, measureName, measureIri, timeSeriesIri);
    }

    /**
     * Adds the measure to be stored within this instance.
     *
     * @param measureName   Name of the measure associated with the asset.
     * @param unit          Measure unit symbol
     * @param measureIri    Corresponding dataIRI of the measure associated with the asset.
     * @param timeSeriesIri Corresponding time series IRI of the measure.
     */
    protected void addMeasure(String measureName, String unit, String measureIri, String timeSeriesIri) {
        this.addMeasure(this.ASSET_TYPE, measureName, unit, measureIri, timeSeriesIri);
    }

    /**
     * A private overloaded method that adds the measure alongside its asset type to be stored within this instance.
     * This method will only be called when constructing the object
     *
     * @param assetType     The asset type.
     * @param measureName   Name of the measure associated with the asset.
     * @param unit          Measure unit symbol
     * @param measureIri    Corresponding dataIRI of the measure associated with the asset.
     * @param timeSeriesIri Corresponding time series IRI of the measure.
     */
    private void addMeasure(String assetType, String measureName, String unit, String measureIri, String timeSeriesIri) {
        String[] iris = new String[5];
        iris[0] = measureName;
        iris[1] = measureIri;
        iris[2] = timeSeriesIri;
        iris[3] = assetType;
        // Only append a unit if the inserted value is not null
        if (unit != null) iris[4] = unit;
        this.MEASURES.put(measureName, iris);
    }

    /**
     * A getter method for asset name.
     */
    protected String getAssetName() {return this.ASSET_NAME;}

    /**
     * A getter method to retrieve all assets and their associated metadata
     *
     * @returns A queue containing all asset information. Within the array, first position is measure name; Second position is the dataIRI; Third position is time series IRI; Fourth position is the asset type; Fifth position is unit if available.
     */
    protected Queue<String[]> getAssetData() {
        Queue<String[]> measureInfo = new ArrayDeque<>();
        for (String measure : this.MEASURES.keySet()) {
            measureInfo.offer(this.MEASURES.get(measure));
        }
        return measureInfo;
    }
}
