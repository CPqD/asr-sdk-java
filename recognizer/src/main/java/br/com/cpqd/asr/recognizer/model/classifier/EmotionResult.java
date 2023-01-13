package br.com.cpqd.asr.recognizer.model.classifier;

import java.util.HashMap;
import java.util.Map;

public class EmotionResult {
    private String event = "";
    private String emotion = "";
    private String group = "";
    private Map<String, Double> p = new HashMap<>();
    private Map<String, Double> p_groups = new HashMap<>();
    public EmotionResult() {
    }
    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getEmotion() {
        return this.emotion;
    }

    public void setGroup(String group) {
        this.event = group;
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
        return "[ event=" + event + ", emotion= " + emotion + ", group= " + group + ", p=" + p + ", p_groups=" + p_groups + " ]";
    }
}
