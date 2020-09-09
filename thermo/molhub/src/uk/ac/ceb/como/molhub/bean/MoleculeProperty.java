package uk.ac.ceb.como.molhub.bean;

// TODO: Auto-generated Javadoc
/**
 * @author nk510
 * The Class MoleculeProperty saves molecules properties taken form a digital entity  (g09 file). 
 */
/**
 * @author NK
 *
 */
/**
 * @author NK
 *
 */
public class MoleculeProperty {

	private String uniqueFileIdentifier;

	/** The file name. */
	private String fileName;

	/**
	 * The uuid. This property is generated by using current date, CPU milliseconds,
	 * folder path to Apache Tomcat server.
	 */
	private String uuid;

	/** The molecule name. */
	private String moleculeName;

	/** The level of theory. */
	private String levelOfTheory;

	/** The basis set. */
	private String basisSet;

	/** The geometry type. */
	private String geometryType;

	/** The frequency. */
	private Frequency frequency;

	/** The rotational constant. */
	private RotationalConstant rotationalConstant;

	/** The atomic mass. */
	private AtomicMass atomicMass;

	/** The program name. */
	private String programName;

	/** The program version. */
	private String programVersion;

	/** The run date. */
	private String runDate;

	/**
	 * Instantiates a new molecule property.
	 */
	public MoleculeProperty() {
	}

	
	public MoleculeProperty(String uuid, String uniqueFileIdentifier, String moleculeName, String basisSet, String levelOfTheory) {

		this.uuid = uuid;
		this.uniqueFileIdentifier = uniqueFileIdentifier;
		this.moleculeName = moleculeName;
		this.basisSet = basisSet;
		this.levelOfTheory = levelOfTheory;

	}

	/**
	 * Instantiates a new molecule property.
	 *
	 * @param uuid          the uuid for species
	 * @param moleculeName  the molecule name
	 * @param basisSet      the basis set
	 * @param levelOfTheory the level of theory (method)
	 */
	public MoleculeProperty(String uuid, String moleculeName, String basisSet, String levelOfTheory) {

		this.uuid = uuid;
		this.moleculeName = moleculeName;
		this.basisSet = basisSet;
		this.levelOfTheory = levelOfTheory;

	}

	/**
	 * Instantiates a new molecule property.
	 *
	 * @param uuid          the uuid
	 * @param moleculeName  the molecule name
	 * @param basisSet      the basis set
	 * @param levelOfTheory the level of theory
	 * @param geometryType  the geometry type
	 */
//public MoleculeProperty(String uuid,String moleculeName, String basisSet, String levelOfTheory, String geometryType) {
//		
//		this.uuid=uuid;
//		this.moleculeName=moleculeName;
//		this.basisSet=basisSet;
//		this.levelOfTheory=levelOfTheory;
//		this.geometryType=geometryType;
//		
//	}

	/**
	 * Instantiates a new molecule property.
	 *
	 * @param uuid          the uuid
	 * @param moleculeName  the molecule name
	 * @param levelOfTheory the level of theory
	 */
	public MoleculeProperty(String uuid, String moleculeName, String levelOfTheory) {

		this.uuid = uuid;
		this.moleculeName = moleculeName;
		this.levelOfTheory = levelOfTheory;

	}

	/**
	 * Instantiates a new molecule property.
	 *
	 * @param uuid         the uuid
	 * @param moleculeName the molecule name
	 */
	public MoleculeProperty(String uuid, String moleculeName) {

		this.uuid = uuid;
		this.moleculeName = moleculeName;
	}

	/**
	 * Instantiates a new molecule property.
	 *
	 * @param moleculeName the molecule name
	 */
	public MoleculeProperty(String moleculeName) {

		this.moleculeName = moleculeName;

	}

	/**
	 * Gets the molecule name.
	 *
	 * @return the molecule name
	 */
	public String getMoleculeName() {
		return moleculeName;
	}

	/**
	 * Sets the molecule name.
	 *
	 * @param moleculeName the new molecule name
	 */
	public void setMoleculeName(String moleculeName) {
		this.moleculeName = moleculeName;
	}

	/**
	 * Gets the level of theory.
	 *
	 * @return the level of theory
	 */
	public String getLevelOfTheory() {
		return levelOfTheory;
	}

	/**
	 * Sets the level of theory.
	 *
	 * @param levelOfTheory the new level of theory
	 */
	public void setLevelOfTheory(String levelOfTheory) {
		this.levelOfTheory = levelOfTheory;
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 *
	 * @param uuid the new uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the basis set.
	 *
	 * @return the basis set
	 */
	public String getBasisSet() {
		return basisSet;
	}

	/**
	 * Sets the basis set.
	 *
	 * @param basisSet the new basis set
	 */
	public void setBasisSet(String basisSet) {
		this.basisSet = basisSet;
	}

	/**
	 * Gets the geometry type.
	 *
	 * @return the geometry type
	 */
	public String getGeometryType() {
		return geometryType;
	}

	/**
	 * Sets the geometry type.
	 *
	 * @param geometryType the new geometry type
	 */
	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}

	/**
	 * Gets the program name.
	 *
	 * @return the program name
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * Sets the program name.
	 *
	 * @param programName the new program name
	 */
	public void setProgramName(String programName) {
		this.programName = programName;
	}

	/**
	 * Gets the program version.
	 *
	 * @return the program version
	 */
	public String getProgramVersion() {
		return programVersion;
	}

	/**
	 * Sets the program version.
	 *
	 * @param programVersion the new program version
	 */
	public void setProgramVersion(String programVersion) {
		this.programVersion = programVersion;
	}

	/**
	 * Gets the run date.
	 *
	 * @return the run date
	 */
	public String getRunDate() {
		return runDate;
	}

	/**
	 * Sets the run date.
	 *
	 * @param runDate the new run date
	 */
	public void setRunDate(String runDate) {
		this.runDate = runDate;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the frequency.
	 *
	 * @return the frequency
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * Sets the frequency.
	 *
	 * @param frequency the new frequency
	 */
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	/**
	 * Gets the rotational constant.
	 *
	 * @return the rotational constant
	 */
	public RotationalConstant getRotationalConstant() {
		return rotationalConstant;
	}

	/**
	 * Sets the rotational constant.
	 *
	 * @param rotationalConstant the new rotational constant
	 */
	public void setRotationalConstant(RotationalConstant rotationalConstant) {
		this.rotationalConstant = rotationalConstant;
	}

	/**
	 * Gets the atomic mass.
	 *
	 * @return the atomic mass
	 */
	public AtomicMass getAtomicMass() {
		return atomicMass;
	}

	/**
	 * Sets the atomic mass.
	 *
	 * @param atomicMass the new atomic mass
	 */
	public void setAtomicMass(AtomicMass atomicMass) {
		this.atomicMass = atomicMass;
	}


	public String getUniqueFileIdentifier() {
		return uniqueFileIdentifier;
	}


	public void setUniqueFileIdentifier(String uniqueFileIdentifier) {
		this.uniqueFileIdentifier = uniqueFileIdentifier;
	}




	
}