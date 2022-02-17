package com.lindar.dotmailer.api;

import com.lindar.dotmailer.util.DefaultEndpoints;
import com.lindar.dotmailer.vo.api.AggregatedBy;
import com.lindar.dotmailer.vo.api.TransactionalEmailStatistics;
import com.lindar.dotmailer.vo.internal.DMAccessCredentials;
import com.lindar.dotmailer.vo.internal.EmailRequest;
import com.lindar.dotmailer.vo.internal.EmailTriggeredCampaignRequest;
import com.lindar.dotmailer.vo.internal.NameValue;
import com.lindar.wellrested.vo.Result;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TransactionalResource extends AbstractResource {

    public TransactionalResource(DMAccessCredentials accessCredentials) {
        super(accessCredentials);
    }

    public Result<Void> send(List<String> toAddresses, int campaignId, Map<String, Object> personalisation, Map<String, Object> metadata) {
        EmailTriggeredCampaignRequest request = EmailTriggeredCampaignRequest
                .builder()
                .toAddresses(toAddresses)
                .campaignId(campaignId)
                .personalizationValues(personalisation)
                .metadata(metadata)
                .build();
        return postAndGetBlankResponse(DefaultEndpoints.EMAIL_TRIGGERED_CAMPAIGN.getPath(), request);
    }

    public Result<Void> send(List<String> toAddresses, int campaignId) {
        return send(toAddresses, campaignId, new HashMap<>(), new HashMap<>());
    }

    public Result<Void> send(String email, int campaignId, Map<String, Object> personalisation) {
        return send(Collections.singletonList(email), campaignId, personalisation, new HashMap<>());
    }

    public Result<Void> send(String email, int campaignId) {
        return send(Collections.singletonList(email), campaignId, new HashMap<>(), new HashMap<>());
    }

    public Result<Void> send(EmailRequest emailRequest) {
        return postAndGetBlankResponse(DefaultEndpoints.EMAIL.getPath(), emailRequest);
    }

    public Result<Void> send(List<EmailRequest> emailRequest) {
        return postAndGetBlankResponse(DefaultEndpoints.EMAIL_BATCH.getPath(), emailRequest);
    }

    public Result<Void> sendTriggeredCampaign(EmailTriggeredCampaignRequest emailRequest) {
        return postAndGetBlankResponse(DefaultEndpoints.EMAIL_TRIGGERED_CAMPAIGN.getPath(), emailRequest);
    }

    public Result<Void> sendTriggeredCampaign(List<EmailTriggeredCampaignRequest> emailRequest) {
        return postAndGetBlankResponse(DefaultEndpoints.EMAIL_TRIGGERED_CAMPAIGN_BATCH.getPath(), emailRequest);
    }

    public Result<TransactionalEmailStatistics> statistics(@NonNull LocalDate startDate, LocalDate endDate, AggregatedBy aggregatedBy) {
        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern(DM_DATE_FORMAT));
        String formattedEndDate = null;

        if (endDate != null) {
            formattedEndDate = endDate.format(DateTimeFormatter.ofPattern(DM_DATE_FORMAT));
        }

        return statistics(formattedStartDate, formattedEndDate, aggregatedBy);
    }

    public Result<TransactionalEmailStatistics> statistics(@NonNull LocalDateTime startDate, LocalDateTime endDate, AggregatedBy aggregatedBy) {
        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern(DM_DATE_TIME_FORMAT));
        String formattedEndDate = null;

        if (endDate != null) {
            formattedEndDate = endDate.format(DateTimeFormatter.ofPattern(DM_DATE_TIME_FORMAT));
        }

        return statistics(formattedStartDate, formattedEndDate, aggregatedBy);
    }

    public Result<TransactionalEmailStatistics> statistics(@NonNull LocalDate startDate, AggregatedBy aggregatedBy) {
        return statistics(startDate, null, aggregatedBy);
    }

    public Result<TransactionalEmailStatistics> statistics(@NonNull LocalDateTime startDate, AggregatedBy aggregatedBy) {
        return statistics(startDate, null, aggregatedBy);
    }

    private Result<TransactionalEmailStatistics> statistics(String formattedStartDate, String formattedEndDate, AggregatedBy aggregatedBy) {
        String path = pathWithParam(DefaultEndpoints.TRANSACTIONAL_EMAIL_STATS_SINCE_DATE.getPath(), formattedStartDate);

        if (formattedEndDate != null) {
            path = addAttrAndValueToPath(path, "endDate", formattedEndDate);
        }

        if (aggregatedBy != null) {
            path = addAttrAndValueToPath(path, "aggregatedBy", aggregatedBy.name());
        }

        return sendAndGet(path, TransactionalEmailStatistics.class);
    }

    private List<NameValue> toNameValueList(Map<String, String> map) {
        if (map == null) return null;
        return map.entrySet().stream()
                  .map(entry -> new NameValue(entry.getKey(), entry.getValue()))
                  .collect(Collectors.toList());
    }

}
