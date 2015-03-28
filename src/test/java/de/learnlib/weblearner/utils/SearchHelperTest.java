package de.learnlib.weblearner.utils;

import de.learnlib.weblearner.learner.connectors.CounterStoreConnector;
import de.learnlib.weblearner.learner.connectors.ConnectorManager;
import de.learnlib.weblearner.learner.connectors.VariableStoreConnector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SearchHelperTest {

    private static final Long PROJECT_ID = 10L;

    @Test
    public void shouldReplaceVariablesCorrectly() {
        VariableStoreConnector variables = mock(VariableStoreConnector.class);
        given(variables.get("name")).willReturn("Jon Doe");
        CounterStoreConnector counter = mock(CounterStoreConnector.class);
        given(counter.get(PROJECT_ID, "counter")).willReturn(42);
        ConnectorManager connector = mock(ConnectorManager.class);
        given(connector.getConnector(VariableStoreConnector.class)).willReturn(variables);
        given(connector.getConnector(CounterStoreConnector.class)).willReturn(counter);

        String result = SearchHelper.insertVariableValues(connector, PROJECT_ID,
                                                          "Hello {{$name}}, you are user no. {{#counter}}!");

        assertEquals("Hello Jon Doe, you are user no. 42!", result);
    }

    @Test
    public void shouldNotReplaceAnythingIfTextContainsNoVariables() {
        ConnectorManager connector = mock(ConnectorManager.class);

        String result = SearchHelper.insertVariableValues(connector, PROJECT_ID,
                                                          "Hello Jon Doe, you are user no. 42!");

        assertEquals("Hello Jon Doe, you are user no. 42!", result);
        verify(connector, never()).getConnector(any(Class.class));
    }
}
