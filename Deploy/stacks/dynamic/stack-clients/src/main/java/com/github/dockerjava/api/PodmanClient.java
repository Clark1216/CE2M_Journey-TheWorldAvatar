package com.github.dockerjava.api;

import com.cmclinnovations.swagger.podman.ApiClient;
import com.github.dockerjava.api.command.ListPodsCmd;
import com.github.dockerjava.api.command.RemovePodCmd;

public interface PodmanClient extends DockerClient {

    public ApiClient getPodmanClient();

    /**
     * Command to list all pods.
     *
     * @return command
     */
    ListPodsCmd listPodsCmd();

    /**
     * Command to remove a pod
     * 
     * @param podId pod id or pod name
     * @return command
     */
    RemovePodCmd removePodCmd(String podId);

}
