package com.cmclinnovations.jps.agent.kinetics.simulation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import uk.ac.cam.cares.jps.base.slurm.job.SlurmJobException;
import uk.ac.cam.cares.jps.base.slurm.job.Status;

import org.junit.Assert;

public class KineticsAgentJobRequestTest {
	@Test
	public void testJobRequest() {
		try {
			String query = "{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\": \"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\": \"http://www.theworldavatar.com/kb/ontospecies/00b7e248-ae24-35bf-b7a0-b470b923ddf6.owl#00b7e248-ae24-35bf-b7a0-b470b923ddf6\"}";
			KineticsAgent kineticsAgent = new KineticsAgent();
			String messageActual = kineticsAgent.setUpJob(query).toString();
			Assert.assertEquals(messageActual, Status.JOB_SETUP_SUCCESS_MSG.getName());
			System.out.println(messageActual);
		} catch (KineticsAgentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SlurmJobException e) {
			e.printStackTrace();
		}
	}
}
