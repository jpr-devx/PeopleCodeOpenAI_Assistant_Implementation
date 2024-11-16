import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
    private String threadId; // Data member to allow for the integration of OpenAI's Assistant API

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
     * OpenAI Constructor allowing for OpenAI's Assistant API to be implemented. File search, code interpretation,
     * and other functions/tools can be added on OpenAI's UI. This assistant must be created beforehand and it's ID
     * passed in
     * @param apiKey OpenAI API key
     * @param modelName OpenAI model
     * @param assistantId OpenAI Assistant ID (this can be seen in Dashboard>Assistants)
     */
    public OpenAIConversation(String apiKey, String modelName, String assistantId){
        this.chatMemory=MessageWindowChatMemory.withMaxMessages(10);

        // Adds threadId for messages to take place in (Equivalent to AssistantConversation's setThread)
        if (this.threadId == null){
            try {
                URI uri = new URI("https://api.openai.com/v1/threads");
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();


                // Setting request headers
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header,
                // required for Assistants beta access

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

    /**
     * This askQuestion method takes in two additional parameters, an OpenAI API key and an ID corresponding to an
     * OpenAI assistant that has been created.
     *
     * @param apiKey OpenAI API key
     * @param context System instructions for OpenAI assistant
     * @param question User question to be asked to the assistant
     * @param assistantId ID corresponding to an already-created OpenAI assistant
     * @return Assistant's reply
     */
    public String askQuestion(String apiKey, String context, String question, String assistantId) {

        // Here is an outline for the algorithm:
        //          1) Modify the assistant with the context (instructions on their end)
        //          2) Send user question to the thread through a POST REQUEST. Then Add user message to langchain's
        //          ChatMemory object we have as a data member for the OpenAIConverstion object
        //          3) Run the thread with the newly modified assistant (assistant will reply with this run)
        //          4) Get the list of messages in the thread after the run has been completed
        //          5) Return the most recent message in that list, the assistant's message
        //          6) Add assistant's reply to langchain4j's ChatMemory object

        // 1) Modify the assistant with the context passed in
        try {
            // URL for the OpenAI Chat Completion endpoint
            URL url = new URL("https://api.openai.com/v1/assistants/" + assistantId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"instructions\": \"" +
                    context + "\" }";

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

        // 2) Create user message to add it to the thread
        try {
            // URL for the OpenAI Chat Completion endpoint

            URI uri = new URI("https://api.openai.com/v1/threads/" + threadId + "/messages");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            // Setting headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("OpenAI-Beta", "assistants=v2");  // Adding the beta HTTP header
            connection.setDoOutput(true);

            // JSON payload
            String jsonInputString = "{ \"role\": \"" + "user" +
                    "\", \"content\": \"" + question +
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
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2) (cont.) Now add user message to Langchain4J's chatmemory object
        this.chatMemory.add(new UserMessage(question));


        // 3) Run thread to get assistant's reply (One run, we get the most recent message, the assistant's message
        // from the list of Messages object that we can get from OpenAI. This list is sorted by the time the message
        // is created in descending order
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
            String jsonInputString = "{ \"assistant_id\": \"" +
                    assistantId + "\", \"stream\": " +
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

        Object messagesObject = null;

        // 4) Get the list of Message objects from OpenAI
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
                    messagesObject = response;
                }
            } else {
                System.out.println("Error: " + status);
                System.out.println("Msg: " + msg);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5) Get the most recent message back by parsing through the JSON that was stored in the messagesObject, a
        // response from out previous API call to get the list of Message objects from OpenAI

        if (messagesObject == null) return null; // Null check for thirdApi call response, returns null if true

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(messagesObject.toString());
            String assistantReply = rootNode.path("data").get(0).get("content").get(0).path("text").path("value").toString();

            // 6) Add assistant reply to langchain4j's chatmemory object as an AiMessage object
            this.chatMemory.add(new AiMessage(assistantReply));

            return assistantReply;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Just to catch anything if a message isn't produced for now
    }


    /**
     * This generateSampleQuestions takes in two additional parameters to access OpenAI's Assistant API, an OpenAI
     * API key and an ID corresponding to an OpenAI assistant that has been created.
     * @param apiKey OpenAI API key
     * @param context System instructions for OpenAI assistant
     * @param assistantId ID corresponding to an already-created OpenAI assistant
     * @param count Number of sample questions to be generated
     * @param maxWords Max length allowed for each sample question
     * @return List of sample questions
     */
    public List<String> generateSampleQuestions(String apiKey, String context, String assistantId, int count,
                                                int maxWords){

        String instructions = "For the context following, please provide a list of " + count + " questions with a maximum of " + maxWords + " words per question.";
        instructions = instructions + " Return the questions as a string with delimiter '%%' between each generated question";


        String[] questionArray =  askQuestion(apiKey, context, instructions, assistantId).split("%%");
        return List.of(questionArray);
    }

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

        String apiKey = System.getenv("OPENAI_API_KEY");
        String modelName = "gpt-4o-mini";
        String assistantId = System.getenv("ASSISTANT_ID");


        // Base methods demonstration
        // Example conversation
//        OpenAIConversation conversation = new OpenAIConversation(apiKey,modelName);
        // Generate sample questions
//        List<String> questions = conversation.generateSampleQuestions("Questions about films in the 1960s", 3, 10);
//        System.out.println("Sample questions: " + questions);

        // Ask a question
//        String response = conversation.askQuestion("You are a film expert", "What are the three best Quentin Tarintino movies?");
//        System.out.println("Response: " + response);

        // Ask another question to show continuation-- openAI knows 'he' is Tarantino from memory
//        response = conversation.askQuestion("You are a film expert", "How old is he");
//        System.out.println("Response: " + response);

        // Print conversation history
//        System.out.println("\nConversation History:");
//        System.out.println(conversation);



        // Assistant methods demonstration
//        // Example conversation
        OpenAIConversation assistantConversation = new OpenAIConversation(apiKey, modelName, assistantId);
//        // Generate sample questions
        List<String> questions = assistantConversation.generateSampleQuestions(apiKey,"You are an expert in Discrete " +
                        "math teaching a graduate student who has no clue how to go thorough this worksheet.",
                assistantId, 3, 50);
        System.out.println("Sample questions: " + questions);
//
//        // Ask a question
        String response = assistantConversation.askQuestion(apiKey, "You are an expert in Discrete Mathematics " +
                        "teaching a graduate student has no clue how to go through this worksheet.",
                "How should I start with question 1?",
                assistantId);
        System.out.println("Response: " + response);


        // Print conversation history
        System.out.println("\nConversation History:");
        System.out.println(assistantConversation);

    }

}
