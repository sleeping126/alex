package de.learnlib.alex.core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * A simple counter class.
 */
@Entity
public class Counter implements Serializable {

    /** to be serializable. */
    private static final long serialVersionUID = 5495935413098569457L;

    /** The ID of the counter in the DB. */
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long counterId;

    /** The project the counter belongs to. */
    @ManyToOne
    @NaturalId
    @JsonIgnore
    private Project project;

    /** The name of the counter. */
    @NaturalId
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9]*")
    private String name;

    /** The value of the counter. */
    @NotNull
    private Integer value;

    /**
     * Get the project in which the counter is used in..
     * @return The related project.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Set a new project for the counter.
     *
     * @param project
     *         The new related project.
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get the ID of {@link Project} the Symbol belongs to.
     *
     * @return The parent Project.
     * @requiredField
     */
    @JsonProperty("project")
    public Long getProjectId() {
        if (project == null) {
            return 0L;
        } else {
            return this.project.getId();
        }
    }

    /**
     * Set the ID of the {@link Project} the Symbol belongs to.
     *
     * @param projectId
     *            The new parent Project.
     */
    @JsonProperty("project")
    public void setProjectId(Long projectId) {
        this.project = new Project(projectId);
    }

    /**
     * Get the name of the counter.
     *
     * @return The counter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set a new name for the counter.
     *
     * @param name
     *         The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of the counter.
     *
     * @return The current value.
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Set a new value for the counter.
     *
     * @param value
     *         The new value.
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals|MagicNumber|NeedBraces - auto generated by IntelliJ IDEA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Counter)) return false;

        Counter counter = (Counter) o;

        if (project != null ? !project.equals(counter.project) : counter.project != null) return false;
        return !(name != null ? !name.equals(counter.name) : counter.name != null);
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
    // CHECKSTYLE.OFF: AvoidInlineConditionals|MagicNumber|NeedBraces
}
