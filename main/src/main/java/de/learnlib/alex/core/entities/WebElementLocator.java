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

package de.learnlib.alex.core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.learnlib.alex.utils.CSSUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.openqa.selenium.By;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Embeddable class that helps working with selenium web elements.
 */
@Embeddable
@JsonPropertyOrder(alphabetic = true)
public class WebElementLocator implements Serializable {

    private static final long serialVersionUID = -6070241271039096113L;

    /** The type of selectors. */
    public enum Type {
        CSS, XPATH
    }

    /** The selector of the element[s]. */
    @NotBlank
    @Column(columnDefinition = "CLOB")
    private String selector;

    /** What kind of selector {@link #selector} is. */
    @NotNull
    @Column(name = "selectorType")
    private Type type;

    @JsonIgnore
    public By getBy() {
        if (type.equals(Type.CSS)) {
            return By.cssSelector(CSSUtils.escapeSelector(selector));
        } else {
            return By.xpath(selector);
        }
    }

    /** @return {@link #selector}. */
    public String getSelector() {
        return selector;
    }

    /** @param selector {@link #selector}. */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    /** @return {@link #type}. */
    public Type getType() {
        return type;
    }

    /** @param type {@link #selector}. */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return selector + "(" + type.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebElementLocator that = (WebElementLocator) o;

        if (selector != null ? !selector.equals(that.selector) : that.selector != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = selector != null ? selector.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
