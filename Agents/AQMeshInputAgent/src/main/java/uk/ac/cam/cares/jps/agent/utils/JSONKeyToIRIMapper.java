package uk.ac.cam.cares.jps.agent.utils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class to retrieve store a mapping from JSON key to knowledge graph IRI which extends a HashMap with fixed types.
 * @author Niklas Kasenburg
 */
public class JSONKeyToIRIMapper {

    private String iriPrefix;
    // Mapping from JSON key to IRI
    private final HashMap<String, String> jsonToIRIMapping  = new HashMap<>();
    // Mapping from IRI to JSON key
    private final HashMap<String, String> iriToJSONMapping  = new HashMap<>();


    /**
     * Standard constructor can be used if the mapping to use does not contain keys without value.
     */
    public JSONKeyToIRIMapper()  {}

    /**
     * Constructor setting the prefix.
     * @param iriPrefix The prefix used when generating an IRI.
     */
    public JSONKeyToIRIMapper(String iriPrefix) {
        setIRIPrefix(iriPrefix);
    }

    /**
     * Constructor using directly a mapping file and setting the prefix.
     * @param iriPrefix The prefix used when generating an IRI.
     * @param filepath The path to the mapping file.
     */
    public JSONKeyToIRIMapper(String iriPrefix, String filepath) throws IOException {
        setIRIPrefix(iriPrefix);
        readMappingFromFile(filepath);
    }

    /**
     * Reads the JSON key to IRI from a properties files.
     * @param filepath The path to the properties file.
     */
    public void readMappingFromFile(String filepath) throws IOException {

        // Check whether properties file exists at specified location
        File file = new File(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException("No properties file found at specified filepath: " + filepath);
        }
        // Read the mapping from the properties file
        // Try-with-resource to ensure closure of input stream
        try (InputStream input = new FileInputStream(file)) {

            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);
            // Set the key value pairs from the properties to the HashMap
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                // Create an IRI if no one exists for the key
                if (value.isEmpty()) {
                    // Create a random IRI in the format: <Prefix>_<JSONKey>_<UUID>
                    value = generateIRI(this.iriPrefix, key);
                    // TODO: Should be replaces by logging
                    System.out.println("No IRI provided for key " + key + ". The IRI was automatically generated: " + value);
                }
                // Check that the IRI is valid
                if (checkIRI(value)) {
                    // Check that the IRI is not already used for another key
                    String oldKey = iriToJSONMapping.get(value);
                    if (oldKey != null) {
                        throw new IOException("The IRI "+ value + " is already used for the key " + oldKey + ", and can not be used for key " + key);
                    }
                    // Update both maps
                    else {
                        jsonToIRIMapping.put(key, value);
                        iriToJSONMapping.put(value, key);
                    }
                }
                else {
                    throw new IOException("The value for key "+ key + " is not a valid URI: " + value);
                }
            }
        }
    }

    /**
     * Saves the JSON key to IRI mapping to a properties files overwriting any content in the file if it already exists.
     * @param filepath The path to the properties file.
     */
    public void saveToFile(String filepath) throws IOException {
        File file = new File(filepath);
        // Will overwrite whatever is in the file already
        try (OutputStream output = new FileOutputStream(file, false)) {
            // Create properties
            Properties prop = new Properties();
            // Fill properties with the mapping
            prop.putAll(jsonToIRIMapping);
            prop.store(output, "Note: URLs might look like this: http\\:// \n" +
                    "This is to escape the ':' symbol and ensure that the properties can be loaded in properly.");
        }
    }

    /**
     * Generates a IRI based on the prefix, JSON key and a random UUID.
     * @param prefix The prefix, usually a combination of namespace and an API identifier.
     * @param jsonKey The JSON key.
     * @return The generated IRI as string.
     */
    public String generateIRI(String prefix, String jsonKey) {
        return String.join("_", prefix, jsonKey, UUID.randomUUID().toString());
    }

    /**
     * Retrieves the IRI for a JSON key. Wraps the get method of the HashMap field.
     * @param jsonKey The JSON key for which to retrieve the IRI.
     * @return The IRI that is mapped to the JSON key or null if there is no mapping.
     */
    public String getIRI(String jsonKey) {
        return jsonToIRIMapping.get(jsonKey);
    }

    /**
     * Retrieves the JSON key for an IRI. Wraps the get method of the HashMap field.
     * @param iri The IRI provided as string.
     * @return The JSON key that is mapped to the IRI or null if there is no mapping.
     */
    public String getJSONKey(String iri) {
        return iriToJSONMapping.get(iri);
    }

    /**
     * Retrieves all IRIs that are handled in the mapping.
     * @return The IRIs as a list of string in no particular order.
     */
    public List<String> getAllIRIs() {
        return new ArrayList<>(iriToJSONMapping.keySet());
    }

    /**
     * Retrieves all JSON keys that are handled in the mapping.
     * @return The JSON keys as a list of string in no particular order.
     */
    public List<String> getAllJSONKeys() {
        return new ArrayList<>(jsonToIRIMapping.keySet());
    }

    /**
     * Getter for the IRI prefix.
     * @return The currently set IRI prefix.
     */
    public String getIRIPrefix() {
        return iriPrefix;
    }

    /**
     * Setter for the IRI prefix.
     * @param iriPrefix The new IRI prefix.
     */
    public void setIRIPrefix(String iriPrefix) {
        // Checks whether the prefix is a valid URI before setting it
        if (checkIRI(iriPrefix)) {
            this.iriPrefix = iriPrefix;
        }
        else throw new IllegalArgumentException(iriPrefix + " is not a valid IRI.");
    }

    /**
     * Checks whether an IRI is valid.
     * @param iri The IRI to test provided as string.
     */
    private boolean checkIRI(String iri) {
        // Every legal (full) IRI contains at least one ':' character to separate the scheme from the rest of the IRI
        return Pattern.compile("\\w+\\S+:\\S+\\w+").matcher(iri).matches();
    }
}
