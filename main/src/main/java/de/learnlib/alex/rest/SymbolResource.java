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

import de.learnlib.alex.actions.RESTSymbolActions.CallAction;
import de.learnlib.alex.core.dao.ProjectDAO;
import de.learnlib.alex.core.dao.SymbolDAO;
import de.learnlib.alex.core.entities.Project;
import de.learnlib.alex.core.entities.Symbol;
import de.learnlib.alex.core.entities.SymbolAction;
import de.learnlib.alex.core.entities.SymbolVisibilityLevel;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.exceptions.NotFoundException;
import de.learnlib.alex.security.UserPrincipal;
import de.learnlib.alex.utils.IdsList;
import de.learnlib.alex.utils.ResourceErrorHandler;
import de.learnlib.alex.utils.ResponseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.UnauthorizedException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * REST API to manage the symbols.
 *
 * @resourcePath symbols
 * @resourceDescription Operations about symbols
 */
@Path("/projects/{project_id}/symbols")
@RolesAllowed({"REGISTERED"})
public class SymbolResource {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Context information about the URI. */
    @Context
    private UriInfo uri;

    /** The {@link SymbolDAO} to use. */
    @Inject
    private SymbolDAO symbolDAO;

    /** The security context containing the user of the request. */
    @Context
    private SecurityContext securityContext;

    /** The {@link ProjectDAO} to use. */
    @Inject
    private ProjectDAO projectDAO;

    /**
     * Create a new Symbol.
     *
     * @param projectId The ID of the project the symbol should belong to.
     * @param symbol    The symbol to add.
     * @return On success the added symbol (enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the related Project or Group could not be found.
     * @responseType de.learnlib.alex.core.entities.Symbol
     * @successResponse 201 created
     * @errorResponse   400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSymbol(@PathParam("project_id") Long projectId, Symbol symbol) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("createSymbol({}, {}) for user {}.", projectId, symbol, user);

        try {
            checkSymbolBeforeCreation(symbol, projectId); // can throw an IllegalArgumentException

            Project project = projectDAO.getByID(user.getId(), projectId);
            if (project.getUser().equals(user)) {
                symbol.setUser(user);
                symbolDAO.create(symbol);

                String symbolURL = uri.getBaseUri() + "projects/" + symbol.getProjectId()
                        + "/symbols/" + symbol.getId();

                LOGGER.traceExit(symbol);
                return Response.status(Status.CREATED).header("Location", symbolURL).entity(symbol).build();
            } else {
                throw new UnauthorizedException("The user may not create a symbol in this project");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.createSymbol", Status.BAD_REQUEST, e);
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.createSymbol", Status.BAD_REQUEST, e);
        } catch (UnauthorizedException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.createSymbol", Status.UNAUTHORIZED, e);
        }
    }

    /**
     * Execute an action without creating a learning context.
     *
     * @param projectId The id of the project.
     * @param action    The action to test
     * @return The result of the executed action.
     * @throws NotFoundException If the related Project could not be found.
     */
    @POST
    @Path("/actions/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response testSymbolAction(@PathParam("project_id") Long projectId, SymbolAction action)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("testSymbolAction({}, {}) for user {}.", projectId, action, user);

        Project project = projectDAO.getByID(user.getId(), projectId);
        if (action instanceof CallAction) { // other actions might be worth testing, too.
            CallAction callAction = (CallAction) action;
            CallAction.TestResult result = callAction.testRequest(project.getBaseUrl());
            return Response.ok(result).build();
        } else {
            return Response.noContent().build();
        }
    }

