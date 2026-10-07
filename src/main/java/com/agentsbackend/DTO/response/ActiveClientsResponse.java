package com.agentsbackend.DTO.response;

/**
 * ActiveClientsResponse - DTO for active clients metric.
 * Represents the count of active clients on the platform at a given time.
 */
public class ActiveClientsResponse {
    private Long totalActiveClients;
    private Long activeClientsLast30Days;
    private Long totalClients;
    private Double activeClientPercentage;
    private Long activeAccounts;
    private Long totalAccounts;

    // Constructors
    public ActiveClientsResponse() {}

    public ActiveClientsResponse(Long totalActiveClients, Long activeClientsLast30Days, Long totalClients,
                               Double activeClientPercentage, Long activeAccounts, Long totalAccounts) {
        this.totalActiveClients = totalActiveClients;
        this.activeClientsLast30Days = activeClientsLast30Days;
        this.totalClients = totalClients;
        this.activeClientPercentage = activeClientPercentage;
        this.activeAccounts = activeAccounts;
        this.totalAccounts = totalAccounts;
    }

    // Getters and Setters
    public Long getTotalActiveClients() {
        return totalActiveClients;
    }

    public void setTotalActiveClients(Long totalActiveClients) {
        this.totalActiveClients = totalActiveClients;
    }

    public Long getActiveClientsLast30Days() {
        return activeClientsLast30Days;
    }

    public void setActiveClientsLast30Days(Long activeClientsLast30Days) {
        this.activeClientsLast30Days = activeClientsLast30Days;
    }

    public Long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(Long totalClients) {
        this.totalClients = totalClients;
    }

    public Double getActiveClientPercentage() {
        return activeClientPercentage;
    }

    public void setActiveClientPercentage(Double activeClientPercentage) {
        this.activeClientPercentage = activeClientPercentage;
    }

    public Long getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(Long activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public Long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(Long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    @Override
    public String toString() {
        return "ActiveClientsResponse{" +
                "totalActiveClients=" + totalActiveClients +
                ", activeClientsLast30Days=" + activeClientsLast30Days +
                ", totalClients=" + totalClients +
                ", activeClientPercentage=" + activeClientPercentage +
                ", activeAccounts=" + activeAccounts +
                ", totalAccounts=" + totalAccounts +
                '}';
    }
}
