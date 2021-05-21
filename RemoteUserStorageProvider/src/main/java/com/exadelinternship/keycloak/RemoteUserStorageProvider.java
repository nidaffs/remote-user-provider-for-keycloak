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
import org.keycloak.models.utils.DefaultRoles;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoteUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, CredentialInputValidator {

    private KeycloakSession session;
    private ComponentModel componentModel;
    private UserApiService userService;

    public RemoteUserStorageProvider(KeycloakSession session, ComponentModel componentModel, UserApiService userService) {
        this.session = session;
        this.componentModel = componentModel;
        this.userService = userService;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        System.out.println("supportsCredentialType");
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel userModel, String credentialType) {
        if (!supportsCredentialType(credentialType)) {
            return false;
        }
        return !getCredentialStore()
                .getStoredCredentialsByTypeStream(realm, userModel, credentialType)
                .collect(Collectors.toList())
                .isEmpty();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel userModel, CredentialInput credentialInput) {
        System.out.println(userModel.getUsername());
        boolean valid = userService.verifyAdministratorPassword(userModel.getUsername(), credentialInput.getChallengeResponse());
        System.out.println(valid);
        System.out.println(userModel.getUsername());
        return valid;
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();

        return getUserByUsername(username, realm);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }


    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        UserModel returnValue = null;
        AdministratorDto administratorDto = userService.getAdmin(username);
        if (administratorDto != null) {
            returnValue = createUserModel(username, realm);
        }
        System.out.println("getUserByUsername");
        System.out.println(username);
        return returnValue;

    }

    private UserModel createUserModel(String username, RealmModel realm) {

        return new AbstractUserAdapter(session, realm, componentModel) {

            @Override
            public Set<RoleModel> getRoleMappings() {
                System.out.println("enter in getRoleMappings long");
                Set<RoleModel> rolesToAssign = new HashSet<>();
                System.out.println(userService.getAdmin(username));
                System.out.println(userService.getAdmin(username).getRoles());
                System.out.println(realm.getRole("SUPERADMIN"));

                userService.getAdmin(username).getRoles().stream().forEach(role -> rolesToAssign.add(realm.getRole(role)));

                return rolesToAssign;
            }

            @Override
            public String getUsername() {
                System.out.println("enter in getUsername");
                System.out.println(username);
                return username;
            }

            @Override
            public String getEmail() {
                System.out.println("enter in getEmail");
                System.out.println(userService.getAdmin(username).getEmail());
                return userService.getAdmin(username).getEmail();
            }

        };
    }

    private UserCredentialStore getCredentialStore() {
        return session.userCredentialManager();
    }
}