    /**
     * Create a bunch of new Symbols.
     *
     * @param projectId The ID of the project the symbol should belong to.
     * @param symbols   The symbols to add.
     * @return On success the added symbols (enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the related Projects or Groups could not be found.
     * @responseType java.util.List<de.learnlib.alex.core.entities.Symbol>
     * @successResponse 201 created
     * @errorResponse   400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchCreateSymbols(@PathParam("project_id") Long projectId, List<Symbol> symbols)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("batchCreateSymbols({}, {}) for user {}.", projectId, symbols, user);

        try {
            Project project = projectDAO.getByID(user.getId(), projectId);
            if (project.getUser().equals(user)) {
                for (Symbol symbol : symbols) {
                    checkSymbolBeforeCreation(symbol, projectId); // can throw an IllegalArgumentException
                    symbol.setUser(user);
                }
                symbolDAO.create(symbols);

                LOGGER.traceExit(symbols);
                return ResponseHelper.renderList(symbols, Status.CREATED);
            } else {
                throw new UnauthorizedException("The user may not create the symbols in this project");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.batchCreateSymbols",
                                                               Status.BAD_REQUEST, e);
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.batchCreateSymbols",
                                                               Status.BAD_REQUEST, e);
        } catch (UnauthorizedException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.createSymbol", Status.UNAUTHORIZED, e);
        }
    }

    private void checkSymbolBeforeCreation(Symbol symbol, Long projectId) {
        if (symbol.getProjectId() == null) {
            symbol.setProjectId(projectId);
        } else if (!Objects.equals(symbol.getProjectId(), projectId)) {
            throw new IllegalArgumentException("The symbol should not have a project"
                    + " or at least the project id should be the one provided via the get parameter");
        }
    }

    /**
     * Get all the Symbols of a specific Project.
     *
     * @param projectId       The ID of the project.
     * @param visibilityLevel Specify the visibility level of the symbols you want to get.
     *                        Valid values are: 'all'/ 'unknown', 'visible', 'hidden'.
     *                        Optional.
     * @return A list of all Symbols belonging to the project. This list can be empty.
     * @responseType java.util.List<de.learnlib.alex.core.entities.Symbol>
     * @successResponse 200 OK
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@PathParam("project_id") Long projectId,
                           @QueryParam("visibility") @DefaultValue("VISIBLE") SymbolVisibilityLevel visibilityLevel) {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("getAll({}, {}) for user {}.", projectId, visibilityLevel, user);

        List<Symbol> symbols;
        try {
            symbols = symbolDAO.getAll(user, projectId, visibilityLevel);
        } catch (NotFoundException e) {
            symbols = new LinkedList<>();
        }

        LOGGER.traceExit(symbols);
        return ResponseHelper.renderList(symbols, Status.OK);
    }

    /**
     * Get Symbols by a list of ids.
     *
     * @param projectId       The ID of the project
     * @param ids The non empty list of ids.
     * @return A list of the symbols whose ids were given
     * @throws NotFoundException If the requested Symbols or the related Projects or Groups could not be found.
     * @responseType java.util.List<de.learnlib.alex.core.entities.Symbol>
     * @successResponse 200 OK
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @GET
    @Path("/batch/{ids}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByIdRevisionPairs(@PathParam("project_id") Long projectId,
                                         @PathParam("ids") IdsList ids)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("getByIdRevisionPairs({}, {}) for user {}.", projectId, ids, user);

        List<Symbol> symbols = symbolDAO.getByIds(user, projectId, ids);

        LOGGER.traceExit(symbols);
        return ResponseHelper.renderList(symbols, Status.OK);
    }

    /**
     * Get a Symbol by its ID.
     *
     * @param projectId The ID of the project.
     * @param id        The ID of the symbol.
     * @return A Symbol matching the projectID & ID or a not found response.
     * @throws NotFoundException If the requested Symbol or the related Project or Group could not be found.
     * @responseType de.learnlib.alex.core.entities.Symbol
     * @successResponse 200 OK
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("project_id") Long projectId, @PathParam("id") Long id) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("get({}, {})  for user {}.", projectId, id, user);

        Symbol symbol = symbolDAO.get(user, projectId, id);

        LOGGER.traceExit(symbol);
        return Response.ok(symbol).build();
    }

    /**
     * Update a Symbol.
     *
     * @param projectId The ID of the project.
     * @param id        The ID of the symbol.
     * @param symbol    The new symbol data.
     * @return On success the updated symbol (maybe enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the given Symbol or the related Project or Groups could not be found.
     * @responseType de.learnlib.alex.core.entities.Symbol
     * @successResponse 200 OK
     * @errorResponse   400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("project_id") Long projectId, @PathParam("id") Long id, Symbol symbol)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("update({}, {}, {}) for user {}.", projectId, id, symbol, user);

        if (symbolDoesntMatchURLParameter(symbol, projectId, id, user)) {
            LOGGER.traceExit();
            return Response.status(Status.BAD_REQUEST).build();
        }
        symbol.setUser(user);
        symbol.setProjectId(projectId);

        try {
            symbolDAO.update(symbol);

            LOGGER.traceExit(symbol);
            return Response.ok(symbol).build();
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.update", Status.BAD_REQUEST, e);
        }
    }

    private boolean symbolDoesntMatchURLParameter(Symbol symbol, Long projectId, Long id, User user) {
        if (symbol.getId() != null
                && symbol.getId() != 0
                && !Objects.equals(id, symbol.getId())) {
            return true;
        }

        if (symbol.getProjectId() != null
                && symbol.getProjectId() != 0L
                && !Objects.equals(projectId, symbol.getProjectId())) {
            return true;
        }

        if (symbol.getUserId() != null
                && symbol.getUserId() != 0L
                && !Objects.equals(user.getId(), symbol.getUserId())) {
            return true;
        }

        return false;
    }

    /**
     * Update a bunch of Symbols.
     *
     * @param projectId The ID of the project.
     * @param ids       The IDs of the symbols.
     * @param symbols   The new symbol data.
     * @return On success the updated symbol (maybe enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the given Symbols or the related Projects or Groups could not be found.
     * @responseType de.learnlib.alex.core.entities.Symbol
     * @successResponse 200 OK
     * @errorResponse   400 bad request `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @PUT
    @Path("/batch/{ids}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response batchUpdate(@PathParam("project_id") Long projectId,
                                @PathParam("ids") IdsList ids,
                                List<Symbol> symbols) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("batchUpdate({}, {}, {}) for user {}.", projectId, ids, symbols, user);

        Set idsSet = new HashSet<>(ids);
        for (Symbol symbol : symbols) {
            if (!idsSet.contains(symbol.getId())
                    || !Objects.equals(projectId, symbol.getProjectId())
                    || !symbol.getUserId().equals(user.getId())) {
                LOGGER.traceExit();
                return Response.status(Status.BAD_REQUEST).build();
            }
        }
        try {
            symbolDAO.update(symbols);

            LOGGER.traceExit(symbols);
            return ResponseHelper.renderList(symbols, Status.OK);
        } catch (ValidationException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.batchUpdate", Status.BAD_REQUEST, e);
        }
    }

    /**
     * Move a Symbol to a new group.
     *
     * @param projectId The ID of the project.
     * @param symbolId  The ID of the symbol.
     * @param groupId   The ID of the new group.
     * @return On success the moved symbol (enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the requested Symbols or the related Project or Groups could not be found.
     */
    @PUT
    @Path("/{symbol_id}/moveTo/{group_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveSymbolToAnotherGroup(@PathParam("project_id") Long projectId,
                                             @PathParam("symbol_id")  Long symbolId,
                                             @PathParam("group_id")   Long groupId)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("moveSymbolToAnotherGroup({}, {}, {}) for user {}.", projectId, symbolId, groupId, user);

