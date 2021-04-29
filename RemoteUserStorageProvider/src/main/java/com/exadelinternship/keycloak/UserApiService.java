package com.exadelinternship.keycloak;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/verify")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserApiService {

    @GET
    @Path("/{login}")
    AdministratorDto getAdmin(@PathParam("login") String login);

    @POST
    @Path("/{login}")
    boolean verifyAdministratorPassword(@PathParam("login") String login, String password);

}
