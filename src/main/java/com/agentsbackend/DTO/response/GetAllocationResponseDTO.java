package com.agentsbackend.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public class GetAllocationResponseDTO {
    private List<IndustryAllocationDTO> industryBreakdown;
    private List<AssetClassAllocationDTO> assetClassBreakdown;

    public GetAllocationResponseDTO() {
    }

    public GetAllocationResponseDTO(List<IndustryAllocationDTO> industryBreakdown,
                                     List<AssetClassAllocationDTO> assetClassBreakdown) {
        this.industryBreakdown = industryBreakdown;
        this.assetClassBreakdown = assetClassBreakdown;
    }

    public List<IndustryAllocationDTO> getIndustryBreakdown() {
        return industryBreakdown;
    }

    public void setIndustryBreakdown(List<IndustryAllocationDTO> industryBreakdown) {
        this.industryBreakdown = industryBreakdown;
    }

    public List<AssetClassAllocationDTO> getAssetClassBreakdown() {
        return assetClassBreakdown;
    }

    public void setAssetClassBreakdown(List<AssetClassAllocationDTO> assetClassBreakdown) {
        this.assetClassBreakdown = assetClassBreakdown;
    }

    // Inner DTO for industry allocation
    public static class IndustryAllocationDTO {
        private String industry;
        private BigDecimal percentage;
        private BigDecimal value;

        public IndustryAllocationDTO() {
        }

        public IndustryAllocationDTO(String industry, BigDecimal percentage, BigDecimal value) {
            this.industry = industry;
            this.percentage = percentage;
            this.value = value;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    // Inner DTO for asset class allocation
    public static class AssetClassAllocationDTO {
        private String assetClass;
        private BigDecimal percentage;
        private BigDecimal value;

        public AssetClassAllocationDTO() {
        }

        public AssetClassAllocationDTO(String assetClass, BigDecimal percentage, BigDecimal value) {
            this.assetClass = assetClass;
            this.percentage = percentage;
            this.value = value;
        }

        public String getAssetClass() {
            return assetClass;
        }

        public void setAssetClass(String assetClass) {
            this.assetClass = assetClass;
        }

        public BigDecimal getPercentage() {
            return percentage;
        }

        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
}
