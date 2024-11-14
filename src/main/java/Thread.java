import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Thread {

    public String getThreadId() {
        return threadId;
    }

    public String getObject() {
        return object;
    }

    public long getCreated_at() {
        return created_at;
    }

    private String threadId;
    private String object;
    private long created_at;
    private Map<String, String> metadata;
    private Map<String, String> tool_resources;

    public Thread(Object response){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());

            // Retrieve a specific key's value
            this.threadId = rootNode.get("id").asText();
            this.object = rootNode.get("object").asText();
            this.created_at = rootNode.get("created_at").asLong();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
