package org.btet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 * The Budget class represents a budget that is allocated to a specific employee, by a manager.
 * It has a Builder class that is used to create a Budget object, implementing the Builder pattern.
 * The Budget class implements the Reportable interface, which contains a method for generating a report.
 * */
public class Budget implements Reportable{
    /**
     * The Builder class is used to create a Budget object. It has a method for each field of the Budget class.
     * The build method creates a new Budget object with the values that were set in the Builder, those
     * values are BigDecimal allocatedAmount, String givenTo and String givenBy, representing the
     * allocated amount of money, the employee that the budget is allocated to and the manager that allocated the budget.
     * */
    public static class Builder{
        private BigDecimal allocatedAmount;
        private String givenTo;
        private String givenBy;
        public Builder allocateBudgetAmount(BigDecimal allocatedAmount) {
            this.allocatedAmount = allocatedAmount;
            return this;
        }
        public Builder givenTo(String givenTo) {
            this.givenTo = givenTo;
            return this;
        }
        public Builder givenBy(String givenBy) {
            this.givenBy = givenBy;
            return this;
        }
        public Budget build() {
            return new Budget(this);
        }
    }
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount = BigDecimal.ZERO;
    private String givenTo;
    private String givenBy;
    /**
     * The constructor is private, so the only way to create a Budget object is through the Builder class.
     * */
    private Budget(Builder builder) {
        this.givenTo = builder.givenTo;
        this.givenBy = builder.givenBy;
        this.allocatedAmount = builder.allocatedAmount;
    }

    @Override
    public String generateReport() {
        return "This is an automated report for a reportable budget used by: "+givenTo + ". All rights reserved by BTET. " +
                "Any unauthorized use is strictly prohibited! " + "Generated on " + LocalDate.now().
                format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+"." ;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getGivenTo() {
        return givenTo;
    }

    public String getGivenBy() {
        return givenBy;
    }

}
