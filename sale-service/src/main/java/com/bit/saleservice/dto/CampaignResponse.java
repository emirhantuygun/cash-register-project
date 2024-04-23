package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Sale;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignResponse {
    private Long id;
    private String name;
    private String details;
    private Date expiration;
    private List<Long> saleIds = new ArrayList<>();
}
