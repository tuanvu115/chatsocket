package ClientSocketTCP;

import com.sun.deploy.net.proxy.ProxyUnavailableException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    private List<UserStatusListener> userStatusListenerList = new ArrayList<>();
    private List<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public boolean connect(){
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public boolean login(String login, String password) {
        try {
            String cmd = "login " + login + " " + password + "\n";
            serverOut.write(cmd.getBytes());
            String response = bufferedIn.readLine();
            System.out.println("Response line :"+response);
            if ("ok login".equalsIgnoreCase(response.trim())) {
                startMessageReader();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line = "";
            while((line=bufferedIn.readLine())!= null){
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0].trim();
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    }
                    else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }else if("msg".equalsIgnoreCase(cmd)){
                        String[] tokensMsg = StringUtils.split(line,null,3);
                        handleMessage(tokensMsg);
                    }
                }

            }
        }catch (IOException e){
            System.out.println(e.getMessage());
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];
        messageListeners.forEach( (listener -> {
            listener.onMessage(login,msgBody);
        }));
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListenerList){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListenerList){
            listener.online(login);
        }
    }

    public void logoff() {
        try {
            String cmd = "logoff\n";
            serverOut.write(cmd.getBytes());
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }

    public void msg(String sendTo, String msgBody) {
        try {
            String cmd = "msg " + sendTo + " " + msgBody + "\n";
            serverOut.write(cmd.getBytes());
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListenerList.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListenerList.remove(listener);
    }
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }


    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost",3000);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE :"+login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE :"+login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String formLogin, String msgBody) {
                System.out.println("You got a message from "+formLogin+"===>"+msgBody);
            }
        });

        if(!client.connect()){
            System.err.println("Connect failed.");
        }else{
            System.out.println("Connect successful");
            if(client.login("vu","123")){
                System.out.println("Login successful");
                client.msg("guest","Hello World!");
            }else{
                System.err.println("Login failed");
            }
//            client.logoff();

        }
    }




}
