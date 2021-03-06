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

import de.learnlib.alex.core.dao.FileDAO;
import de.learnlib.alex.core.entities.UploadableFile;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.exceptions.NotFoundException;
import de.learnlib.alex.security.UserPrincipal;
import de.learnlib.alex.utils.ResourceErrorHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * REST API to manage files.
 * @resourcePath files
 * @resourceDescription Operations about files
 */
@Path("/projects/{project_id}/files")
@RolesAllowed({"REGISTERED"})
public class FileResource {

    private static final Logger LOGGER = LogManager.getLogger();

    /** The security context containing the user of the request. */
    @Context
    private SecurityContext securityContext;

    /** The FileDAO to use. */
    @Inject
    private FileDAO fileDAO;

    /**
     * Uploads a new file to the corresponding upload directory uploads/{userId}/{projectId}/{filename}.
     *
     * @param projectId The id of the project the file belongs to.
     * @param uploadedInputStream The input stream for the file.
     * @param fileDetail The form data of the file.
     * @return The HTTP response with the file object on success.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@PathParam("project_id") Long projectId,
                               @FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileDetail) {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("uploadFile({}, {}, {}) for user {}.", projectId, uploadedInputStream, fileDetail, user);

        try {
            fileDAO.create(user.getId(), projectId, uploadedInputStream, fileDetail);

            UploadableFile result = new UploadableFile();
            result.setName(fileDetail.getFileName());
            result.setProjectId(projectId);

            LOGGER.traceExit(result);
            return Response.ok(result).build();
        } catch (IllegalArgumentException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("FileResource.uploadFile", Response.Status.NOT_FOUND, e);
        } catch (IOException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("FileResource.uploadFile",
                                                               Response.Status.INTERNAL_SERVER_ERROR, e);
        } catch (IllegalStateException e) {
            LOGGER.traceExit(e);
            return ResourceErrorHandler.createRESTErrorMessage("FileResource.uploadFile",
                                                               Response.Status.BAD_REQUEST, e);
        }
    }

    /**
     * Get all available files of a project.
     *
     * @param projectId The id of the project.
     * @return The list of all files of the project.
     * @throws NotFoundException If the related Project could not be found.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFiles(@PathParam("project_id") Long projectId) throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("getAllFiles({}) for user {}.", projectId, user);

        List<UploadableFile> allFiles = fileDAO.getAll(user.getId(), projectId);

        LOGGER.traceExit(allFiles);
        return Response.ok(allFiles).build();
    }

    /**
     * Delete a single file from the project directory.
     *
     * @param projectId The id of the project.
     * @param fileName The name of the file.
     * @return Status 204 No Content on success.
     * @throws NotFoundException If the related Project could not be found.
     */
    @DELETE
    @Path("/{file_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteOneFile(@PathParam("project_id") Long projectId, @PathParam("file_name") String fileName)
            throws NotFoundException {
        User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
        LOGGER.traceEntry("deleteOneFile({}, {}) for user {}.", projectId, fileName, user);

        fileDAO.delete(user.getId(), projectId, fileName);

        LOGGER.traceExit("File deleted.");
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
