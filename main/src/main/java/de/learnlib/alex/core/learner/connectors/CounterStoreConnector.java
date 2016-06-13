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

package de.learnlib.alex.core.learner.connectors;

import de.learnlib.alex.core.dao.CounterDAO;
import de.learnlib.alex.core.entities.Counter;
import de.learnlib.alex.core.entities.Project;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.exceptions.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Connector to store and manage counters.
 */
@Service
public class CounterStoreConnector implements Connector {

    /** Use the learner logger. */
    private static final Logger LOGGER = LogManager.getLogger("learner");

    /**
     * The DAO to persist the counters to and fetch the counters from.
     */
    private CounterDAO counterDAO;

    /**
     * Constructor.
     * @param counterDAO An instance of a counter dao.
     */
    @Inject
    public CounterStoreConnector(CounterDAO counterDAO) {
        this.counterDAO = counterDAO;
    }

    @Override
    public void reset() {
        // nothing to do here
    }

    @Override
    public void dispose() {
        // nothing to do here
    }

    /**
     * Set the value of an existing counter.
     * Creates a new counter implicitly with the specified name and value if it does not exist yet.
     *
     * @param userId The id of the user.
     * @param projectId The id of the project.
     * @param name The name of the counter.
     * @param value The value of the counter.
     */
    public void set(Long userId, Long projectId, String name, Integer value) {
        try {
            Counter counter = counterDAO.get(userId, projectId, name);
            counter.setValue(value);
            counterDAO.update(counter);
        } catch (NotFoundException e) {
            createCounter(userId, projectId, name, value);
        }
        LOGGER.debug("Set the counter '" + name + "' in the project <" + projectId + "> "
                     + "of user <" + userId + "> to '" + value + "'.");
    }

    /**
     * Increment the value of an existing counter.
     * Creates a new counter implicitly with the specified name if it does not exist yet.
     * The value of the new counter will be 1.
     *
     * @param userId The id of the user.
     * @param projectId The id of the project.
     * @param name The name of the counter to increment.
     */
    public void increment(Long userId, Long projectId, String name) {
        Counter counter;
        try {
            counter = counterDAO.get(userId, projectId, name);
            counter.setValue(counter.getValue() + 1);
            counterDAO.update(counter);
        } catch (NotFoundException e) {
            counter = createCounter(userId, projectId, name, 1);
        }
        LOGGER.debug("Incremented the counter '" + name + "' in the project <" + projectId + "> "
                     + "of user <" + userId + "> to '" + counter.getValue() + "'.");
    }

    /**
     * Get the value of an existing counter.
     *
     * @param userId The id of the user.
     * @param projectId The id of the project.
     * @param name The name of the counter.
     * @return The positive value of the counter.
     * @throws IllegalStateException
     */
    public Integer get(Long userId, Long projectId, String name) throws IllegalStateException {
        try {
            Counter counter;
            counter = counterDAO.get(userId, projectId, name);
            return counter.getValue();
        } catch (NotFoundException e) {
            throw new IllegalStateException("The counter '" + name + "' was not set and has no value!");
        }
    }

    /**
     * Create a new counter with a name and an initial, non negative value.
     * The name of the counter should not exist in the database.
     *
     * @param userId The id of the user.
     * @param projectId The id of the project.
     * @param name The name of the counter.
     * @param value The initial value of the counter.
     * @return The created counter.
     */
    Counter createCounter(Long userId, Long projectId, String name, Integer value) {
        Counter counter = new Counter();
        counter.setUser(new User(userId));
        counter.setProject(new Project(projectId));
        counter.setName(name);
        counter.setValue(value);
        counterDAO.create(counter);
        return counter;
    }
}
