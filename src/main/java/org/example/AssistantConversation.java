package org.example;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;





public class AssistantConversation {



    String modelName;


    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
//    private static final String API_KEY = "demo";

    public AssistantConversation(){
        this.modelName = "gpt-3.5-turbo";
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


    public String findAssistant(){



        return "";
    }

    public void basic(){

        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [{"role": "user", "content": "Hello, how are you?"}],
                    "max_tokens": 50
                }
            """;

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Handling the response
            int status = connection.getResponseCode();
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
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param args
     */


    public static void main(String[] args){

        AssistantConversation basicConverstaion = new AssistantConversation();
        basicConverstaion.createAssistant();

    }
}