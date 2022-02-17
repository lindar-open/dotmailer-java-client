package com.lindar.dotmailer.vo.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NameValue {
    private String name;
    private Object value;
}
