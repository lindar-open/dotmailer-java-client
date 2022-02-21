package com.lindar.dotmailer.vo.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailTriggeredCampaignRequest {
    private List<String> toAddresses;
    private List<String> ccAddresses;
    private List<String> bccAddresses;
    private String fromAddress;
    private int campaignId;
    private List<NameValue> personalizationValues;
    private Object metadata;
    private List<EmailAttachment> attachments;

    public static class EmailTriggeredCampaignRequestBuilder {
        private List<NameValue> personalizationValues;

        public <T> EmailTriggeredCampaignRequestBuilder personalizationValues(Map<String, T> personalizationValues) {
            this.personalizationValues = toNameValueList(personalizationValues);
            return this;
        }

        public EmailTriggeredCampaignRequestBuilder personalizationValues(List<NameValue> personalizationValues) {
            this.personalizationValues = personalizationValues;
            return this;
        }

        private <T> List<NameValue> toNameValueList(Map<String, T> map) {
            if (map == null) return null;
            return map.entrySet().stream()
                      .map(entry -> new NameValue(entry.getKey(), entry.getValue()))
                      .collect(Collectors.toList());
        }
    }
}
