package org.btet.util;

import org.btet.model.AuditLog;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
/**
 * This class is used to save and load audit logs to and from a file, by providing static methods.
 * */
public class AuditLoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuditLoggerUtil.class);
    private static final String FILE_PATH = "dat/audit_logs.dat";
    private AuditLoggerUtil() {}
    /**
     * This method is used to save an audit log to a file.
     * @param auditLog The audit log to be saved.
     * */
    public static void saveLog(AuditLog auditLog) {
        Set<AuditLog> auditLogs = loadLogs();
        auditLogs.add(auditLog);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            for(AuditLog log: auditLogs){
                oos.writeObject(log);
            }
        } catch (IOException e) {
            logger.info("Error writing audit log: {}", e.getMessage());
        }
    }

    /**
     * This method is used to load all audit logs from a file.
     * @return A set of all audit logs.
     * */
    public static Set<AuditLog> loadLogs() {
        Set<AuditLog> auditLogs = new HashSet<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return auditLogs;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            while (true) {
                AuditLog auditLog = (AuditLog) ois.readObject();
                if(auditLog == null) break;
                auditLogs.add(auditLog);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.info("Error loading audit log: {}", e.getMessage());
        }
        return auditLogs;
    }
}

