package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element.buildingstructure;

import org.apache.jena.rdf.model.Statement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element.IfcModelRepresentation;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StatementHandler;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * A class representing the Window concept in OntoBuildingStructure.
 *
 * @author qhouyee
 */
public class Window extends IfcModelRepresentation {
    private final String elementIRI;
    private final String hostZoneIRI;
    private final String assemblyIRI;
    private final String geomRepIRI;


    /**
     * Standard Constructor initialising the common inputs.
     *
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param placementIri The local placement IRI for the element's position.
     * @param hostZoneIri  The IRI of the zone containing this element .
     * @param assemblyIri  The IRI of the assembly consisting of this element .
     * @param geomRepIri   The IRI of the element's geometry representation.
     */
    public Window(String name, String uid, String placementIri, String hostZoneIri, String assemblyIri, String geomRepIri) {
        super(name, uid, placementIri);
        this.elementIRI = this.getPrefix() + OntoBimConstant.WINDOW_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        this.hostZoneIRI = hostZoneIri;
        this.assemblyIRI = assemblyIri;
        this.geomRepIRI = geomRepIri;
    }

    /**
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    @Override
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        super.addIfcModelRepresentationStatements(statementSet);
        StatementHandler.addStatement(statementSet, this.hostZoneIRI, OntoBimConstant.BOT_CONTAINS_ELEMENT, this.elementIRI);
        StatementHandler.addStatement(statementSet, this.assemblyIRI, OntoBimConstant.BUILDING_STRUCTURE_CONSISTS_OF, this.elementIRI);
        StatementHandler.addStatement(statementSet, this.elementIRI, OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_WINDOW_CLASS);
        StatementHandler.addStatement(statementSet, this.elementIRI, OntoBimConstant.BIM_HAS_IFC_REPRESENTATION, this.getIfcRepIri());
        StatementHandler.addStatement(statementSet, this.getIfcRepIri(), OntoBimConstant.BIM_HAS_GEOM_REP, this.geomRepIRI);
    }
}
