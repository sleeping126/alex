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

import de.learnlib.alex.core.entities.Project;
import de.learnlib.alex.core.entities.SymbolGroup;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.core.repositories.ProjectRepository;
import de.learnlib.alex.core.repositories.SymbolRepository;
import de.learnlib.alex.exceptions.NotFoundException;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDAOImplTest {

    private static final long USER_ID    = 21L;
    private static final long PROJECT_ID = 42L;
    private static final int TEST_PROJECT_COUNT = 3;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SymbolRepository symbolRepository;

    @Mock
    private SymbolDAOImpl symbolDAO;

    @Mock
    private FileDAOImpl fileDAO;

    private ProjectDAO projectDAO;

    @Before
    public void setUp() {
        projectDAO = new ProjectDAOImpl(projectRepository, symbolRepository, symbolDAO, fileDAO);
    }

    @Test
    public void shouldCreateAValidEmptyProject() {
        Project project = new Project();
        Project createdProject = new Project();
        createdProject.setId(1L);
        //
        given(projectRepository.save(project)).willReturn(createdProject);

        projectDAO.create(project);

        verify(projectRepository).save(project);
        assertThat(project.getId(), is(equalTo(1L)));
        assertNotNull(project.getDefaultGroup());
    }

    @Test
    public void shouldCreateAValidPreFilledProject() {
        SymbolGroup testGroup = new SymbolGroup();
        //
        Project project = new Project();
        //
        testGroup.setProject(project);
        project.getGroups().add(testGroup);
        //
        given(projectRepository.save(project)).willReturn(project);

        projectDAO.create(project);

        verify(projectRepository).save(project);
        assertThat(project.getDefaultGroup(), is(equalTo(testGroup)));
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleConstraintViolationExceptionOnProjectCreationGracefully() {
        Project project = new Project();
        //
        given(projectRepository.save(project)).willThrow(ConstraintViolationException.class);

        projectDAO.create(project); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleDataIntegrityViolationExceptionOnProjectCreationGracefully() {
        Project project = new Project();
        //
        given(projectRepository.save(project)).willThrow(DataIntegrityViolationException.class);

        projectDAO.create(project); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleTransactionSystemExceptionOnProjectCreationGracefully() {
        Project project = new Project();
        //
        ConstraintViolationException constraintViolationException;
        constraintViolationException = new ConstraintViolationException("Project is not valid!", new HashSet<>());
        RollbackException rollbackException = new RollbackException("RollbackException", constraintViolationException);
        TransactionSystemException transactionSystemException;
        transactionSystemException = new TransactionSystemException("Spring TransactionSystemException",
                                                                    rollbackException);
        given(projectRepository.save(project)).willThrow(transactionSystemException);

        projectDAO.create(project); // should fail
    }

    @Test
    public void shouldGetAllProjectsOfAnUser() {
        User user = new User();
        user.setId(USER_ID);
        //
        List<Project> projects = createProjectList();
        given(projectRepository.findAllByUser_Id(USER_ID)).willReturn(projects);

        List<Project> allProjects = projectDAO.getAll(user);

        MatcherAssert.assertThat(allProjects.size(), is(equalTo(projects.size())));
        for (Project p : allProjects) {
            assertTrue(projects.contains(p));
        }
    }

    @Test
    public void shouldGetAProjectByItsID() throws NotFoundException {
        Project project = new Project();
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        Project p = projectDAO.getByID(USER_ID, PROJECT_ID);

        assertThat(p, is(equalTo(project)));
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowAnExceptionIfTheProjectCanNotFoundByID() throws NotFoundException {
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(null);

        projectDAO.getByID(USER_ID, PROJECT_ID); // should fail
    }

    @Test
    public void shouldGetAProjectByItsName() throws NotFoundException {
        Project project = new Project();
        //
        given(projectRepository.findOneByUser_IdAndName(USER_ID, "Test Project")).willReturn(project);

        Project p = projectDAO.getByName(USER_ID, "Test Project");

        assertThat(p, is(equalTo(project)));
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowAnExceptionIfTheProjectCanNotFoundByName() throws NotFoundException {
        given(projectRepository.findOneByUser_IdAndName(USER_ID, "Test Project")).willReturn(null);

        projectDAO.getByName(USER_ID, "Test Project"); // should fail
    }

    @Test
    public void shouldUpdateAProject() throws NotFoundException {
        Project project = new Project();
        project.setUserId(USER_ID);
        project.setId(PROJECT_ID);
        project.setDefaultGroup(new SymbolGroup());
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        projectDAO.update(project);

        verify(projectRepository).save(project);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowANotFoundExceptionWhenUpdatingAUnknownProject() throws NotFoundException {
        Project project = new Project();
        project.setUserId(USER_ID);
        project.setId(PROJECT_ID);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(null);

        projectDAO.update(project);
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleConstraintViolationExceptionOnProjectUpdateGracefully() throws NotFoundException {
        Project project = new Project();
        project.setUserId(USER_ID);
        project.setId(PROJECT_ID);
        project.setDefaultGroup(new SymbolGroup());
        //
        given(projectRepository.save(project)).willThrow(ConstraintViolationException.class);
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        projectDAO.update(project); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleDataIntegrityViolationExceptionOnProjectUpdateGracefully() throws NotFoundException {
        Project project = new Project();
        project.setUserId(USER_ID);
        project.setId(PROJECT_ID);
        project.setDefaultGroup(new SymbolGroup());
        //
        given(projectRepository.save(project)).willThrow(DataIntegrityViolationException.class);
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        projectDAO.update(project); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleTransactionSystemExceptionOnProjectUpdateGracefully() throws NotFoundException {
        Project project = new Project();
        project.setUserId(USER_ID);
        project.setId(PROJECT_ID);
        project.setDefaultGroup(new SymbolGroup());
        //
        ConstraintViolationException constraintViolationException;
        constraintViolationException = new ConstraintViolationException("Project is not valid!", new HashSet<>());
        RollbackException rollbackException = new RollbackException("RollbackException", constraintViolationException);
        TransactionSystemException transactionSystemException;
        transactionSystemException = new TransactionSystemException("Spring TransactionSystemException",
                                                                    rollbackException);
        given(projectRepository.save(project)).willThrow(transactionSystemException);
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        projectDAO.update(project); // should fail
    }

    @Test
    public void shouldDeleteAProject() throws NotFoundException {
        Project project = new Project();
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);

        projectDAO.delete(USER_ID, PROJECT_ID);

        verify(projectRepository).delete(project);
    }

    @Test(expected = NotFoundException.class)
    public void shouldFailToDeleteAProjectThatDoesNotExist() throws NotFoundException {
        projectDAO.delete(USER_ID, -1L);
    }


    private List<Project> createProjectList() {
        List<Project> projects = new LinkedList<>();
        for (int i = 0; i  < TEST_PROJECT_COUNT; i++) {
            Project p = new Project();
            projects.add(p);
        }
        return projects;
    }

}
