package com.lindar.dotmailer.vo.api;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProgramEnrolment {

    private String     id;
    private Long       programId;
    private String     status;
    private Date       dateCreated;
    private List<Long> contacts;
    private List<Long> addressBooks;

}
