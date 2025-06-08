package org.btet.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.io.File;
/**
 * The ReceiptFileUtil class is a utility class that provides a static method for returning the path of a receipt file,
 * by opening a file chooser dialog.
 * */
public class ReceiptFileUtil {

    private ReceiptFileUtil() {}

    /**
     * Returns the receipt path in a String by opening a file chooser dialog, applying filters for image files,
     * and remembering the last opened directory.
     * @param uploadReceiptButton the button that triggers the file chooser dialog
     * @return the path of the selected receipt file
     * */
    public static String returnReceiptPath(Button uploadReceiptButton) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(uploadReceiptButton.getScene().getWindow());
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No file selected");
            alert.setContentText("Please select a file");
            alert.showAndWait();
            return null;
        }
    }
}
