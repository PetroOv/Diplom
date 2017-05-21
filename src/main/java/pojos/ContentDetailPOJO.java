package pojos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ContentDetailPOJO implements Serializable {
    private Map<String, String> records = new HashMap<>();

    public ContentDetailPOJO(Map<String, String> records) {
        this.records = records;
    }

    public Map<String, String> getRecords() {
        return records;
    }

    public void setRecords(Map<String, String> records) {
        this.records = records;
    }
}
