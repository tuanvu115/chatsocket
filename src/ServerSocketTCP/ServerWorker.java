package ServerSocketTCP;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;


public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public void run() {
        handleClientSocket();

    }

    private void handleClientSocket() {
        try {
            outputStream = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line = "";

                while ((line = reader.readLine()) != null) {
                    String[] tokens = StringUtils.split(line);
                    if (tokens != null && tokens.length > 0) {
                        String cmd = tokens[0].trim();
                        if ("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)) {
                            handleLogoff();
                            break;
                        } else if ("login".equalsIgnoreCase(cmd)) {
                            handleLogin(outputStream, tokens);
                        } else if ("msg".equalsIgnoreCase(cmd)) {
                            String[] tokenMsg = StringUtils.split(line, null, 3);
                            handleMessage(tokenMsg);
                        } else if ("join".equalsIgnoreCase(cmd)) {
                            handleJoin(tokens);
                        } else if ("leave".equalsIgnoreCase(cmd)) {
                            handleLeave(tokens);
                        } else {
                            String msg = "Unknown " + cmd + "\n";
                            outputStream.write(msg.getBytes());
                        }
                    }
                }

            clientSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) {
        try {
            if (tokens.length == 3) {
                String login = tokens[1].trim();
                String password = tokens[2].trim();

                if ((login.equals("guest") && password.equals("123")) || (login.equals("vu") && password.equals("123"))) {
                    this.login = login;
                    System.out.println("User logged in succesfully " + this.login + "\n");
                    String msg = "ok login\n";
                    outputStream.write(msg.getBytes());
                    List<ServerWorker> workerList = server.getWorkerList();
                    StringBuilder msg2 = new StringBuilder();
                    workerList.forEach((worker) -> {
                        if (worker.getLogin() != null) {
                            if (!login.equalsIgnoreCase(worker.getLogin())) {
                                msg2.append("online " + worker.getLogin() + "\n");
                            }

                        }

                    });
                    String onlineMsg = "online " + login + "\n";
                    workerList.forEach((worker) -> {
                        if (!login.equalsIgnoreCase(worker.getLogin())) {
                            worker.send(onlineMsg);
                        }else{
                            worker.send(msg2.toString());
                        }
                    });
                } else {
                    String msg = "error login\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Login failed for " + login);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleLogoff() {
        server.removeWorker(this);
        String msg = "offline " + this.login + "\n";
        List<ServerWorker> workerList = server.getWorkerList();
        workerList.forEach((worker) -> {
            worker.send(msg);
        });

    }

    private void send(String msg) {
        try {
            if (msg != null) {
                outputStream.write(msg.getBytes());
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleMessage(String[] tokens) {
        String sendTo = tokens[1];
        String body = tokens[2];
        boolean isTopic = sendTo.charAt(0) == '#';
        List<ServerWorker> workerList = server.getWorkerList();
        workerList.forEach((worker) -> {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }

            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        });

    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

}

