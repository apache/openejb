package org.apache.openejb.arquillian.remote;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author rmannibucau
 */
@Path("/rest")
public class TstRestService {
    @Path("{name}") @GET @Produces(MediaType.TEXT_PLAIN) public String hello(@PathParam("name") String name) {
        return "hello, " + name;
    }
}
