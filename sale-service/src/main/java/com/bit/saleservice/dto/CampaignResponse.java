package com.bit.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * This class represents a response for a campaign.
 * It contains the necessary information about a campaign, such as its ID, name, details, expiration date, and the IDs of the related sales.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignResponse {
    private Long id;
    private String name;
    private String details;
    private Date expiration;
    private List<Long> saleIds;
}
