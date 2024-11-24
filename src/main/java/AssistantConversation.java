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


    private String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String BASE_URL = "https://api.openai.com/v1";

    public AssistantConversation(){
        this.modelName = "gpt-4o-mini";
    }

    public AssistantConversation(String assistantId){
        this.modelName = "gpt-4o-mini";
        this.assistantId = assistantId;
        this.threadId = this.createThread();
    }


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

    public String getAssistant(){
        String result = "";
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + this.assistantId);
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
//                    System.out.println("Response from OpenAI: " + response.toString());


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
     * Creates conversation thread
     * @return ThreadId
     */
    private String createThread(){
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
//                    this.thread = new Thread(response);
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
        return "";
    }

    private void deleteThread(){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + this.threadId);
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
                    System.out.println("Response from OpenAI: " + response.toString());
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.threadId = null;
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
     * @return
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

    /**
     * Currently uses already-existent assistant, sets message thread and allows user to send queries to assistant to
     * receive replies back to advance conversation
     */
    public void basicConversation(String assistantId){

        AssistantConversation basicConversation = new AssistantConversation(assistantId);

        String assistant = basicConversation.getAssistant();

        basicConversation.setThread();


        Scanner scan = new Scanner(System.in);
        String input;

        do {
            System.out.print("Query: ");
            input = scan.nextLine();

            Object userMessage = basicConversation.createUserMessage(basicConversation.threadId, input);

            Object assistantReply = basicConversation.assistantReply();
            Object conversation = basicConversation.getMessages(basicConversation.threadId);
            System.out.println(basicConversation.getMostRecentMessage(conversation));


        } while (!input.equals("quit"));
    }

    public void defaultRAGConversation(){


//        String assistant = this.getAssistant();

        this.setThread();


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
     *
     * @param args
     */
    public static void main(String[] args){

        // todo:
        //  1) modify addUpload method to use the /files API endpoint. This seems specific to files within the file
        //  limitation. The 600 page OSHA reg that was used is only 9 MB. For the initial project, a baked in file-size
        //  limitation would be beneficial in terms of cost.
        //  2) retrieveUpload, some way of tracking what files we're dealing with for easy lookup
        //  3) Vector store creation, this is a parameter used when creating/modifying assistant with file_search as a tool


//        AssistantConversation test = new AssistantConversation();
//
//        Object testUploadResponse = test.addUpload("cs514_exception_handling_worksheet.pdf");

        AssistantConversation conversation = new AssistantConversation();

        // TOMS ADDED IN CODE TO CREATE A NEW ASSISTANT AND ATTACH THE UPLOADED FILE. AT THE MOMENT THE FILE IS HARDCODED
        // COMMENT THIS IN TO TEST IT. COMMENTED OUT NOW TO NOT SPAM UPLOADS
//        conversation.createAssistant();
//        String fileId = conversation.uploadFile("cs514_exception_handling_worksheet.pdf");
//        System.out.println("Uploaded file ID: " + fileId);
//        if (fileId != null) {
//            // Get file info
//            FileResponse fileInfo = conversation.getFileInfo(fileId);
//            if (fileInfo != null) {
//                System.out.println("File status: " + fileInfo.getStatus());
//                System.out.println("File size: " + fileInfo.getBytes() + " bytes");
//            }
////
//            // Attach file to assistant
//            boolean attached = conversation.attachFileToAssistant(fileId);
//            System.out.println("File attached to assistant: " + attached);
//        }


        conversation.createAssistant();
        System.out.println("File has been uploaded to assistant: " + conversation.uploadFileToAssistant("cs514_exception_handling_worksheet.pdf"));
        System.out.println("File has been uploaded to assistant: " + conversation.uploadFileToAssistant("Javascript and React Worksheet.pdf"));

        conversation.defaultRAGConversation();
    }
}