package org.btet.model;
/**
 * Interface for generating official reports, keeping the authenticity of the report.
 */
public interface Reportable {
    /**
     * Generates an official report by the BTET company guidelines.
     * @return the report as a string.
     */
    String generateReport();
}
