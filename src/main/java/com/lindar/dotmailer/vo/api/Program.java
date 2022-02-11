package com.lindar.dotmailer.vo.api;

import lombok.Data;

import java.util.Date;

@Data
public class Program {

    private Long   id;
    private String name;
    private String status;
    private Date   dateCreated;

}
