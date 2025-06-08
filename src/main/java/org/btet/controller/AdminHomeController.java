package org.btet.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.btet.model.AuditLog;
import org.btet.util.AuditLoggerUtil;
import org.btet.util.LoginUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
/**
 * Controller class for the Admin Home Page, which is the first page that the admin sees after logging in.
 * This page displays the audit logs of the system, which are the logs of all the changes made to the system.
 */
public class AdminHomeController {
    @FXML
    private TableView<AuditLog> auditLogTable;
    @FXML
    private TableColumn<AuditLog, String> changedRecordColumn;
    @FXML
    private TableColumn<AuditLog, String> oldValueColumn;
    @FXML
    private TableColumn<AuditLog, String> newValueColumn;
    @FXML
    private TableColumn<AuditLog, String> changedByColumn;
    @FXML
    private TableColumn<AuditLog, LocalDateTime> timestampColumn;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ComboBox<String> roleComboBox;

    private static Logger logger = Logger.getLogger(AdminHomeController.class.getName());
    private static final String ADMIN_STRING = "ADMIN";
    private static final String MANAGER_STRING = "MANAGER";
    private static final String EMPLOYEE_STRING = "EMPLOYEE";
    /**
     * Initializes the Admin Home Page, sets cell values for the audit log table, loads the audit logs,
     * sets up a Timeline thread to refresh the audit logs every 15 seconds, sets values for the roleComboBox,
     * and sets a listener for the roleComboBox to filter the audit logs based on the role selected.
     * This method is called automatically when the FXML file is loaded.
     */
    public void initialize() {
        welcomeLabel.setText(LoginUtil.getLoggedUserRecord().username() + "|HOME");
        changedRecordColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getChangedEntity()));
        oldValueColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOldValue()));
        newValueColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNewValue()));
        changedByColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getModifiedBy()));
        timestampColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimestamp()));

        timestampColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(item));
                }
            }
        });
        roleComboBox.getItems().addAll("ALL",ADMIN_STRING, MANAGER_STRING, EMPLOYEE_STRING);
        roleComboBox.getItems().forEach(role -> roleComboBox.setStyle("-fx-alignment: CENTER;"));
        roleComboBox.getSelectionModel().select(0);
        roleComboBox.valueProperty().addListener(e -> filterLogs());
        auditLogTable.getColumns().forEach(column -> column.setStyle("-fx-alignment: CENTER;"));
        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> loadLogs()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
        loadLogs();
    }
    /**
     * Loads the audit logs from the database and displays them in the audit log table.
     */
    private void loadLogs() {
        try{
        Set<AuditLog> auditLogs = AuditLoggerUtil.loadLogs();
        ObservableList<AuditLog> auditLogObservableList = FXCollections.observableArrayList(auditLogs.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp())).toList());
        auditLogTable.setItems(auditLogObservableList);
        }catch (RuntimeException e){
            logger.info("Error loading audit logs: " + e.getMessage());
        }
    }
    /**
     * Filters the audit logs based on the role selected in the roleComboBox.
     */
    public void filterLogs() {
        Set<AuditLog> auditLogs = AuditLoggerUtil.loadLogs();
        if(roleComboBox.getSelectionModel().getSelectedItem().equals(ADMIN_STRING)){
            auditLogs = auditLogs.stream().filter(auditLog -> auditLog.getModifiedBy().equals(ADMIN_STRING)).collect(Collectors.toSet());
        }
        else if(roleComboBox.getSelectionModel().getSelectedItem().equals(MANAGER_STRING)) {
            auditLogs = auditLogs.stream().filter(auditLog -> auditLog.getModifiedBy().equals(MANAGER_STRING)).collect(Collectors.toSet());
        }
        else if(roleComboBox.getSelectionModel().getSelectedItem().equals(EMPLOYEE_STRING)){
            auditLogs = auditLogs.stream().filter(auditLog -> auditLog.getModifiedBy().equals(EMPLOYEE_STRING)).collect(Collectors.toSet());
        }
        ObservableList<AuditLog> auditLogObservableList = FXCollections.observableArrayList(auditLogs);
        auditLogTable.setItems(auditLogObservableList);
    }
}

