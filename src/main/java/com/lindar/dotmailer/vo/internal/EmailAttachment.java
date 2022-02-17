package com.lindar.dotmailer.vo.internal;

import lombok.Data;

@Data
public class EmailAttachment {
    private String fileName;
    private String mimeType;
    private String content;
}
