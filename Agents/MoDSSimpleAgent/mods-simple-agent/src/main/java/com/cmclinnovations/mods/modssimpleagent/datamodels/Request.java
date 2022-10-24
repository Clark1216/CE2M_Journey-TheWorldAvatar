package com.cmclinnovations.mods.modssimpleagent.datamodels;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

    @JsonInclude(Include.NON_NULL)
    String jobID;
    @JsonProperty("SimulationType")
    private String simulationType;
    @JsonProperty("Algorithms")
    @JsonInclude(Include.NON_NULL)
    private List<Algorithm> algorithms;
    @JsonProperty("Inputs")
    @JsonInclude(Include.NON_NULL)
    private Data inputs;
    @JsonProperty("Outputs")
    @JsonInclude(Include.NON_NULL)
    private Data outputs;
    @JsonProperty("Sensitivities")
    @JsonInclude(Include.NON_NULL)
    private List<SensitivityResult> sensitivities;
    @JsonProperty("LoadSurrogate")
    @JsonInclude(Include.NON_NULL)
    private String loadSurrogate;
    @JsonProperty("SaveSurrogate")
    @JsonInclude(Include.NON_NULL)
    private Boolean saveSurrogate;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getSimulationType() {
        return simulationType;
    }

    public void setSimulationType(String simulationType) {
        this.simulationType = simulationType;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<Algorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public Data getInputs() {
        return inputs;
    }

    public void setInputs(Data inputs) {
        this.inputs = inputs;
    }

    public Data getOutputs() {
        return outputs;
    }

    public void setOutputs(Data outputs) {
        this.outputs = outputs;
    }

    public List<SensitivityResult> getSensitivities() {
        return sensitivities;
    }

    public void setSensitivities(List<SensitivityResult> sensitivities) {
        this.sensitivities = sensitivities;
    }

    public Boolean getSaveSurrogate() {
        return saveSurrogate;
    }

    public void setSaveSurrogate(Boolean saveSurrogate) {
        this.saveSurrogate = saveSurrogate;
    }

    public String getLoadSurrogate() {
        return loadSurrogate;
    }

    public void setLoadSurrogate(String loadSurrogate) {
        this.loadSurrogate = loadSurrogate;
    }
    
}
