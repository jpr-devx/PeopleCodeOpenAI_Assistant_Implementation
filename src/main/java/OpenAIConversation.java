import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * A class to represent a conversation with a chatbot.
 * It hides the details of LangChain4j and OpenAI, so client can
 * ask just call 'askQuestion' with context (instructions, etc.) and
 * the question, and get a string back. The conversation thus far is sent
 * to OpenAI on each question.
 *
 * Client can also get sample questions based on a given context, and can reset
 * conversation to start over.
 */
public class OpenAIConversation {
    private MessageWindowChatMemory chatMemory;
    private ChatLanguageModel chatModel;


    private String threadId; // For assistant conversations (Langchain4J vs OpenAI Assistant API integration gap, API
    // call workaround and integration into pre-existent PeopleCodeOpenAI code

    // Constructors
    public OpenAIConversation(){
        // demo is a key that LangChain4j provides to access OpenAI
        // for free. It has limitations, e.g., you have to use 3.5-turbo,
        // but is useful for testing.
        // Once you get going, you should get your own key from OpenAI.
        this("demo", "gpt-4o-mini");
    }
    public OpenAIConversation(String apiKey) {
        this(apiKey, "gpt-4o-mini");
    }
    public OpenAIConversation( String apiKey, String modelName) {
        this.chatModel = OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
        this.chatMemory=MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * OpenAI Constructor with OpenAI Assistant implementation. File search, code interpretation, and other
     * functions/tools can be added on OpenAI's UI. This assistant must be created beforehand and it's ID passed in
     * @param apiKey OpenAI API key
     * @param modelName OpenAI model
     * @param assistantId OpenAI Assistant ID (this can be seen in Dashboard>Assistants)
     */
    public OpenAIConversation(String apiKey, String modelName, String assistantId){
        //note: I don't know if anything else needs to be put in here
        this.chatMemory=MessageWindowChatMemory.withMaxMessages(10);

        // Adds threadId for messages to take place in (Equivalent to AssistantConversation's setThread)
        if (this.threadId == null){
            try {
                // URL for the OpenAI Chat Completion endpoint
                URL url = new URL("https://api.openai.com/v1/threads");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Setting headers
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
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
    }

    /** askQuestion allows user to ask a question with context (e.g., instructions
     * for how OpenAI should respond. It adds the context and question to the memory,
     * in the form langchain4j wants, then asks the question, then puts response into memory and returns text of response. in the form
     */

    public String askQuestion(String context, String question) {
        SystemMessage sysMessage = SystemMessage.from(context);
        chatMemory.add(sysMessage);
        UserMessage userMessage = UserMessage.from(question);
        chatMemory.add(userMessage);
        // Generate the response from the model
        Response response = chatModel.generate(chatMemory.messages());
        AiMessage aiMessage = (AiMessage) response.content();
        chatMemory.add(aiMessage);
        String responseText = aiMessage.text();

        return responseText;
    }


    // note: Later functionality can be added to just modify the assistant with the new instructions regardless of
    //  whether or not what OpenAI has a different String on their end.
    /**
     * Right now context isn't used as it can already be modified online.
     *
     * @param apiKey
     * @param context
     * @param question
     * @param assistantId
     * @return
     */
    public String askQuestion(String apiKey, String context, String question, String assistantId) {
        //note: How can I organize the code the best here? Originally there are helper methods I use to retrieve the
        // latest message and then return it (once after a user submits a question and again after the thread is ran
        // to retrieve the assistant's response.

        //note: There would be three API calls in here and the processing that comes with them. The amount of
        // redundant code in this method would be insane.
        //clarify with Dr. Wolber on intent. Preserve existent class methods or would it be encouraged to create
        // helper methods to compartmentalize the code that would be used in assistant-centered methods that overload
        // the PeopleCodeOpenAI methods?
        Object userMessage = this.createUserMessage(apiKey, this.threadId, question);

        Object assistantReply = this.assistantReply(apiKey, assistantId);
        Object conversation = this.getMessages(this.threadId, apiKey);
        //note: Only the assistant message is printed out here since the CLI retains the user's query
        return this.getMostRecentMessage(conversation);
    }

    //HELPER METHODS FOR ASSISTANT METHODS
    /**
     * Returns a list of Message objects sorted by created_at in descending order by default. For use in
     * getMostRecentMessage
     * @param threadId ID corresponding to thread containing the messages
     * @return list of Message objects
     */
    private Object getMessages(String threadId, String apiKey){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
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
     * Takes object returned from getMessages and returns most recent message
     * @param messages List of Message objects
     * @return most recent message as a String
     */
    private String getMostRecentMessage(Object messages){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(messages.toString());
            return rootNode.path("data").get(0).get("content").get(0).path("text").path("value").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns assistant object for other methods
     * @param apiKey
     * @param assistantId
     * @return
     */
    private String getAssistant(String apiKey, String assistantId){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + assistantId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
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
     *
     * @param apiKey
     * @param threadId
     * @param message
     * @return
     */
    private Object createUserMessage(String apiKey, String threadId, String message){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
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

    private Object assistantReply(String apiKey, String assistantId){
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/threads/" + this.threadId + "/runs");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
//            String jsonInputString = "{ \"assistant_id\": \"" + this.assistantId + "\"}";

            // JSON payload
            String jsonInputString = "{ \"assistant_id\": \"" +
                    assistantId + "\", \"stream\": " +
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







    // END OF HELPER METHODS


    /**
     * generateSampleQuestions generate sample questions with a given context. You can specify the number of questions
     * and the max words that should be generated for each question. This method is
     * often used to provide user with sample questions to trigger the dialogue.
     */
    public List<String> generateSampleQuestions(String context, int count, int maxWords) {
        List<String> questions = new ArrayList<>();
        String instructions = "For the context following, please provide a list of " + count + " questions with a maximum of " + maxWords + " words per question.";
        instructions = instructions + " Return the questions as a string with delimiter '%%' between each generated question";
        SystemMessage sysMessage = SystemMessage.from(instructions );
        UserMessage userMessage = UserMessage.from(context);
        List<ChatMessage> prompt = new ArrayList<>();
        prompt.add(sysMessage);
        prompt.add(userMessage);
        Response response = chatModel.generate(prompt);
        AiMessage aiMessage = (AiMessage) response.content();
        String responseText = aiMessage.text();
        String[] questionArray = responseText.split("%%");
        return List.of(questionArray);
    }
    public void resetConversation() {
        chatMemory.clear();
    }


    /**
     *
     * @return the messages thus far
     */
    public String toString() {
        return chatMemory.messages().toString();
    }
    /**
     *
     * main is a sample that asks two questions, the second of which
     * can only be answered if model remembers the first.
     */

    public static void main(String[] args) {
//        // Example conversation
//        OpenAIConversation conversation = new OpenAIConversation();
//        // Generate sample questions
//        List<String> questions = conversation.generateSampleQuestions("Questions about films in the 1960s", 3, 10);
//        System.out.println("Sample questions: " + questions);
//
//        // Ask a question
//        String response = conversation.askQuestion("You are a film expert", "What are the three best Quentin Tarintino movies?");
//        System.out.println("Response: " + response);
//
//        // Ask another question to show continuation-- openAI knows 'he' is Tarantino from memory
//        response = conversation.askQuestion("You are a film expert", "How old is he");
//        System.out.println("Response: " + response);
//
//        // Print conversation history
//        System.out.println("\nConversation History:");
//        System.out.println(conversation);
        ;
        OpenAIConversation conversation = new OpenAIConversation(System.getenv("OPENAI_API_KEY"), "gpt-4o-mini",System.getenv("ASSISTANT_ID"));

//        String response = conversation.askQuestion(System.getenv("OPENAI_API_KEY"),)


    }

}
