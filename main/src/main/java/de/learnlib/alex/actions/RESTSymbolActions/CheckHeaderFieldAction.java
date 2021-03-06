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

package de.learnlib.alex.actions.RESTSymbolActions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.learnlib.alex.core.entities.ExecuteResult;
import de.learnlib.alex.core.learner.connectors.WebServiceConnector;
import de.learnlib.alex.utils.SearchHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

/**
 * RESTSymbolAction to check the HTTP Header fields of the last request.
 */
@Entity
@DiscriminatorValue("rest_checkHeaderField")
@JsonTypeName("rest_checkHeaderField")
public class CheckHeaderFieldAction extends RESTSymbolAction {

    private static final long serialVersionUID = -7234083244640666736L;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Marker LEARNER_MARKER = MarkerManager.getMarker("LEARNER");

    /** The key of the header field to check for the value. */
    @NotBlank
    private String key;

    /** The expected value which should be in the header field. */
    @NotBlank
    private String value;

    /** Field to determine if the search string is a regular expression. */
    private boolean regexp;

    /**
     * Get the key of the header field to inspect.
     *
     * @return The key of the header field.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the key of the header field to inspect.
     *
     * @param key
     *         The new key of the header field.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the expected value of the header field.
     *
     * @return The value to search for.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the expected value of the header field.
     * All variables and counters will be replaced with their values.
     *
     * @return The value to search for.
     */
    @JsonIgnore
    public String getValueWithVariableValues() {
        return insertVariableValues(value);
    }

    /**
     * Set the value which should be inside of the header field.
     *
     * @param value
     *         The new value to look for.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Should the value be treated as regular expression while searching for a text?
     *
     * @return true, if value should be a regular expression: false otherwise.
     */
    public boolean isRegexp() {
        return regexp;
    }

    /**
     * Set the flag if the value is a regular expression for the text search.
     *
     * @param regexp
     *         true if the value is a regular expression.
     */
    public void setRegexp(boolean regexp) {
        this.regexp = regexp;
    }

    @Override
    public ExecuteResult execute(WebServiceConnector connector) {
        List<Object> headerFieldValues = connector.getHeaders().get(key);
        if (headerFieldValues == null) {
            LOGGER.info(LEARNER_MARKER, "Could header {} against the value {}, because the header was not found "
                                            + "(regExp: {}, ignoreFailure: {}, negated: {}).",
                        key, value, regexp, ignoreFailure, negated);
            return getFailedOutput();
        }

        boolean result;
        if (regexp) {
            result = searchWithRegex(headerFieldValues);
        } else {
            result = search(headerFieldValues);
        }

        LOGGER.info(LEARNER_MARKER, "Checked header {} with the value {} against {} => {}"
                                        + "(regExp: {}, ignoreFailure: {}, negated: {}).",
                    key, headerFieldValues, value, result, regexp, ignoreFailure, negated);
        if (result) {
            return getSuccessOutput();
        } else {
            return getFailedOutput();
        }
    }

    private boolean search(List<Object> headerFieldValues) {
        return headerFieldValues.contains(getValueWithVariableValues());
    }

    private boolean searchWithRegex(List<Object> headerFieldValues) {
        for (Object headerFieldValue : headerFieldValues) {
            String headerFieldValueAsString = headerFieldValue.toString();
            if (SearchHelper.searchWithRegex(getValueWithVariableValues(), headerFieldValueAsString)) {
                return true;
            }
        }
        return false;
    }

}
