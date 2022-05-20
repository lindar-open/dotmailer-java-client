package com.lindar.dotmailer.api;

import com.google.gson.reflect.TypeToken;
import com.lindar.dotmailer.util.DefaultEndpoints;
import com.lindar.dotmailer.vo.api.Campaign;
import com.lindar.dotmailer.vo.api.CampaignContactActivity;
import com.lindar.dotmailer.vo.api.CampaignInfo;
import com.lindar.dotmailer.vo.api.CampaignSummary;
import com.lindar.dotmailer.vo.internal.CampaignRequest;
import com.lindar.dotmailer.vo.internal.DMAccessCredentials;
import com.lindar.wellrested.vo.Result;
import com.lindar.wellrested.vo.ResultBuilder;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CampaignResource extends AbstractResource {
    
    public CampaignResource(DMAccessCredentials accessCredentials) {
        super(accessCredentials);
    }
    
    /**
     * Returns a list of Campaigns including only the base information and <b>without email html content</b>.
     * If you require all the information about each campaign then use the <b>listComprehensive</b> method
     */
    public Result<List<CampaignInfo>> list() {
        return sendAndGetFullList(DefaultEndpoints.CAMPAIGNS.getPath(), new TypeToken<List<CampaignInfo>>() {});
    }
    
    /**
     * Returns a list of Campaigns including only the base information and <b>without email html content</b>.
     * If you require all the information about each campaign then use the <b>listComprehensive</b> method
     */
    public Result<List<CampaignInfo>> listWithActivitySince(Date startDate) {
        return sendAndGetFullList(pathWithParam(DefaultEndpoints.CAMPAIGNS_WITH_ACTIVITY_SINCE.getPath(), new DateTime(startDate).toString(DM_DATE_FORMAT)), new TypeToken<List<CampaignInfo>>() {});
    }

    public Result<List<CampaignInfo>> listWithActivitySince(Date startDate, boolean roundToDate) {
        String dateTemplate = roundToDate ? DM_DATE_FORMAT : DM_DATE_TIME_FORMAT;
        return sendAndGetFullList(pathWithParam(DefaultEndpoints.CAMPAIGNS_WITH_ACTIVITY_SINCE.getPath(), new DateTime(startDate).toString(dateTemplate)), new TypeToken<List<CampaignInfo>>() {});
    }
    
    /**
     * Returns a list of Campaigns including html content, summary and all the activities. 
     * If you require only the base information about each campaign then use the <b>list</b> method
     */
    public Result<List<Campaign>> listComprehensive() {
        Result<List<CampaignInfo>> campaignsResult = sendAndGetFullList(DefaultEndpoints.CAMPAIGNS.getPath(), new TypeToken<List<CampaignInfo>>() {});
        if (!campaignsResult.isSuccessAndNotNull() || campaignsResult.getData().isEmpty()) {
            return ResultBuilder.of(campaignsResult).buildAndIgnoreData();
        }
        List<Campaign> comprehensiveCampaigns = campaignsResult.getData().stream()
                .map(baseCampaign -> get(baseCampaign.getId()).orElse(new Campaign(baseCampaign)))
                .collect(Collectors.toList());
        
        return ResultBuilder.successful(comprehensiveCampaigns);
    }
    
    /**
     * Returns the full information about a campaign, including html content, summary and all the activities. 
     * In order to do this, the client will make at least 3 requests (or more depending on the activities), 
     * for each type of information and stitch the data together in one Campaign object.
     * 
     * Please note: if you only need the info, the summary or the activity then use the specific methods allocated. Enjoy!
     */
    public Result<Campaign> get(Long id) {
        Result<CampaignInfo> info = info(id);
        if (!info.isSuccessAndNotNull()) {
            return ResultBuilder.of(info).buildAndIgnoreData();
        }
        Campaign campaign = new Campaign(info.getData());
        summary(id).ifSuccessAndNotNull(campaign::setSummary);
        activities(id).ifSuccessAndNotNull(campaign::setActivities);

        return ResultBuilder.successful(campaign);
    }
    
    public Result<CampaignInfo> info(Long id) {
        String path = pathWithId(DefaultEndpoints.CAMPAIGN_INFO.getPath(), id);
        return sendAndGet(path, CampaignInfo.class);
    }
    
    public Result<CampaignSummary> summary(Long id) {
        String path = pathWithId(DefaultEndpoints.CAMPAIGN_SUMMARY.getPath(), id);
        return sendAndGet(path, CampaignSummary.class);
    }
    
    public Result<List<CampaignContactActivity>> activities(Long id) {
        String path = pathWithId(DefaultEndpoints.CAMPAIGN_ACTIVITY.getPath(), id);
        return sendAndGetFullList(path, new TypeToken<List<CampaignContactActivity>>() {});
    }
    

    public Result<List<CampaignContactActivity>> activitiesSince(Long id, Date startDate, boolean roundToDate) {
        String dateTemplate = roundToDate ? DM_DATE_FORMAT : DM_DATE_TIME_FORMAT;
        String path = pathWithIdAndParam(DefaultEndpoints.CAMPAIGN_ACTIVITY_SINCE.getPath(), id, new DateTime(startDate).toString(dateTemplate));
        return sendAndGetFullList(path, new TypeToken<List<CampaignContactActivity>>() {});
    }

    public Result<List<CampaignContactActivity>> activitiesSince(Long id, Date startDate) {
        return activitiesSince(id, startDate, true);
    }

    public Result<CampaignInfo> update(CampaignInfo updateCampaign) {
        return putAndGet(pathWithId(DefaultEndpoints.CAMPAIGN_INFO.getPath(), updateCampaign.getId()), updateCampaign);
    }

    public Result<Void> send(CampaignRequest campaignRequest) {
        return postAndGetBlankResponse(DefaultEndpoints.CAMPAIGN_SEND.getPath(), campaignRequest);
    }
}
