package pojos;

import java.io.Serializable;


public class ChannelPOJO implements Serializable {
    private String name;
    private String properties;

    public ChannelPOJO(String name, String properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ChannelPOJO{" +
                "name='" + name + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
