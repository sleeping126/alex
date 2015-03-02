package de.learnlib.weblearner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public class SymbolGroup implements Serializable {

    /** to be serializable. */
    private static final long serialVersionUID = 4986838799404559274L;

    /** The ID of the SymbolGroup in the DB. */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonIgnore
    private Long groupId;

    @NotBlank
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "projectId")
    private Project project;

    @Column(nullable = false)
    private Long id;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.REMOVE })
    private Set<Symbol> symbols;

    public SymbolGroup() {
        this.groupId = 0L;
        this.id = 0L;
        this.symbols = new HashSet<>();
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JsonProperty("project")
    public long getProjectId() {
        if (project == null) {
            return 0L;
        }
        return project.getId();
    }

    @JsonProperty("project")
    public void setProjectId(long projectId) {
        this.project = new Project(projectId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(Set<Symbol> symbols) {
        this.symbols = symbols;
    }

    @JsonProperty("symbolAmount")
    public int getSymbolSize() {
        return this.symbols.size();
    }

    //todo(alex.s): remove me
    @JsonProperty("symbolAmount")
    public void setSymbolSize(int size) {
        // NOOOOOOOOOOOOOOOO....
    }

    public void addSymbol(Symbol symbol) {
        this.symbols.add(symbol);
        symbol.setGroup(this);
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals|MagicNumber - auto generated by Intellij IDEA

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymbolGroup group = (SymbolGroup) o;

        if (id != null ? !id.equals(group.id) : group.id != null) return false;
        if (project != null ? !project.equals(group.project) : group.project != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    // CHECKSTYLE.OFF: AvoidInlineConditionals|MagicNumber

    @Override
    public String toString() {
        return "SymbolGroup[" + groupId + "]: (" + getProjectId() + ", " + getId() + ")";
    }
}