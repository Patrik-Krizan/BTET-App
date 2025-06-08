package org.btet.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.btet.database_repository.BudgetRepository;
import org.btet.database_repository.ManagerRepository;
import org.btet.exception.RepositoryAccessException;
import org.btet.model.Budget;
import org.btet.model.Manager;
import org.btet.util.LoginUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Logger;

/**
 * Controller class for the employee budget popup, which displays the budget information of the employee.
 */
public class EmployeeBudgetPopupController {
    @FXML
    private Label managerNameLabel;
    @FXML
    private Label allocatedAmountLabel;
    @FXML
    private Label spentAmountLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label percentUsedLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label reportLabel;

    private static BudgetRepository<Budget> budgetRepository = new BudgetRepository<>();
    private static ManagerRepository<Manager> managerRepository = new ManagerRepository<>();
    private static Logger logger = Logger.getLogger(EmployeeBudgetPopupController.class.getName());
    /**
     * Initializes the employee budget popup, setting the labels to the budget information of the employee,
     * as well as setting the progress bar to the percentage of the budget used.
     */
    public void initialize() {
        try {
            budgetRepository.updateSpentAmount(LoginUtil.getLoggedUserRecord().username());
            Budget budget = budgetRepository.findByUsername(LoginUtil.getLoggedUserRecord().username());
            if(budget == null) {
                titleLabel.setText("NO RECEIVED BUDGET");
                return;
            }
            managerNameLabel.setText(managerRepository.findByUsername(budget.getGivenBy()).getName());
            allocatedAmountLabel.setText(budget.getAllocatedAmount().toString()+"€");
            spentAmountLabel.setText(budget.getSpentAmount().toString()+"€");
            reportLabel.setText(budget.generateReport());
            reportLabel.setWrapText(true);
            reportLabel.setStyle("-fx-text-alignment: center;");

            BigDecimal spentAmount = budget.getSpentAmount();
            BigDecimal allocatedAmount = budget.getAllocatedAmount();
            BigDecimal percentUsed = spentAmount.divide(allocatedAmount, MathContext.DECIMAL32);

            if (percentUsed.compareTo(BigDecimal.valueOf(1.0)) > 0) {
                percentUsed = BigDecimal.valueOf(1.0);
            }

            progressBar.setProgress(percentUsed.doubleValue());
            progressBar.setStyle("-fx-accent: rgb(224, 123, 0);");
            int percentUsedInt = percentUsed.multiply(BigDecimal.valueOf(100)).intValue();
            percentUsedLabel.setText(percentUsedInt + "% used");
        } catch (RepositoryAccessException e) {
            logger.info("Error accessing the budget database: " + e.getMessage());
            managerNameLabel.setText("");
            allocatedAmountLabel.setText("");
            spentAmountLabel.setText("");
            titleLabel.setText("NO RECEIVED BUDGET");
        }
    }
}

