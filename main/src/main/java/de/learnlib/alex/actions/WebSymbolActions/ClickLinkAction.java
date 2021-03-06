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

package de.learnlib.alex.actions.WebSymbolActions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.learnlib.alex.core.entities.ExecuteResult;
import de.learnlib.alex.core.learner.connectors.WebSiteConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.hibernate.validator.constraints.NotBlank;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Action to click on a link by its visible link.
 */
@Entity
@DiscriminatorValue("web_clickLinkByText")
@JsonTypeName("web_clickLinkByText")
public class ClickLinkAction extends WebSymbolAction {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Marker LEARNER_MARKER = MarkerManager.getMarker("LEARNER");

    /** The value the site is checked for. */
    @NotBlank
    private String value;

    /**
     * Get the value to check.
     *
     * @return The value to check.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the value to check.
     * All variables and counters will be replaced with their values.
     *
     * @return The value to check.
     */
    @JsonIgnore
    public String getValueWithVariableValues() {
        return insertVariableValues(value);
    }

    /**
     * Set the value to check for.
     *
     * @param value
     *            The new value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ExecuteResult execute(WebSiteConnector connector) {
        try {
            WebElement element = connector.getLinkByText(getValueWithVariableValues());
            element.click();

            LOGGER.info(LEARNER_MARKER, "Clicked on the link '{}' (ignoreFailure: {}, negated: {}).",
                        value, ignoreFailure, negated);
            return getSuccessOutput();
        } catch (NoSuchElementException e) {
            LOGGER.info(LEARNER_MARKER, "Could not click on the link '{}' (ignoreFailure: {}, negated: {}).",
                        value, ignoreFailure, negated, e);
            return getFailedOutput();
        }
    }
}
