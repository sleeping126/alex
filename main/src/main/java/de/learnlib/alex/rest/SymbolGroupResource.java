/*
 * Copyright 2016 TU Dortmund
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.learnlib.alex.rest;

import de.learnlib.alex.core.dao.ProjectDAO;
import de.learnlib.alex.core.dao.SymbolDAO;
import de.learnlib.alex.core.dao.SymbolGroupDAO;
import de.learnlib.alex.core.entities.Project;
import de.learnlib.alex.core.entities.Symbol;
import de.learnlib.alex.core.entities.SymbolGroup;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.exceptions.NotFoundException;
import de.learnlib.alex.security.UserPrincipal;
import de.learnlib.alex.utils.ResourceErrorHandler;
import de.learnlib.alex.utils.ResponseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.UnauthorizedException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ValidationException;
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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * REST API to manage groups.
 *
 * @resourcePath groups
 * @resourceDescription Operations for groups
 */
@Path("/projects/{project_id}/groups")
@RolesAllowed("REGISTERED")
public class SymbolGroupResource {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Context information about the URI. */
    @Context
    private UriInfo uri;

    /** The SymbolGroupDAO to use. */
    @Inject
    private SymbolGroupDAO symbolGroupDAO;

    /** The SymbolDAO to use. */
    @Inject
    private SymbolDAO symbolDAO;

    /** The ProjectDAO to use. */
    @Inject
    private ProjectDAO projectDAO;

    /** The security context containing the user of the request. */
    @Context
    private SecurityContext securityContext;

