package com.github.dockerjava.jaxrs;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

import com.cmclinnovations.swagger.podman.ApiClient;
import com.cmclinnovations.swagger.podman.JSON;
import com.cmclinnovations.swagger.podman.RFC3339DateFormat;
import com.cmclinnovations.swagger.podman.auth.Authentication;
import com.github.dockerjava.jaxrs.filter.ResponseStatusExceptionFilter;

final public class ApiClientExtension extends ApiClient {

    private final PoolingHttpClientConnectionManager connManager;
    private final URI dockerHost;

    public ApiClientExtension(URI dockerHost) {
        this.connManager = new PoolingHttpClientConnectionManager(getSchemeRegistry(dockerHost)) {

            @Override
            public void close() {
                super.shutdown();
            }

            @Override
            public void shutdown() {
                // Disable shutdown of the pool. This will be done later, when this factory is
                // closed
                // This is a workaround for finalize method on jerseys ClientRuntime which
                // closes the client and shuts down the connection pool when it is garbage
                // collected
            }
        };
        this.dockerHost = dockerHost;

        json = new JSON();
        httpClient = buildHttpClient(debugging);

        this.dateFormat = new RFC3339DateFormat();

        // Set default User-Agent.
        setUserAgent("Swagger-Codegen/1.0.0/java");

        // Setup authentications (key: authentication name, value: authentication).
        authentications = new HashMap<String, Authentication>();
        // Prevent the authentications from being modified.
        authentications = Collections.unmodifiableMap(authentications);
    }

    @Override
    protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

        clientConfig.register(new ResponseStatusExceptionFilter());
        // clientConfig.register(JsonClientFilter.class);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        switch (dockerHost.getScheme()) {
            case "unix":
                break;
            default:
                throw new IllegalArgumentException("Unsupported protocol scheme: " + dockerHost);
        }

        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connManager);

        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, requestConfigBuilder.build());
    }

    private Registry<ConnectionSocketFactory> getSchemeRegistry(URI originalUri) {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("unix", new UnixConnectionSocketFactory(originalUri));
        return registryBuilder.build();
    }
}