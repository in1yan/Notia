package shared;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Utils {
    
    public static String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    public static String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
    
    public static Path toPath(String relativePath) {
        return Paths.get(relativePath);
    }
    
    public static void startConversationWith(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("User: ");
                String userMessage = scanner.nextLine();
                
                if ("exit".equalsIgnoreCase(userMessage)) {
                    break;
                }
                
                String response = assistant.chat(userMessage);
                System.out.println("Assistant: " + response);
            }
        }
    }
}
