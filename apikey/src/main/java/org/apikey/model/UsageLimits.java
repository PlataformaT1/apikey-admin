package org.apikey.model;

import lombok.Data;

@Data
public class UsageLimits {
    private Integer dailyLimit;
    private Integer limit;
}
