import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private SecretKey key;

    public Client(Socket socket, String username) throws NoSuchAlgorithmException, Exception {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.key = generateAESKey();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to generate a random AES key
    private SecretKey generateAESKey() throws NoSuchAlgorithmException, Exception{
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom());
        SecretKey key = keyGenerator.generateKey();
        return key;  
    }

    // Function to encrypt data using AES
    private String encrypt(String messageToSend, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);       
        byte[] encryptedMessage = cipher.doFinal(messageToSend.getBytes());
        return Base64.getEncoder().encodeToString(encryptedMessage);

    }

    // Function to decrypt data using AES
    private String decrypt(String encryptedMessage, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedMessage);
    }

    public String sendMessage() throws Exception {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();
                String encryptedMessage = encrypt(messageToSend, key);
                String decryptedMessage = decrypt(encryptedMessage, key);
                bufferedWriter.write("\t\t\t\t\t\t\t\t\t" + username + " : " + decryptedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush(); 
            }
            
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return username;
        
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run(){
                String msgFromChat;
                while(socket.isConnected()){
                    try{
                        msgFromChat = bufferedReader.readLine();
                        System.out.println(msgFromChat);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }   

    public static void main(String[] args) throws Exception {

        System.out.print("Enter your username for the chat: "); 
        Scanner scanner = new Scanner(System.in) ;
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 8888); //Connect to the Server
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
        scanner.close();
        
    }

}



