package me.maxouxax.supervisor.supervised;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

public class SupervisedDescriptionFile {

    private String name = null;
    private String main = null;
    private String version = null;
    private String description = null;
    private List<String> authors = null;
    private List<String> contributors = null;
    private String website = null;

    public SupervisedDescriptionFile() {
    }

    public static SupervisedDescriptionFile fromFile(final InputStream inputStream) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, SupervisedDescriptionFile.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