        Symbol symbol = symbolDAO.get(user, projectId, symbolId);
        symbolDAO.move(symbol, groupId);

        LOGGER.traceExit(symbol);
        return Response.ok(symbol).build();
    }

    /**
     * Move a bunch of Symbols to a new group.
     *
     * @param projectId The ID of the project.
     * @param symbolIds The ID of the symbols.
     * @param groupId   The ID of the new group.
     * @return On success the moved symbols (enhanced with information from the DB); An error message on failure.
     * @throws NotFoundException If the requested Symbols or the related Project or Groups could not be found.
     */
    @PUT
    @Path("/batch/{symbol_ids}/moveTo/{group_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveSymbolToAnotherGroup(@PathParam("project_id") Long projectId,
                                             @PathParam("symbol_ids") IdsList symbolIds,
                                             @PathParam("group_id") Long groupId)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("moveSymbolToAnotherGroup({}, {}, {}) for user {}.", projectId, symbolIds, groupId, user);

        List<Symbol> symbols = symbolDAO.getByIds(user, projectId, SymbolVisibilityLevel.ALL, symbolIds);
        symbolDAO.move(symbols, groupId);

        LOGGER.traceExit(symbols);
        return Response.ok(symbols).build();
    }

    /**
     * Mark one symbol as hidden.
     *
     * @param projectId The ID of the project.
     * @param id        The ID of the symbol to hide.
     * @return On success no content will be returned; an error message on failure.
     * @throws NotFoundException If the requested Symbol or the related Project or Group could not be found.
     * @successResponse 204 OK & no content
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Path("/{id}/hide")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hide(@PathParam("project_id") Long projectId, @PathParam("id") Long id) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("hide({}, {}) for user {}.", projectId, id, user);

        try {
            Symbol s = symbolDAO.get(user, projectId, id);
            if (s.getUser().equals(user)) {
                symbolDAO.hide(user.getId(), projectId, Collections.singletonList(id));
                Symbol symbol = symbolDAO.get(user, projectId, id);

                LOGGER.traceExit(symbol);
                return Response.ok(symbol).build();
            } else {
                throw new UnauthorizedException("The symbol does not belong to the user");
            }
        } catch (UnauthorizedException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("SymbolResource.hide", Status.UNAUTHORIZED, e);
        }
    }

    /**
     * Mark a bunch of symbols as hidden.
     *
     * @param projectId The ID of the project.
     * @param ids       The IDs of the symbols to hide.
     * @return On success no content will be returned; an error message on failure.
     * @throws NotFoundException If the requested Symbols or the related Project or Groups could not be found.
     * @successResponse 204 OK & no content
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Path("/batch/{ids}/hide")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hide(@PathParam("project_id") long projectId, @PathParam("ids") IdsList ids)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("hide({}, {}) for user {}.", projectId, ids, user);

        List<Symbol> symbols = symbolDAO.getByIds(user, projectId, SymbolVisibilityLevel.ALL, ids);
        symbolDAO.hide(user.getId(), projectId, ids);

        LOGGER.traceExit(symbols);
        return ResponseHelper.renderList(symbols, Status.OK);
    }

    /**
     * Remove the hidden flag from a symbol.
     *
     * @param projectId The ID of the project.
     * @param id        The ID of the symbol to show.
     * @return On success no content will be returned; an error message on failure.
     * @throws NotFoundException If the requested Symbol or the related Project or Group could not be found.
     * @successResponse 204 OK & no content
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Path("/{id}/show")
    @Produces(MediaType.APPLICATION_JSON)
    public Response show(@PathParam("project_id") long projectId, @PathParam("id") Long id) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("show({}, {}) for user {}.", projectId, id, user);

        symbolDAO.show(user.getId(), projectId, Collections.singletonList(id));
        Symbol symbol = symbolDAO.get(user, projectId, id);

        LOGGER.traceExit(symbol);
        return Response.ok(symbol).build();
    }

    /**
     * Remove the hidden flag from a bunch of symbols.
     *
     * @param projectId The ID of the project.
     * @param ids       The IDs of the symbols to show.
     * @return On success no content will be returned; an error message on failure.
     * @throws NotFoundException If the requested Symbols or the related Project or Groups could not be found.
     * @successResponse 204 OK & no content
     * @errorResponse   404 not found `de.learnlib.alex.utils.ResourceErrorHandler.RESTError
     */
    @POST
    @Path("/batch/{ids}/show")
    @Produces(MediaType.APPLICATION_JSON)
    public Response show(@PathParam("project_id") long projectId, @PathParam("ids") IdsList ids)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("show({}, {}) for user {}.", projectId, ids, user);

        symbolDAO.show(user.getId(), projectId, ids);
        List<Symbol> symbols = symbolDAO.getByIds(user, projectId, SymbolVisibilityLevel.ALL, ids);

        LOGGER.traceExit(symbols);
        return ResponseHelper.renderList(symbols, Status.OK);
    }

}
