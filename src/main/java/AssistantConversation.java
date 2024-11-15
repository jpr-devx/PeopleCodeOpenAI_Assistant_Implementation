import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Scanner;


public class AssistantConversation {



    String modelName;
    String assistantId;
    Thread thread;


    private String API_KEY = System.getenv("OPENAI_API_KEY");
    private String ASSISTANT_ID = System.getenv("ASSISTANT_ID");
//    private static final String API_KEY = "demo";

    public AssistantConversation(){
        this.modelName = "gpt-3.5-turbo";
    }

    public AssistantConversation(String assistantId){
        this.modelName = "gpt-3.5-turbo";
        this.assistantId = assistantId;
        this.thread = new Thread(this.createThread());
    }


    public void createAssistant(){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = """
                {
                    "model": "gpt-3.5-turbo",
                    "temperature": 0.5,
                    "name":"API_ACCESS_TEST_JAVA"
                }
            """;


            // String jsonInputString = "{ \"model\": \"" + this.modelName +
            //                                "\", \"temperature\": \"" + 0.5 +
            //                                "\", \"name\": \"" + assistantName +
            //                                "\" }";


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
                    System.out.println("Response from OpenAI: " + response.toString());


                    //create ObjectMapper instance
                    ObjectMapper objectMapper = new ObjectMapper();

                    //read JSON like DOM Parser
                    JsonNode rootNode = objectMapper.readTree(response.toString());
                    this.assistantId = rootNode.path("id").toString();

                    // Getting rid of pair of double quotes, there's probably a more elegant way to do this
                    this.assistantId = this.assistantId.substring(1,this.assistantId.length()-1);

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

    public String getAssistant(){
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


        return "";
    }

    /**
     * Creates conversation thread
     * @return Thread object
     */
    private Object createThread(){
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

    private void deleteThread(){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + this.thread.getThreadId());
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
        this.thread = null;
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
        if (this.thread == null){
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
                        this.thread = new Thread(response);

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
            URL url = new URL("https://api.openai.com/v1/threads/" + this.thread.getThreadId() + "/runs");
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


    // I don't think this is necessary, tagging it for now.
    @Deprecated
    public HashMap<String, String> listMessages(String threadId) {
        Object messagesResponse = this.getMessages(threadId);
        HashMap<String, String> resultMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();


        try {
            // Parse the JSON string
            JsonNode root = mapper.readTree(messagesResponse.toString());
            JsonNode dataArray = root.path("data");

            // Iterate over each element in the data array
            for (JsonNode dataItem : dataArray) {
                String id = dataItem.path("created_at").asText();
                JsonNode contentArray = dataItem.path("content");

                // Extract "value" from the first content item if available
                if (contentArray.isArray() && contentArray.size() > 0) {
                    JsonNode textNode = contentArray.get(0).path("text");
                    String contentValue = textNode.path("value").asText();
                    resultMap.put(id, contentValue);
                }
            }


            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Currently uses already-existent assistant, sets message thread and allows user to send queries to assistant to
     * receive replies back to advance conversation
     */
    public void basicConversation(){

        AssistantConversation basicConversation = new AssistantConversation(this.ASSISTANT_ID);

        String assistant = basicConversation.getAssistant();

        basicConversation.setThread();


        Scanner scan = new Scanner(System.in);
        String input;

        do {
            System.out.print("Query: ");
            input = scan.nextLine();

            Object userMessage = basicConversation.createUserMessage(basicConversation.thread.getThreadId(), input);

            Object assistantReply = basicConversation.assistantReply();
            Object conversation = basicConversation.getMessages(basicConversation.thread.getThreadId());
            System.out.println(basicConversation.getMostRecentMessage(conversation));


        } while (!input.equals("quit"));
    }

    public void defaultRAGConversation(){
        AssistantConversation RAGConversation = new AssistantConversation(this.ASSISTANT_ID);

        String assistant = RAGConversation.getAssistant();

        RAGConversation.setThread();


        Scanner scan = new Scanner(System.in);
        String input;

        do {
            System.out.print("Query: ");
            input = scan.nextLine();

            Object userMessage = RAGConversation.createUserMessage(RAGConversation.thread.getThreadId(), input);

            Object assistantReply = RAGConversation.assistantReply();
            Object conversation = RAGConversation.getMessages(RAGConversation.thread.getThreadId());
            //note: Only the assistant message is printed out here since the CLI retains the user's query
            System.out.println(RAGConversation.getMostRecentMessage(conversation));


        } while (!input.equals("quit"));
    }

    /**
     * Given the complete file path with filename.ext, file is uploaded to OpenAI. Not sure where yet
     * Individual files can be up to 512 MB, and the size of all files uploaded by one organization can be up to 100 GB.
     * There seems to be a workaround for this file size limitation through uploading by parts limited to 64 MB with
     * a total file size limit of 8 GB
     *
     * NOTE: this method does not lead to the actual uploading. It must be completed.
     * @param filePath complete filepath with extension
     * @return pendingUpload (Upload object with status "pending")
     */
    private Object addUpload(String filePath){

        try {
            File file = new File(filePath);
            String fileName = file.getName();
            long fileBytes = file.length();
            String filePurpose = "assistants";
            String fileMimeType = Files.probeContentType(file.toPath());


//            System.out.println("File Name: " + fileName);
//            System.out.println("File Size (in bytes): " + fileBytes);
//            System.out.println("File Purpose: " + filePurpose);
//            System.out.println("File MIME Type: " + fileMimeType);



            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/uploads");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"filename\": \"" + fileName +
                                       "\", \"purpose\": \"" + filePurpose +
                                       "\", \"bytes\": " + fileBytes +
                                       ", \"mime_type\": \"" + fileMimeType +
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
        return null;

    }


    private Object completeUpload(Object pendingUpload){
        // todo: take response from addUpload return and submit POST request with uploadID

        return null;


    }

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
        conversation.defaultRAGConversation();




    }
}