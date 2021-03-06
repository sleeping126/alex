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
import de.learnlib.alex.core.repositories.SymbolActionRepository;
import de.learnlib.alex.core.repositories.SymbolGroupRepository;
import de.learnlib.alex.core.repositories.SymbolRepository;
import de.learnlib.alex.exceptions.NotFoundException;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SymbolGroupDAOImplTest {

    private static final long USER_ID    = 21L;
    private static final long PROJECT_ID = 42L;
    private static final long GROUP_ID   = 84L;
    private static final int TEST_GROUP_COUNT = 3;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SymbolGroupRepository symbolGroupRepository;

    @Mock
    private SymbolRepository symbolRepository;

    @Mock
    private SymbolActionRepository symbolActionRepository;

    private SymbolGroupDAO symbolGroupDAO;

    @Before
    public void setUp() {
        symbolGroupDAO = new SymbolGroupDAOImpl(projectRepository, symbolGroupRepository, symbolRepository,
                                                symbolActionRepository);
    }

    @Test
    public void shouldCreateAValidGroup() {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setUser(user);
        group.setProject(project);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.save(group)).willReturn(group);

        symbolGroupDAO.create(group);

        verify(symbolGroupRepository).save(group);
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleDataIntegrityViolationExceptionOnGroupCreationGracefully() {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setUser(user);
        group.setProject(project);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.save(group)).willThrow(DataIntegrityViolationException.class);

        symbolGroupDAO.create(group); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleTransactionSystemExceptionOnGroupCreationGracefully() {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setUser(user);
        group.setProject(project);
        //
        ConstraintViolationException constraintViolationException;
        constraintViolationException = new ConstraintViolationException("Project is not valid!", new HashSet<>());
        RollbackException rollbackException = new RollbackException("RollbackException", constraintViolationException);
        TransactionSystemException transactionSystemException;
        transactionSystemException = new TransactionSystemException("Spring TransactionSystemException",
                                                                    rollbackException);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.save(group)).willThrow(transactionSystemException);

        symbolGroupDAO.create(group); // should fail
    }

    @Test
    public void shouldGetAllGroupsOfAProject() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        List<SymbolGroup> groups = createGroupsList();
        //
        given(projectRepository.findOneByUser_IdAndId(user.getId(), PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.findAllByUser_IdAndProject_Id(USER_ID, PROJECT_ID)).willReturn(groups);

        List<SymbolGroup> allGroups = symbolGroupDAO.getAll(user, PROJECT_ID);

        assertThat(allGroups.size(), is(equalTo(groups.size())));
        for (SymbolGroup g : allGroups) {
            assertTrue(groups.contains(g));
        }
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowAnExceptionIfYouWantToGetAllGroupsOfANonExistingProject() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);

        symbolGroupDAO.getAll(user, -1L);
    }

    @Test
    public void shouldGetAGroupByItsID() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        //
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);

        SymbolGroup g = symbolGroupDAO.get(user, PROJECT_ID, group.getId());

        assertThat(g, is(equalTo(group)));
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowAnExceptionIfTheGroupCanNotBeFound() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);

        symbolGroupDAO.get(user, -1L, -1L); // should fail
    }

    @Test
    public void shouldUpdateAGroup() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        //
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);

        symbolGroupDAO.update(group);

        verify(symbolGroupRepository).save(group);
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleDataIntegrityViolationExceptionOnGroupUpdateGracefully() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        //
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);
        given(symbolGroupRepository.save(group)).willThrow(DataIntegrityViolationException.class);

        symbolGroupDAO.update(group); // should fail
    }

    @Test(expected = ValidationException.class)
    public void shouldHandleTransactionSystemExceptionOnGroupUpdateGracefully() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        //
        ConstraintViolationException constraintViolationException;
        constraintViolationException = new ConstraintViolationException("Project is not valid!", new HashSet<>());
        RollbackException rollbackException = new RollbackException("RollbackException", constraintViolationException);
        TransactionSystemException transactionSystemException;
        transactionSystemException = new TransactionSystemException("Spring TransactionSystemException",
                                                                    rollbackException);
        //
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);
        given(symbolGroupRepository.save(group)).willThrow(transactionSystemException);

        symbolGroupDAO.update(group); // should fail
    }

    @Test
    public void shouldDeleteAGroup() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);

        symbolGroupDAO.delete(user, PROJECT_ID, GROUP_ID);

        verify(symbolGroupRepository).delete(group);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotDeleteTheDefaultGroupOfAProject() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);
        //
        Project project = new Project();
        project.setId(PROJECT_ID);
        //
        SymbolGroup group = new SymbolGroup();
        group.setId(GROUP_ID);
        group.setUser(user);
        group.setProject(project);
        project.setDefaultGroup(group);
        //
        given(projectRepository.findOneByUser_IdAndId(USER_ID, PROJECT_ID)).willReturn(project);
        given(symbolGroupRepository.findOneByUser_IdAndProject_IdAndId(USER_ID, PROJECT_ID, GROUP_ID))
                                                                                                     .willReturn(group);

        symbolGroupDAO.delete(user, PROJECT_ID, GROUP_ID); // should fail
    }

    @Test(expected = NotFoundException.class)
    public void shouldFailToDeleteAProjectThatDoesNotExist() throws NotFoundException {
        User user = new User();
        user.setId(USER_ID);

        symbolGroupDAO.delete(user, PROJECT_ID, -1L);
    }


    private List<SymbolGroup> createGroupsList() {
        List<SymbolGroup> groups = new LinkedList<>();
        for (int i = 0; i  < TEST_GROUP_COUNT; i++) {
            SymbolGroup g = new SymbolGroup();
            groups.add(g);
        }
        return groups;
    }

}
