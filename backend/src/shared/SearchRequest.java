package shared;

import java.io.Serializable;

/**
 * Αίτημα αναζήτησης παιχνιδιών για player / dummy client.
 */
public class SearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String providerName; // προαιρετικό
    private Integer minStars;    // προαιρετικό
    private String betCategory;  // "$", "$$", "$$$" ή null
    private String riskLevel;    // "low", "medium", "high" ή null
    private Double minBet;       // προαιρετικό
    private Double maxBet;       // προαιρετικό

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public Integer getMinStars() { return minStars; }
    public void setMinStars(Integer minStars) { this.minStars = minStars; }

    public String getBetCategory() { return betCategory; }
    public void setBetCategory(String betCategory) { this.betCategory = betCategory; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getMinBet() { return minBet; }
    public void setMinBet(Double minBet) { this.minBet = minBet; }

    public Double getMaxBet() { return maxBet; }
    public void setMaxBet(Double maxBet) { this.maxBet = maxBet; }
}

