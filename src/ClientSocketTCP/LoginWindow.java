package ClientSocketTCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    public LoginWindow(){
        setTitle("Login");
        this.client = new ChatClient("localhost",3000);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        getContentPane().add(p, BorderLayout.CENTER);
        pack();
        setVisible(true);

    }

    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();
        if(client.login(login,password)){
            UserListPane userListPane = new UserListPane(client);
            JFrame frame = new JFrame("User List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400,600);
            frame.getContentPane().add(userListPane,BorderLayout.CENTER);
            frame.setVisible(true);
            setVisible(false);
        }else{
            JOptionPane.showMessageDialog(this,"Invalid login/password.");
        }
    }

    public static void main(String[] args) {
        LoginWindow loginWindow = new LoginWindow();

    }
}
