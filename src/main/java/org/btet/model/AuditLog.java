package org.btet.model;
import java.io.*;
import java.time.LocalDateTime;

/**
 * This class is used to store the audit logs of the application.
 * It stores the entity that was changed, the old value, the new value, the user who made the change and the timestamp of the change.
 * Each AuditLog has a changedEntity, oldValue, newValue, modifiedBy and timestamp, representing the entity that was changed, the old value of the entity,
 * the new value of the entity, the user who made the change and the timestamp of the change.
 */
public class AuditLog implements Serializable {
    private String changedEntity;
    private String oldValue;
    private String newValue;
    private String modifiedBy;
    private LocalDateTime timestamp;
    /**
     * Constructor for the AuditLog class.
     * @param changedEntity The entity that was changed.
     * @param oldValue The old value of the entity.
     * @param modifiedBy The users role who made the change, keeping the user's role instead of the
     * user's name for security reasons.
     */
    public AuditLog(String changedEntity, String oldValue,String modifiedBy) {
        this.changedEntity = changedEntity;
        this.oldValue = oldValue;
        this.modifiedBy = modifiedBy;
        this.timestamp = LocalDateTime.now();
    }
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedEntity() { return changedEntity; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getModifiedBy() { return modifiedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setChangedEntity(String changedEntity) {
        this.changedEntity = changedEntity;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}