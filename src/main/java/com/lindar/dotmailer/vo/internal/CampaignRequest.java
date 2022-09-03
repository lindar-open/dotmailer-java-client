package com.lindar.dotmailer.vo.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CampaignRequest {
    private String       campaignId;
    private List<String> addressBookIds;
    private List<Long>   contactIds;
}
