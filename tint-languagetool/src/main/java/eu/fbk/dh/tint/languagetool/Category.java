package eu.fbk.dh.tint.languagetool;

/**
 * Created by alessio on 02/12/16.
 */

public class Category {
    String id, name;

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
