package uk.ac.cam.ceb.como.jaxb.xml.generation;

import uk.ac.cam.ceb.como.io.chem.file.jaxb.Module;

public class GenerateCompChemModule {
	
	/**
	 * @author nk510
	 * @return <p>Instance of Module class and sets dictRef value to 'cc:initialization'.</p>
	 */
	public static Module generateInitialModule() {
		
		Module module = new Module();
		
		module.setDictRef("cc:initialization");

		
		return module;
	}

	/**
	 * @author nk510
	 * @return <p>Instance of Module class and sets dictRef value to 'cc:finalization'.</p>
	 */

	public static Module generateFinalModule() {
		
		Module module = new Module();
		
		module.setDictRef("cc:finalization");

		
		return module;
	}
	
	/**
	 * @author nk510
	 * @return <p>Instance of Module class and sets dictRef value to 'cc:environment'.</p>
	 */

	public static Module getEnvironmentModule() {
		
		Module module = new Module();
		
		module.setDictRef("cc:environment");

		
    	return module;
	}
	
	/**
	 * Gets the root module.
	 *
	 * @author nk510
	 * @param initialModule the in module
	 * @param finalModule the f module
	 * @param rootModule the root module
	 * @return <p>Returns object variable of Module JAXB class. It contains information
	 *         (DictRef) about job lists, and job.</p>
	 */
	
	public static Module getRootModule(Module initialModule, Module finalModule, Module environmentModule, Module rootModule) {

		Module jobListModule = new Module();

		rootModule.getAny().add(jobListModule);

		jobListModule.setDictRef("cc:jobList");

		Module jobModule = new Module();

		jobListModule.getAny().add(jobModule);

		jobModule.setDictRef("cc:job");

		jobModule.getAny().add(initialModule);

		jobModule.getAny().add(finalModule);
		
		jobModule.getAny().add(environmentModule);

		return jobModule;
	}
	
}