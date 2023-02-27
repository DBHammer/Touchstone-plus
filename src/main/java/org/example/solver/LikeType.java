package org.example.solver;

public class LikeType {

    public boolean frontMatch;

    public boolean middleMatch;

    public boolean behindMatch;

    public String percentage;

    public LikeType(boolean frontMatch, boolean middleMatch, boolean behindMatch, String percentage) {
        this.frontMatch = frontMatch;
        this.middleMatch = middleMatch;
        this.behindMatch = behindMatch;
        this.percentage = percentage;
    }

    public boolean isFrontMatch() {
        return frontMatch;
    }

    public void setFrontMatch(boolean frontMatch) {
        this.frontMatch = frontMatch;
    }

    public boolean isMiddleMatch() {
        return middleMatch;
    }

    public void setMiddleMatch(boolean middleMatch) {
        this.middleMatch = middleMatch;
    }

    public boolean isBehindMatch() {
        return behindMatch;
    }

    public void setBehindMatch(boolean behindMatch) {
        this.behindMatch = behindMatch;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}
