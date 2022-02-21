package com.lindar.dotmailer.vo.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailRequest {
    private List<String> toAddresses;
    private List<String> ccAddresses;
    private List<String> bccAddresses;
    private String fromAddress;
    private String subject;
    private String htmlContent;
    private String plainTextContent;
    private Object metadata;
    private List<EmailAttachment> attachments;
}
