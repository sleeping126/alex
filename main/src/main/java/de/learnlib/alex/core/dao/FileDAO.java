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

package de.learnlib.alex.core.dao;

import de.learnlib.alex.core.entities.UploadableFile;
import de.learnlib.alex.exceptions.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

/**
 * Interface to describe how Files are handled.
 */
public interface FileDAO {

    /**
     * Put a new file into the file system.
     *
     * @param userId
     *         The user the file belongs to.
     * @param projectId
     *         The id of the project that the file belongs to.
     * @param uploadedInputStream
     *         The file as input stream.
     * @param fileDetail
     *         Other file (meta) data.
     * @throws IOException
     *         If something during the actual writing went wrong.
     * @throws IllegalStateException
     *         If the file already exists or the destination directory is not a directory or otherwise blocked.
     */
    void create(Long userId, Long projectId, InputStream uploadedInputStream, FormDataContentDisposition fileDetail)
            throws IOException, IllegalStateException;

    /**
     * Get a list of all fiels of a user within a project.
     *
     * @param userId
     *         The user to show the files of.
     * @param projectId
     *         The project the files belong to.
     * @return A List of Files. Can be empty.
     * @throws NotFoundException
     *         If no files can be found.
     */
    List<UploadableFile> getAll(Long userId, Long projectId) throws NotFoundException;

    /**
     * Get the absolute path to a file on the machine.
     *
     * @param userId
     *         The user the file belongs to.
     * @param projectId
     *         The id of the project that the file belongs to.
     * @param fileName
     *         The name of the file.
     * @return The absolute path to the file on the actual machine.
     * @throws NotFoundException
     *         If the file could not be found.
     */
    String getAbsoluteFilePath(Long userId, Long projectId, String fileName) throws NotFoundException;

    /**
     * Deletes a file.
     *
     * @param userId
     *         The user the file belongs to.
     * @param projectId
     *         The id of the project that the file belongs to.
     * @param fileName
     *         The name of the file to delete.
     * @throws NotFoundException
     *         If the file could not be found.
     */
    void delete(Long userId, Long projectId, String fileName) throws NotFoundException;

    /**
     * Deletes the project directory.
     *
     * @param userId
     *         The id of the user.
     * @param projectId
     *         The id of the project.
     * @throws IOException
     *         If the directory could not be deleted.
     */
    void deleteProjectDirectory(Long userId, Long projectId) throws IOException;

    /**
     * Deletes the user directory.
     *
     * @param userId
     *         The id of the user.
     * @throws IOException
     *         If the directory could not be deleted.
     */
    void deleteUserDirectory(Long userId) throws IOException;
}
