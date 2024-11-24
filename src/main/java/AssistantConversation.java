import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;


public class AssistantConversation {



    String modelName;
    String assistantId;
    String threadId;
    ArrayList<String> chatMessages;


    private String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String BASE_URL = "https://api.openai.com/v1";

    public AssistantConversation(){
        this.modelName = "gpt-4o-mini";
        this.assistantId = null;
        this.createThread();
        this.chatMessages = new ArrayList<>();
    }

    public AssistantConversation(String assistantId){
        this.modelName = "gpt-4o-mini";
        this.assistantId = assistantId;
        this.createThread();
        this.chatMessages = new ArrayList<>();
    }

    /** Creates a new OpenAI assistant (with file_search enabled) object and sets this.assistantId to the ID contained
     * in the Assistant object
     */
    public void createAssistant(){
        try {
            URL url = new URL(BASE_URL + "/assistants");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", this.modelName);
            payload.put("name", "API_ACCESS_TEST_JAVA");
            payload.put("instructions", "You are a helpful assistant.");

            // Add tools configuration
            List<Map<String, String>> tools = new ArrayList<>();
            Map<String, String> retrieval = new HashMap<>();
            retrieval.put("type", "file_search");
            tools.add(retrieval);

            payload.put("tools", tools);

            ObjectMapper mapper = new ObjectMapper();
            try (OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, payload);
            }

            int status = connection.getResponseCode();
            String response = readResponse(connection);

            System.out.println("Assistant creation status: " + status);
            System.out.println("Assistant creation response: " + response);

            if (status == 200 || status == 201) {
                JsonNode rootNode = mapper.readTree(response);
                this.assistantId = rootNode.get("id").asText();
                System.out.println("Assistant created successfully with ID: " + this.assistantId);
            } else {
                System.err.println("Error creating assistant. Status: " + status);
                System.err.println("Response: " + response);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to create assistant: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Sets this.assistantId to assistant created with name assistantName
     * @param assistantName desired name for assistant
     */
    public void createAssistant(String assistantName){
        try {
            URL url = new URL(BASE_URL + "/assistants");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", this.modelName);
            payload.put("name", assistantName);
            payload.put("instructions", "You are a helpful assistant.");

            // Add tools configuration
            List<Map<String, String>> tools = new ArrayList<>();
            Map<String, String> retrieval = new HashMap<>();
            retrieval.put("type", "file_search");
            tools.add(retrieval);

            payload.put("tools", tools);

            ObjectMapper mapper = new ObjectMapper();
            try (OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, payload);
            }

            int status = connection.getResponseCode();
            String response = readResponse(connection);

            System.out.println("Assistant creation status: " + status);
            System.out.println("Assistant creation response: " + response);

            if (status == 200 || status == 201) {
                JsonNode rootNode = mapper.readTree(response);
                this.assistantId = rootNode.get("id").asText();
                System.out.println("Assistant created successfully with ID: " + this.assistantId);
            } else {
                System.err.println("Error creating assistant. Status: " + status);
                System.err.println("Response: " + response);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to create assistant: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns OpenAI Assistant object tied with ID this.assistantId
     * @return OpenAI Assistant Object
     */
    public String getAssistant(String assistantId){
        String result = "";
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + assistantId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();

            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    result = response.toString();
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * Modifies assistant's temperature
     * @param temp 0 to 2
     */
    private void changeTemperature(double temp){
        assert temp >= 0 && temp <= 2 : "Temperature out of range 0.0 - 2.0";

        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + assistantId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("temperature", temp);

            ObjectMapper mapper = new ObjectMapper();
            try (OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, payload);
            }

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();

            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
//                    System.out.println("Temperature has been changed to " + temp + "\n" + response);
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Returns description of assistant
     */
    private String generateDescription(){
        // Create temp thread
        String tempThreadId = "";
        String description = "";
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    tempThreadId = rootNode.get("id").asText();

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send userMessage asking to generate a description including "A domain expert on <description of documents
        // loaded for that assistant>"
        String message = "Generate a description in quotes starting with 'A domain expert on <description>' where " +
                "<description> is a summary of the documents that you have loaded and the applications that stem from" +
                " the information contained in those documents";
        String userMessageResponse = this.createUserMessage(tempThreadId, message).toString();

        // Run thread and obtain assistant's generated description
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + tempThreadId + "/runs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"assistant_id\": \"" +
                    this.assistantId + "\", \"stream\": " +
                    true + " }";

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        description = this.getMostRecentMessage(this.getMessages(tempThreadId)).toString();

        // Delete temp thread
        this.deleteThread(tempThreadId);

        return description;
    }


    /**
     * Called after user message is created. Generates thread title for sidebar
     * @return thread title
     */
    private String generateThreadTitle(String userMessage){
        // Create temp thread
        String tempThreadId = "";
        String threadTitle = "";
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    tempThreadId = rootNode.get("id").asText();

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send userMessage asking to generate a description including "A domain expert on <description of documents
        // loaded for that assistant>"
        String message =
                "Generate a short phrase describing the user's question. You are required to be concise for the " +
                        "purpose of this being a label for a conversation. If you have files loaded, use them" +
                        " as context for your title generation. Be specific in your title when the user is " +
                        "referring to your files. For instance, if the user is asking about a particular line " +
                        "item, section, problem, or point in any of your files, use the content located at the " +
                        "section that the user is asking about in your title generation. Leave out any Title label in your reply. User's question: " + userMessage;
        String userMessageResponse = this.createUserMessage(tempThreadId, message).toString();

        // Run thread and obtain assistant's generated description
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + tempThreadId + "/runs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"assistant_id\": \"" +
                    this.assistantId + "\", \"stream\": " +
                    true + " }";

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        threadTitle = this.getMostRecentMessage(this.getMessages(tempThreadId)).toString();

        // Delete temp thread
        this.deleteThread(tempThreadId);

        return threadTitle;
    }


    /**
     * Returns a List of the most N-recent Assistant Objects
     * @return list of Assistant objects
     */
    private Object getAssistants(int N){
        assert N > 0 && N <= 100 : "Number is out of range: 1 - 100";
        try {
            URL url = new URL(BASE_URL + "/assistants?order=desc&limit="+N);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");


            int status = connection.getResponseCode();
            String response = readResponse(connection);


            if (status == 200 || status == 201) {
                System.out.println("Assistants retrieved successfully: " + response);
                return response;
            } else {
                System.err.println("Error creating assistant. Status: " + status);
                System.err.println("Response: " + response);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to create assistant: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Creates conversation thread and sets this.threadId to the id tied to the OpenAI Thread object
     * @return id tied to OpenAI Thread object, empty string if POST request is unsuccessful
     */
    private void createThread(){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    this.threadId = rootNode.get("id").asText();

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteThread(String threadId){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Thread deleted: " + response.toString());
                }
            } else {
                System.out.println("Error in deleting thread: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.threadId.equals(threadId)) this.threadId = null;

    }

    private Object getThread(String threadId){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response;
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setThread(){
        if (this.threadId == null){
            try {
                // URL for the OpenAI Chat Completion endpoint
                URL url = new URL("https://api.openai.com/v1/threads");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Setting headers
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header

                // Handling the response
                int status = connection.getResponseCode();
                String msg = connection.getResponseMessage();
                if (status == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
//                    System.out.println("Response from OpenAI: " + response.toString());
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response.toString());
                        this.threadId = rootNode.get("id").asText();
                    }
                } else {
                    System.out.println("Error: " + status);
                    System.out.println("Msg: " + msg);
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Object createUserMessage(String threadId, String message){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"role\": \"" + "user" +
                    "\", \"content\": \"" + message +
                    "\" }";

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                this.chatMessages.add(message);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
//                    System.out.println("Response from OpenAI: " + response.toString());
                    return response;

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Object assistantReply(){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + this.threadId + "/runs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
//            String jsonInputString = "{ \"assistant_id\": \"" + this.assistantId + "\"}";

            // JSON payload
            String jsonInputString = "{ \"assistant_id\": \"" +
                    this.assistantId + "\", \"stream\": " +
                    true + " }";

//            jsonInputString = "{ \"assistant_id\": \"" +
//                    this.assistantId + "\"}";



            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
//                    System.out.println("Response from OpenAI: " + response.toString());
                    String assistantMessage = this.getMostRecentMessage(this.getMessages(this.threadId)).toString();
                    this.chatMessages.add(assistantMessage);
                    return assistantMessage;

                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns a list of Message objects sorted by created_at in descending order by default
     * @param threadId ID corresponding to thread containing the messages
     * @return list of Message objects
     */
    private Object getMessages(String threadId){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header


            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response;
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Uses getMessages method to return list of OpenAI Message objects and returns most recent one. Since messages are presented by timestamp in descending order, this is the first message
     * @param messages List of OpenAI message objects
     * @return most recent OpenAI message
     */
    private Object getMostRecentMessage(Object messages){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(messages.toString());
            return rootNode.path("data").get(0).get("content").get(0).path("text").path("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * Uploads local file to OpenAI and returns the fileID of the OpenAI File Object
     * @param filePath location of file to be uploaded
     * @return fileID of the OpenAI File Object
     */
    public String uploadFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + filePath);
            }

            URL url = new URL(BASE_URL + "/files");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"purpose\"\r\n\r\n");
                writer.append("assistants").append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n");
                writer.flush();

                Files.copy(file.toPath(), os);
                os.flush();

                writer.append("\r\n");
                writer.append("--").append(boundary).append("--").append("\r\n");
            }

            int status = connection.getResponseCode();
            String response = readResponse(connection);

            if (status == 200 || status == 201) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response);
                String fileId = rootNode.get("id").asText();
                System.out.println("File uploaded successfully. File ID: " + fileId);
                return fileId;
            } else {
                System.err.println("Error uploading file. Status: " + status);
                System.err.println("Response: " + response);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Upload failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convenient helper method to be used to read responses from API requests
     * @param connection HttpURLConnection object
     * @return response
     * @throws IOException
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getResponseCode() >= 400 ?
                connection.getErrorStream() : connection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Retrieves file information after upload
     * @param fileId The ID of the uploaded file
     * @return FileResponse object containing file details, or null if failed
     */
    public FileResponse getFileInfo(String fileId) {
        try {
            URL url = new URL(BASE_URL + "/files/" + fileId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int status = connection.getResponseCode();
            String response = readResponse(connection);

            if (status == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response, FileResponse.class);
            } else {
                System.err.println("Error getting file info. Status: " + status);
                System.err.println("Response: " + response);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to get file info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attaches an uploaded file to the assistant
     * @param fileId The ID of the uploaded file
     * @return boolean indicating success
     */
    public boolean attachFileToAssistant(String fileId) {
        if (assistantId == null) {
            System.err.println("No assistant ID available. Create an assistant first.");
            return false;
        }

        try {
            URL url = new URL(BASE_URL + "/assistants/" + assistantId + "/files");  // Changed endpoint
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
            connection.setDoOutput(true);

            Map<String, String> payload = new HashMap<>();
            payload.put("file_id", fileId);  // Changed to single file_id

            ObjectMapper mapper = new ObjectMapper();
            try (OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, payload);
            }

            int status = connection.getResponseCode();
            String response = readResponse(connection);

            if (status == 200 || status == 201) {
                System.out.println("File attached to assistant successfully.");
                return true;
            } else {
                System.err.println("Error attaching file. Status: " + status);
                System.err.println("Response: " + response);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to attach file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cradle-to-grave method combining uploadFile, getFileInfo and attachFileToAssistant
     * @return true if file was uploaded to assistant object, false otherwise
     */
    public boolean uploadFileToAssistant(String filePath) {
        String fileId = uploadFile(filePath);
        if (fileId != null) {
            // Get file info
            FileResponse fileInfo = this.getFileInfo(fileId);
            if (fileInfo != null) {
                System.out.println("File status: " + fileInfo.getStatus());
                System.out.println("File size: " + fileInfo.getBytes() + " bytes");
            }
//
            // Attach file to assistant
            boolean attached = this.attachFileToAssistant(fileId);
            System.out.println("File attached to assistant: " + attached);
            return attached;
        }
        System.out.println("No file was uploaded, fileId was null");
        return false;
    }


    // Response class for file information
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileResponse {
        public String id;
        public String object;
        public long bytes;
        public long created_at;
        public String filename;
        public String purpose;
        public String status;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        public long getBytes() { return bytes; }
        public void setBytes(long bytes) { this.bytes = bytes; }
        public long getCreatedAt() { return created_at; }
        public void setCreatedAt(long created_at) { this.created_at = created_at; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }


    /*
     * Demonstration methods
     */


    public void defaultRAGConversation(){

        Scanner scan = new Scanner(System.in);
        String input;

        do {
            System.out.print("Query: ");
            input = scan.nextLine();

            Object userMessage = this.createUserMessage(this.threadId, input);

            Object assistantReply = this.assistantReply();
            Object conversation = this.getMessages(this.threadId);
            //note: Only the assistant message is printed out here since the CLI retains the user's query
            System.out.println(this.getMostRecentMessage(conversation));


        } while (!input.equals("quit"));
    }


    /**
     * Adds description to assistant object
     * @param description
     */
    private void addDescriptionToAssistant(String description){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + assistantId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("description", description);

            ObjectMapper mapper = new ObjectMapper();
            try (OutputStream os = connection.getOutputStream()) {
                mapper.writeValue(os, payload);
            }

            // Handling the response
            int status = connection.getResponseCode();
            String msg = connection.getResponseMessage();

            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
//                    System.out.println("Temperature has been changed to " + temp + "\n" + response);
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Gives demonstrations on the following main functions:
     * 1) Assistant creation
     * 2) Local file upload //note: need to figure out how bulk upload will work (e.g. filepath iteration and
     *                         .uploadFileToAssistant method calls
     * 3) Assistant domain expertise description generation
     * 4) Assistant temperature tuning
     * 5) Conversation history
     * 6) Thread title generation
     * @param args
     */
    public static void main(String[] args){

        AssistantConversation conversation = new AssistantConversation();

        // Method calls demonstrating file upload and addition to assistant
        // 1) Assistant creation
//        conversation.createAssistant("TEMP_TEST_DESCRIPTION");

        // 2) Local file upload and adding to assistant
//        System.out.println("File has been uploaded to assistant: " + conversation.uploadFileToAssistant("cs514_exception_handling_worksheet.pdf"));
//        System.out.println("File has been uploaded to assistant: " + conversation.uploadFileToAssistant("Javascript and React Worksheet.pdf"));
//
        // 3) Method calls demonstrating description generation on assistant's domain expertise then adding it to the
        // Assistant object on OpenAIs end
//        String description = conversation.generateDescription();
//        System.out.println("\n\nDescription generated: " + description);
//        conversation.addDescriptionToAssistant(description);
//        System.out.println("\n\nAfter add:");
//        System.out.println(conversation.getAssistant(conversation.assistantId));

        // 4) Assistant temperature tuning
//        System.out.println("Assistant object before temperature add:\n" + conversation.getAssistant(conversation.assistantId));
//        conversation.changeTemperature(0.2);
//        System.out.println("Assistant object after temperature add:\n" + conversation.getAssistant(conversation.assistantId));

        // 5) Method calls demonstrating back and forth with conversation history ArrayList addition
//        conversation.createUserMessage(conversation.threadId, "What is the answer to problem 3?");
//        conversation.assistantReply();
//        conversation.createUserMessage(conversation.threadId, "Can you explain your solution in simpler terms?");
//        conversation.assistantReply();
//
        // 6) Conversation history
//        System.out.println("\n\nConversation history:");
//        for (String message : conversation.chatMessages) {
//            System.out.println("Message:\n" + message + "\n");
//        }

        // 7) Method calls demonstrating thread title generation following a userMessage
//        String threadTitle = conversation.generateThreadTitle(conversation.chatMessages.getFirst());
//        System.out.println("Thread title: " + threadTitle);

    }
}