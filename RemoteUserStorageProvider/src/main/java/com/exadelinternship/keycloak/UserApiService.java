package com.exadelinternship.keycloak;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/verify")
//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public interface UserApiService {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{username}")
    AdministratorDto getAdmin(@PathParam("username") String username);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{username}/password")
    Boolean verifyAdministratorPassword(@PathParam("username") String username, String password);

}
