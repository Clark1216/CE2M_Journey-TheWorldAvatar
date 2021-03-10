/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cam.ceb.como.enthalpy.calculator.io.reactions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.cam.ceb.como.enthalpy.calculator.io.Parser;

/**
 *
 * @author pb556
 */
public class ReactionListParser extends Parser {

    protected ReactionDataList data = new ReactionDataList();
    private Logger logger = Logger.getLogger(getClass());

    public ReactionListParser() {
        super();
    }

    public ReactionListParser(File file) {
        super(file);
    }

    @Override
    public void parse() throws FileNotFoundException, IOException {
        // extract products and reactants
        if (file == null) {
            logger.error("File cannot be read. No path is defined.", new IOException("No path is defined."));
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int lineNum = 0;
        String targetSpecies;
        while ((line = br.readLine()) != null) {
            // process the line
            lineNum++;
            if (line.isEmpty()) {
                continue;
            }
            if (line.contains("<->")) {
                line = line.toLowerCase();
                String[] mainItems = line.split("\\:");
                if (mainItems.length == 2) {
                    targetSpecies = mainItems[0].trim();
                    String[] sides = mainItems[1].trim().split("<->");
                    if (sides.length == 2) {
                        HashMap<String, Double> reactants = extractSpecies(sides[0].trim());
                        HashMap<String, Double> products = extractSpecies(sides[1].trim());
                        if (reactants == null || products == null) {
                            continue;
                        }
                        data.add(new ReactionData(reactants, products, targetSpecies));
                    } else {
                        logger.warn("Invalid line identified (line " + lineNum + "): Invalid definition of the reaction");
                    }
                }
                else {
                    logger.warn("Invalid line identified (line " + lineNum + "): Invalid definition of the target species and the reaction");
                }
            } else {
                logger.warn("Invalid line identified (line " + lineNum + "): Invalid definition of the reaction");
            }
        }
        br.close();
    }

    protected HashMap<String, Double> extractSpecies(String s) {
        HashMap<String, Double> species = new HashMap<String, Double>();
        String[] items = s.split("\\+");
        if (items.length >= 1) {
            for (int i = 0; i < items.length; i++) {
                String[] subItems = items[i].split("x");
                if (subItems.length == 2) {
                    species.put(subItems[1].trim().replace("\"", ""), Double.parseDouble(subItems[0].trim()));
                } else {
                    logger.warn("Invalid species data identified (" + s + ")");
                    return null;
                }
            }
        }
        return species;
    }

    @Override
    public Object get() {
        return data;
    }
}
