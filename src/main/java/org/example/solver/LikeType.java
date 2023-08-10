package org.example.solver;

public class LikeType {

    public boolean frontMatch;

    public boolean middleMatch;

    public boolean behindMatch;

    public String rows;

    public LikeType(boolean frontMatch, boolean middleMatch, boolean behindMatch, String rows) {
        this.frontMatch = frontMatch;
        this.middleMatch = middleMatch;
        this.behindMatch = behindMatch;
        this.rows = rows;
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

    public String getRows() {
        return rows;
    }

    public void setRows(String percentage) {
        this.rows = percentage;
    }

    public boolean isOnlyFrontMatch() {
        if (frontMatch && !middleMatch && !behindMatch) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnlyBehindMatch() {
        if (!frontMatch && !middleMatch && behindMatch) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnlyMiddleMatch() {
        if (frontMatch && !middleMatch && behindMatch) {
            return true;
        } else {
            return false;
        }
    }

    public void setOnlyBehindMatch() {
        this.behindMatch = true;
        this.middleMatch = false;
        this.frontMatch = false;
    }
}
