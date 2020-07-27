package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectDisconnectTask extends Task.Backgroundable  {

    private RemoteConnection connection;

    public ConnectDisconnectTask(RemoteConnection connection, @Nullable Project project, String taskName) {
        super(project, taskName);
        this.connection = connection;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        boolean isConnected = this.connection.isConnected();
        try {
            if (isConnected) {
                this.connection.disconnect();
            } else {
                this.connection.connect();
            }
            CoreUtil.updateConnection(connection);
        } catch (Exception e) {
            if (isConnected) {
                Logger.logWarning("An error occurred when disconnecting from the connection " + this.connection.getName(), e);
            } else {
                Logger.logWarning("An error occurred when connecting to the connection " + this.connection.getName(), e);
            }
        }
    }
}
