package eu.fbk.dh.tint.languagetool;


/**
 * Created by alessio on 02/12/16.
 */

public class Rule {
    String id, subId;
    String description;
    String issueType;
    Category category;

    public Rule(String id, String subId, String description, String issueType,
            Category category) {
        this.id = id;
        this.subId = subId;
        this.description = description;
        this.issueType = issueType;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
