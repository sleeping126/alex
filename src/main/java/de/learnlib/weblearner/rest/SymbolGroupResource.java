package de.learnlib.weblearner.rest;

import de.learnlib.weblearner.dao.SymbolGroupDAO;
import de.learnlib.weblearner.entities.Project;
import de.learnlib.weblearner.entities.Symbol;
import de.learnlib.weblearner.entities.SymbolGroup;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedList;
import java.util.List;

@Path("/projects/{project_id}/groups")
public class SymbolGroupResource {

    /** Context information about the URI. */
    @Context
    private UriInfo uri;

    @Inject
    private SymbolGroupDAO symbolGroupDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(@PathParam("project_id") long projectId, SymbolGroup group) {
        group.setProjectId(projectId);
        symbolGroupDAO.create(group);

        String groupURL = uri.getBaseUri() + "projects/" + group.getProjectId() + "/groups/" + group.getId();
        return Response.status(Response.Status.CREATED).header("Location", groupURL).entity(group).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@PathParam("project_id") long projectId, @QueryParam("embed") String embed) {
        System.out.println("fjdfgjdflkg");

        List<SymbolGroup> groups = symbolGroupDAO.getAll(projectId);
        System.out.println(groups);

        return Response.ok(groups).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("project_id") long projectId, @PathParam("id") Long id) {
        SymbolGroup group = symbolGroupDAO.get(projectId, id);
        return Response.ok(group).build();
    }

    @GET
    @Path("/{id}/symbols")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSymbols(@PathParam("project_id") long projectId, @PathParam("id") Long id) {
        LinkedList<Symbol> symbols = new LinkedList<>();
        for (int i = 1; i <= 10; i++) {
            Symbol newSymbol = new Symbol();
            newSymbol.setId(i);
            newSymbol.setName("Symbol " + i);
            newSymbol.setAbbreviation("symb_" + i);
            symbols.add(newSymbol);
        }

        return Response.ok(symbols).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("project_id") long projectId, @PathParam("id") Long id, SymbolGroup group) {
        symbolGroupDAO.update(group);
        return Response.ok(group).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAResultSet(@PathParam("project_id") long projectId,  @PathParam("id") Long id) {
        symbolGroupDAO.delete(projectId, id);
        return Response.noContent().build();
    }
}
