package com.lindar.dotmailer.vo.api;

import lombok.Data;

import java.util.List;

@Data
public class NewProgramEnrolment {

    private Long       programId;
    private List<Long> contacts;
    private List<Long> addressBooks;

}
