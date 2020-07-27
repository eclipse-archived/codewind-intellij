package org.eclipse.codewind.intellij.ui.form;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPasswordField;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.ui.wizard.AbstractConnectionWizardStep;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RemoteConnectionForm {

    private JPanel contentPane;
    private JPanel rowPanel;
    private JLabel connectionNameLabel;
    private JTextField connectionNameField;
    private JLabel urlLabel;
    private JTextField urlField;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JTextPane descriptionTextPane;

    private AbstractConnectionWizardStep step;
    private Project project;
    private ConnectionManager connectionManager;
    private CodewindConnection connection;


    private boolean isChanged = false;
    private boolean isUpdating = false;


    public RemoteConnectionForm(AbstractConnectionWizardStep step, Project project, ConnectionManager connectionManager, CodewindConnection connection) {
        this.step = step;
        this.project = project;
        this.connectionManager = connectionManager;
        this.connection = connection;
    }

    private void createUIComponents() {

        descriptionTextPane = new JTextPane();
        connectionNameLabel = new JLabel();
        connectionNameLabel.setText(message("CodewindConnectionComposite_ConnNameLabel"));
        urlLabel = new JLabel();
        urlLabel.setText(message("CodewindConnectionComposite_UrlLabel"));
        usernameLabel = new JLabel();
        usernameLabel.setText(message("CodewindConnectionComposite_UserLabel"));
        passwordLabel = new JLabel();
        passwordLabel.setText(message("CodewindConnectionComposite_PasswordLabel"));

        connectionNameField = new JTextField();
        urlField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JBPasswordField();
        System.out.println("Background " + connectionNameLabel.getBackground());
        System.out.println("Background " + connectionNameLabel.getForeground());

        descriptionTextPane.setForeground(connectionNameLabel.getForeground());
        descriptionTextPane.setBackground(connectionNameLabel.getBackground());

        addListener(connectionNameField);
        addListener(urlField);
        addListener(usernameField);
        addListener(passwordField);
    }

    public void initialize() {
        if (this.connection != null) {
            this.isUpdating = true;
            setConnectionName(connection.getName());
            setUrl(connection.getBaseURI().toString());
            setUsername(connection.getUsername());
            this.isUpdating = false;
        }
    }

    private void addListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isUpdating) {
                    return;
                }
                step.fireStateChanging();
                isChanged = true;
            }
        });
    }

    public void setDescriptionTextArea(String text) {
        this.descriptionTextPane.setText(text);
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public String getConnectionName() {
        return connectionNameField.getText().trim();
    }

    public void setConnectionName(String connectionName) {
        connectionNameField.setText(connectionName);
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public void setUrl(String url) {
        urlField.setText(url);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public void setUsername(String username) {
        usernameField.setText(username);
    }

    public String getPassword() {
        return WidgetUtils.getPassword(passwordField);
    }

    public void setPassword(String password) {
        passwordField.setText(password);
    }

    public String validatePage() {
        String name = getConnectionName();
        String url = getUrl();
        String user = getUsername();

        if (name.length() == 0) {
            return message("CodewindConnectionComposite_NoConnNameError");
        } else {
            CodewindConnection existingConnection = ConnectionManager.getActiveConnectionByName(name);
            if (existingConnection != null && (connection == null || !existingConnection.getConid().equals(connection.getConid()))) {
                return message("CodewindConnectionComposite_ConnNameInUseError", name);
            }
        }

        // Check that the url is valid and not already used
        if (!url.isEmpty()) {
            try {
                URI uri = URI.create(url);
                new URI(url);
            } catch (URISyntaxException e) {
                return message("CodewindConnectionComposite_InvalidUrlError", url);
            }
            CodewindConnection existingConnection = ConnectionManager.getActiveConnection(url.endsWith("/") ? url : url + "/");
            if (existingConnection != null && (connection == null || !existingConnection.getConid().equals(connection.getConid()))) {
                return message("CodewindConnectionComposite_UrlInUseError", new Object[] {existingConnection.getName(), url});
            }
        }

        // Check that all of the connection fields are filled in
        if (url.isEmpty() || user.isEmpty()) {
            return message("CodewindConnectionComposite_MissingConnDetailsError");
        }
        if (passwordField.getPassword().length == 0) {
            if (connection != null) {
                return message("CodewindConnectionComposite_NoPasswordForUpdateError");
            }
            return message("CodewindConnectionComposite_MissingConnDetailsError");
        }

        return null;
    }

    public boolean isComplete() {
        return getConnectionName().length() > 0 && getUrl().length() > 0 && getUsername().length() > 0 && passwordField.getPassword().length > 0;
    }
}