    /**
     * Create a new group.
     *
     * @param projectId
     *         The ID of the project.
     * @param group
     *         The group to create.
     * @return On success the added group (enhanced with information from the DB); an error message on failure.
     * @throws NotFoundException If the related Project could not be found.
     * @responseType de.learnlib.alex.core.entities.SymbolGroup
     * @successResponse 201 created
     * @errorResponse 400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(@PathParam("project_id") long projectId, SymbolGroup group) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("createGroup({}, {}) for user {}.", projectId, group, user);

        try {
            Project project = projectDAO.getByID(user.getId(), projectId);
            if (!project.getUser().equals(user)) {
                throw new ValidationException("You are not the owner of the project");
            }

            group.setProjectId(projectId);
            group.setUser(user);
            symbolGroupDAO.create(group);

            LOGGER.traceExit(group);
            String groupURL = uri.getBaseUri() + "projects/" + group.getProjectId() + "/groups/" + group.getId();
            return Response.status(Response.Status.CREATED).header("Location", groupURL).entity(group).build();
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.create",
                                                               Response.Status.BAD_REQUEST, e);
        }
    }

    /**
     * Get a list of all groups within on projects.
     *
     * @param projectId
     *         The ID of the project.
     * @param embed
     *         The properties to embed in the response.
     * @return All groups in a list. If the project contains no groups the list will be empty.
     * @throws NotFoundException If the related Project could not be found.
     * @responseType java.util.List<de.learnlib.alex.core.entities.SymbolGroup>
     * @successResponse 200 OK
     * @errorResponse 400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     * @errorResponse 404 not found   `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@PathParam("project_id") long projectId, @QueryParam("embed") String embed)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("getAll({}, {}) for user {}.", projectId, embed, user);

        try {
            SymbolGroupDAO.EmbeddableFields[] embeddableFields = parseEmbeddableFields(embed);
            List<SymbolGroup> groups = symbolGroupDAO.getAll(user, projectId, embeddableFields);

            LOGGER.traceExit(groups);
            return ResponseHelper.renderList(groups, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.getAll",
                                                               Response.Status.BAD_REQUEST, e);
        }
    }

    /**
     * Get a one group.
     *
     * @param projectId
     *         The ID of the project.
     * @param id
     *            The ID of the group within the project.
     * @param embed
     *         The properties to embed in the response.
     * @return The requested group.
     * @throws NotFoundException If the related Project could not be found.
     * @responseType de.learnlib.alex.core.entities.SymbolGroup
     * @successResponse 200 OK
     * @errorResponse   400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     * @errorResponse   404 not found   `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("project_id") long projectId,
                        @PathParam("id") Long id,
                        @QueryParam("embed") String embed)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("get({}, {}, {}) for user {}.", projectId, id, embed, user);

        try {
            SymbolGroupDAO.EmbeddableFields[] embeddableFields = parseEmbeddableFields(embed);
            SymbolGroup group = symbolGroupDAO.get(user, projectId, id, embeddableFields);

            LOGGER.traceExit(group);
            return Response.ok(group).build();
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.get",
                                                               Response.Status.BAD_REQUEST, e);
        }
    }

    /**
     * Not implemented yet.
     * Returns just dummy values.
     *
     * @param projectId
     *         The ID of the project.
     * @param id
     *         The ID of the group within the project.
     * @return The list of symbols within one group.
     * @throws NotFoundException If the related Project or Group could not be found.
     * @successResponse 200 OK
     * @responseType java.util.List<de.learnlib.alex.core.entities.Symbol>
     * @errorResponse 404 not found   `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @GET
    @Path("/{id}/symbols")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSymbols(@PathParam("project_id") long projectId, @PathParam("id") Long id)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("getSymbols({}, {}) for user {}.", projectId, id, user);

        List<Symbol> symbols = symbolDAO.getAll(user, projectId, id);

        LOGGER.traceExit(symbols);
        return ResponseHelper.renderList(symbols, Response.Status.OK);
    }

    /**
     * Update a group.
     *
     * @param projectId
     *         The ID of the project.
     * @param id
     *         The ID of the group within the project.
     * @param group
     *         The new values
     * @return On success the updated group (enhanced with information from the DB).
     * @throws NotFoundException If the related Project could not be found.
     * @responseType de.learnlib.alex.core.entities.SymbolGroup
     * @successResponse 200 OK
     * @errorResponse 400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     * @errorResponse 404 not found   `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("project_id") long projectId, @PathParam("id") Long id, SymbolGroup group)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("update({}, {}, {}) for user {}.", projectId, id, group, user);

        try {
            if (group.getUserId().equals(user.getId())) {
                symbolGroupDAO.update(group);

                LOGGER.traceExit(group);
                return Response.ok(group).build();
            } else {
                throw new UnauthorizedException("You are not allowed to edit this group");
            }
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.update",
                                                               Response.Status.BAD_REQUEST, e);
        } catch (UnauthorizedException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.update",
                                                                Response.Status.UNAUTHORIZED, e);
        }
    }

    /**
     * Delete a group.
     *
     * @param projectId
     *         The ID of the project.
     * @param id
     *         The ID of the group within the project.
     * @return On success no content will be returned.
     * @throws NotFoundException If the related Project could not be found.
     * @successResponse 204 OK & no content
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAResultSet(@PathParam("project_id") long projectId,  @PathParam("id") Long id)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("deleteAResultSet({}, {}) for user {}.", projectId, id, user);

        try {
            SymbolGroup group = symbolGroupDAO.get(user, projectId, id);

            if (group.getUserId().equals(user.getId())) {
                symbolGroupDAO.delete(user, projectId, id);

                LOGGER.traceExit("Group {} deleted.", id);
                return Response.noContent().build();
            } else {
                throw new UnauthorizedException("You are not allowed to delete the group");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.update",
                                                               Response.Status.BAD_REQUEST, e);
        } catch (UnauthorizedException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolGroupResource.update",
                                                                Response.Status.UNAUTHORIZED, e);
        }
    }

    private SymbolGroupDAO.EmbeddableFields[] parseEmbeddableFields(String embed) throws IllegalArgumentException {
        if (embed == null) {
            return new SymbolGroupDAO.EmbeddableFields[0];
        }

        String[] fields = embed.split(",");

        SymbolGroupDAO.EmbeddableFields[] embedFields = new SymbolGroupDAO.EmbeddableFields[fields.length];
        for (int i = 0; i < fields.length; i++) {
            embedFields[i] = SymbolGroupDAO.EmbeddableFields.fromString(fields[i]);
        }

        return embedFields;
    }
}
