package ClientSocketTCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MessagePane extends JPanel implements MessageListener {
    private final ChatClient client;
    private final String login;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private TextField inputField = new TextField();

    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;
        client.addMessageListener(this);
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList),BorderLayout.CENTER);
        add(inputField,BorderLayout.SOUTH);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                client.msg(login,text);
                listModel.addElement("You: "+text);
                inputField.setText("");
            }
        });

    }

    @Override
    public void onMessage(String formLogin, String msgBody) {
        if(login.equalsIgnoreCase(formLogin)){
            String line = formLogin + " :" + msgBody;
            listModel.addElement(line);
        }
    }
}
