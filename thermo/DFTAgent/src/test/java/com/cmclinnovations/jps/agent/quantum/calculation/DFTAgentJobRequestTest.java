package com.cmclinnovations.jps.agent.quantum.calculation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import org.junit.Assert;

public class DFTAgentJobRequestTest {
	@Test
	public void testJobRequest() {
		try {
			String query = "{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\": \"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\": \"http://www.theworldavatar.com/kb/ontospecies/00b7e248-ae24-35bf-b7a0-b470b923ddf6.owl#00b7e248-ae24-35bf-b7a0-b470b923ddf6\"}";
			DFTAgent dftAgent = new DFTAgent();
			String messageActual = dftAgent.setUpJob(query);
			Assert.assertEquals(messageActual, Jobs.JOB_SETUP_SUCCESS_MSG.getName());
			System.out.println(messageActual);
		} catch (DFTAgentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
