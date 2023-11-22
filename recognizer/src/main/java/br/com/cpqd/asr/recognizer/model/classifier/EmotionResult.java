package br.com.cpqd.asr.recognizer.model.classifier;

import java.util.HashMap;
import java.util.Map;

public class EmotionResult extends EmotionClassResult {
    private String group = "";
    private Map<String, Double> p = new HashMap<>();
    private Map<String, Double> p_groups = new HashMap<>();

    public EmotionResult() {
        super();
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public void setP(Map<String, Double> p) {
        this.p.clear();
        this.p.putAll(p);
    }

    public Map<String, Double> getP() {
        return this.p;
    }

    public void setP_groups(Map<String, Double> p) {
        this.p_groups.clear();
        this.p_groups.putAll(p);
    }

    public Map<String, Double> getP_groups() {
        return this.p_groups;
    }

    public String toString() {
        return "[ event=" + getEvent() + ", emotion= " + getEmotion() + ", group= " + group + ", p=" + p + ", p_groups="
                + p_groups + " ]";
    }
}
