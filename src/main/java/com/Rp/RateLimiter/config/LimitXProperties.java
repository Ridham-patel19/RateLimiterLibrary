package com.Rp.RateLimiter.config;

import com.Rp.RateLimiter.core.RateLimiterAlgo;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "limitx")
public class LimitXProperties {
    private boolean enable = true;
    private int limit = 100;
    private RateLimiterAlgo algorithm = RateLimiterAlgo.TOKEN_BUCKET;
    private long duration = 60000;

    private List<String> excludes = new ArrayList<>();

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isEnable(){
        return enable;
    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public RateLimiterAlgo getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(RateLimiterAlgo algorithm) {
        this.algorithm = algorithm;
    }
}
