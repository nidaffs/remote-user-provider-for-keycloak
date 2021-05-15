package com.exadelinternship.keycloak;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class RemoteUserStorageProviderFactory implements UserStorageProviderFactory<RemoteUserStorageProvider> {

    public static final String PROVIDER_NAME = "username-90";

    @Override
    public RemoteUserStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        System.out.println(keycloakSession.getContext().getUri().getQueryParameters());
        return new RemoteUserStorageProvider(keycloakSession, componentModel, buildHttpClient("http://spring-backend:8090"));//http://spring-backend:8090
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    private UserApiService buildHttpClient(String uri) {

        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(uri);

        System.out.println("buildHttpClient");
        return target.proxyBuilder(UserApiService.class)
                .classloader(UserApiService.class.getClassLoader()).build();

    }
}
