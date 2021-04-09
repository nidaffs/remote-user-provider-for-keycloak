package com.exadelinternship.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoteUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, CredentialInputValidator {

    private KeycloakSession keycloakSession;
    private ComponentModel componentModel;
    private UserApiService userService;

    public RemoteUserStorageProvider(KeycloakSession keycloakSession, ComponentModel componentModel, UserApiService userService) {
        this.keycloakSession = keycloakSession;
        this.componentModel = componentModel;
        this.userService = userService;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
        if (!supportsCredentialType(credentialType)) {
            return false;
        }
        return !getCredentialStore()
                .getStoredCredentialsByTypeStream(realmModel, userModel, credentialType)
                .collect(Collectors.toList())
                .isEmpty();
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        return userService.verifyAdministratorPassword(userModel.getUsername(), credentialInput.getChallengeResponse());
    }

    @Override
    public UserModel getUserById(String id, RealmModel realmModel) {
        return null;
    }

    @Override
    public UserModel getUserByUsername(String login, RealmModel realmModel) {
        UserModel returnValue = null;
        AdministratorDto administratorDto = userService.getAdmin(login);
        if (administratorDto != null) {
            returnValue = createUserModel(login, realmModel);
        }

        return returnValue;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realmModel) {
        return null;
    }

    private UserModel createUserModel(String login, RealmModel realmModel) {
        return new AbstractUserAdapter(keycloakSession, realmModel, componentModel) {
            @Override
            public String getUsername() {
                return login;
            }

            @Override
            public Set<RoleModel> getRoleMappings() {
                Set<RoleModel> set = new HashSet<>();
                RoleModel role = realmModel.getRole(userService.getAdmin(login).getRole().toString());
                set.add(role);
                return set;
            }
        };
    }

    private UserCredentialStore getCredentialStore() {
        return keycloakSession.userCredentialManager();
    }
}
