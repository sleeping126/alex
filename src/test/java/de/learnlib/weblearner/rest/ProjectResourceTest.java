package de.learnlib.weblearner.rest;

import de.learnlib.weblearner.WeblearnerTestApplication;
import de.learnlib.weblearner.dao.LearnerResultDAO;
import de.learnlib.weblearner.dao.ProjectDAO;
import de.learnlib.weblearner.dao.SymbolDAO;
import de.learnlib.weblearner.entities.Project;
import de.learnlib.weblearner.entities.Symbol;
import de.learnlib.weblearner.learner.Learner;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ValidationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ProjectResourceTest extends JerseyTest {

    private static final int PROJECT_TEST_ID = 1;

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private SymbolDAO symbolDAO;

    @Mock
    private LearnerResultDAO learnerResultDAO;

    private Project project;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        Symbol symbol = new Symbol();
        symbol.setName("Project Resource Test Symbol");
        symbol.setAbbreviation("prts");

        project = new Project();
        project.setId(PROJECT_TEST_ID);
        project.setName("Test Project");
        project.addSymbol(symbol);
        given(projectDAO.getByID(PROJECT_TEST_ID, null)).willReturn(project);

        Learner learner = mock(Learner.class);

        return new WeblearnerTestApplication(projectDAO, symbolDAO, learnerResultDAO, learner, ProjectResource.class);
    }

    @Test
    public void shouldCreateAProject() {
        String json = "{\"id\": " + project.getId() + ","
                        + "\"name\": \"" + project.getName() + "\","
                        + "\"baseUrl\": \"" + project.getBaseUrl() + "\","
                        + "\"description\": \"" + project.getDescription() + "\"}";
        Response response = target("/projects").request().post(Entity.json(json));

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("http://localhost:9998/projects/1", response.getHeaderString("Location"));
        verify(projectDAO).create(project);
    }

    @Test
    public void shouldReturn400IfProjectCouldNotBeCreated() {
        willThrow(new ValidationException("Test Message")).given(projectDAO).create(project);

        Response response = target("/projects").request().post(Entity.json(project));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnAllProjectsWithoutEmbedded() {
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        given(projectDAO.getAll(null)).willReturn(projects);

        Response response = target("/projects").request().get();

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("1", response.getHeaderString("X-Total-Count"));
        verify(projectDAO).getAll(null);
    }

    @Test
    public void shouldReturnAllProjectsWithEmbedded() {
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        given(projectDAO.getAll("symbols", "testResults")).willReturn(projects);

        Response response = target("/projects").queryParam("embed", "symbols,testResults").request().get();

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("1", response.getHeaderString("X-Total-Count"));
        verify(projectDAO).getAll("symbols", "testResults");
    }

    @Test
    public void shouldGetTheRightProjectWithoutEmbeddedFields() {
        Response response = target("/projects/" + PROJECT_TEST_ID).request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        verify(projectDAO).getByID(PROJECT_TEST_ID, null);
    }

    @Test
    public void shouldGetTheRightProjectWithEmbedded() {
        given(projectDAO.getByID(PROJECT_TEST_ID, "symbols", "testResults")).willReturn(project);
        Response response = target("/projects/" + PROJECT_TEST_ID).queryParam("embed", "symbols,testResults")
                                .request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        verify(projectDAO).getByID(PROJECT_TEST_ID, "symbols", "testResults");
    }

    @Test
    public void shouldReturn404WhenProjectNotFound() {
        given(projectDAO.getByID(PROJECT_TEST_ID, null)).willReturn(null);

        Response response = target("/projects/" + PROJECT_TEST_ID).request().get();

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(projectDAO).getByID(PROJECT_TEST_ID, null);
    }

    @Test
    public void shouldUpdateTheRightProject() {
        String json = "{\"id\": " + project.getId() + ","
                        + "\"name\": \"" + project.getName() + "\","
                        + "\"baseUrl\": \"" + project.getBaseUrl() + "\","
                        + "\"description\": \"" + project.getDescription() + "\"}";

        target("/project").request().post(Entity.json(project));
        Response response = target("/projects/" + project.getId()).request().put(Entity.json(json));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        verify(projectDAO).update(project);
    }

    /*
    @Test
    public void shouldReturn404OnUpdateWhenProjectNotFound() {
        project.setId(PROJECT_TEST_ID);
        project.setName("Test Project - Update 404");

        willThrow(new IllegalArgumentException()).given(projectDAO).update(project);
        Response response = target("/projects/" + project.getId()).request().put(Entity.json(project));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
    */

    @Test
    public void shouldFailIfIdInUrlAndObjectAreDifferent() {
        project.setName("Test Project - Update Diff");
        target("/project").request().post(Entity.json(project));
        Response response = target("/projects/" + (project.getId() + 1)).request().put(Entity.json(project));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(projectDAO, never()).update(project);
    }

    @Test
    public void shouldReturn400OnUpdateWhenProjectIsInvalid() {
        project.setName("Test Project - Invalid Test");

        willThrow(new ValidationException()).given(projectDAO).update(project);
        Response response = target("/projects/" + project.getId()).request().put(Entity.json(project));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldDeleteTheRightProject() {
        target("/project").request().post(Entity.json(project));

        Response response = target("/projects/" + project.getId()).request().delete();
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

        verify(projectDAO).delete(project.getId());
    }

    @Test
    public void shouldReturn404OnDeleteWhenProjectNotFound() {
        willThrow(new IllegalArgumentException()).given(projectDAO).delete(PROJECT_TEST_ID);
        Response response = target("/projects/" + PROJECT_TEST_ID).request().delete();

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

}
