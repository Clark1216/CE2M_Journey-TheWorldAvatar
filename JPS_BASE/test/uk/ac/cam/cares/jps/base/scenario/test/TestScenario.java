package uk.ac.cam.cares.jps.base.scenario.test;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.config.KeyValueManager;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.scenario.ScenarioHelper;

public class TestScenario extends TestCase {

	public void testDividePath() {
		
		// auto generate a scenario id
		String path = "/";
		String[] actual= ScenarioHelper.dividePath(path);
		assertNotNull(actual[0]);
		assertNull(actual[1]);
		
		// throws an exception because path (without /) does not contain at least 10 characters
		path = "/123456789";
		try {
			ScenarioHelper.dividePath(path);
		} catch (Exception e) {
		}
	
		path = "/1234567890";
		actual= ScenarioHelper.dividePath(path);
		assertEquals("1234567890", actual[0]);
		assertNull(actual[1]);
		
		path = "/1234567890/any/fancy/operation";
		actual= ScenarioHelper.dividePath(path);
		assertEquals("1234567890", actual[0]);
		assertEquals("/any/fancy/operation", actual[1]);
	}
	
	public void testGetLocalPathBaseScenarioForKB() {
		
		String root = KeyValueManager.get("absdir.root");
		
		String url = "http://www.theworldavatar.com/kb/sgp/jurongisland/something.owl";
		String scenarioUrl = null;
		String path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/kb/sgp/jurongisland/something.owl", path);
		
		url = "http://www.jparksimulator.com/data/something.csv";
		scenarioUrl = null;
		path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/data/something.csv", path);
		
		try {
			url = "http://www.theworldavatar.com/kb/sgp/jurongisland/something.owl";
			scenarioUrl = BucketHelper.getScenarioUrl(JPSConstants.SCENARIO_NAME_BASE);
			JPSHttpServlet.enableScenario(scenarioUrl);
			path = BucketHelper.getLocalPath(url, scenarioUrl);
			assertEquals(root + "/kb/sgp/jurongisland/something.owl", path);
		} finally {
			JPSHttpServlet.disableScenario();
		}
	}
	
	public void testGetLocalPathBaseScenarioForJPSKB() {
		
		String root = AgentLocator.getPathToJpsWorkingDir() + ScenarioHelper.SCENARIO_COMP_URL + "/" + JPSConstants.SCENARIO_NAME_BASE;		
		String url = "http://www.theworldavatar.com" + ScenarioHelper.SCENARIO_COMP_URL + "/base/kb/sgp/jurongisland/something.owl";
		String scenarioUrl = null;
		String path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/www_theworldavatar_com/kb/sgp/jurongisland/something.owl", path);
		
		url = "http://www.jparksimulator.com" + ScenarioHelper.SCENARIO_COMP_URL + "/base/data/something.csv";
		scenarioUrl = null;
		path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/www_jparksimulator_com/data/something.csv", path);
		
		try {
			url = "http://www.theworldavatar.com" + ScenarioHelper.SCENARIO_COMP_URL + "/base/kb/sgp/jurongisland/something.owl";
			scenarioUrl = BucketHelper.getScenarioUrl(JPSConstants.SCENARIO_NAME_BASE);
			JPSHttpServlet.enableScenario(scenarioUrl);
			path = BucketHelper.getLocalPath(url, scenarioUrl);
			assertEquals(root + "/www_theworldavatar_com/kb/sgp/jurongisland/something.owl", path);
		} finally {
			JPSHttpServlet.disableScenario();
		}
	}
	
	public void testGetLocalPathNonBaseScenario() {
		
		String scenarioName = "testmy123";
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);
		String root = AgentLocator.getPathToJpsWorkingDir() + ScenarioHelper.SCENARIO_COMP_URL + "/" + scenarioName;
		
		String url = "http://www.theworldavatar.com/jps/kb/sgp/jurongisland/something.owl";
		String path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/www_theworldavatar_com/kb/sgp/jurongisland/something.owl", path);
		
		url = "http://www.jparksimulator.com/jps/data/something.csv";
		path = BucketHelper.getLocalPath(url, scenarioUrl);
		assertEquals(root + "/www_jparksimulator_com/data/something.csv", path);
		
		try {
			url = "http://www.theworldavatar.com/jps/kb/sgp/jurongisland/something.owl";
			JPSHttpServlet.enableScenario(scenarioUrl);
			path = BucketHelper.getLocalPath(url, scenarioUrl);
			assertEquals(root + "/www_theworldavatar_com/kb/sgp/jurongisland/something.owl", path);
		} finally {
			JPSHttpServlet.disableScenario();
		}
	}
	
	public void testGetLocalDataPathBaseScenario() {
		String root = AgentLocator.getPathToJpsWorkingDir() + ScenarioHelper.SCENARIO_COMP_URL + "/" + JPSConstants.SCENARIO_NAME_BASE;	
		String path = BucketHelper.getLocalDataPath();		
		System.out.println(path);
		assertTrue(path.startsWith(root));
		assertTrue(path.contains(JPSConstants.SCENARIO_SUBDIR_DATA));
	}
	
	public void testGetLocalDataPathNonBaseScenario() {

		String scenarioName = "testmy123";
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);

		try {
			JPSHttpServlet.enableScenario(scenarioUrl);
			String root = AgentLocator.getPathToJpsWorkingDir() + ScenarioHelper.SCENARIO_COMP_URL + "/" + scenarioName;	
			String path = BucketHelper.getLocalDataPath();		
			System.out.println(path);
			assertTrue(path.startsWith(root));
			assertTrue(path.contains(JPSConstants.SCENARIO_SUBDIR_DATA));
		} finally {
			JPSHttpServlet.disableScenario();
		}
	}
	
	public void testGetIriPrefixBaseScenario() {
		String prefix = BucketHelper.getIriPrefix();
		System.out.println(prefix);
		assertTrue(prefix.contains("/jps/kb"));
	}
	
	public void testGetIriPrefixNonBaseScenario() {
		String scenarioName = "testGetIriPrefixNonBaseScenario";
		String scenarioUrl = BucketHelper.getScenarioUrl(scenarioName);
		System.out.println("scenarioUrl=" + scenarioUrl);
		JPSHttpServlet.enableScenario(scenarioUrl);		
		try {
			String prefix = BucketHelper.getIriPrefix();
			System.out.println(prefix);
			assertTrue(prefix.contains("/jps/kb"));
		} finally {
			JPSHttpServlet.disableScenario();
		}
	}
	
	public void testGetHashedResource() {
		String resource = "https://www.jparksimulator.com:8080/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#abctest";
		String hashedResource = ScenarioHelper.getHashedResource(resource);
		assertEquals("-1820947590/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl", hashedResource);
	}
}
