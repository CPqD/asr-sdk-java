package br.com.cpqd.asr.recognizer.model.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AgeResult {
    private String event = "";
    private Integer age = 0;
    private Map<String, Double> p = new HashMap<>();
    private String confidence = "";
    public AgeResult() {
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setConfidence(String confidence) {
        this.event = confidence;
    }

    public String getConfidence() {
        return this.confidence;
    }

    public void setP(Map<String, Double> p) {
        this.p.clear();
        this.p.putAll(p);
    }

    public Map<String, Double> getP() {
        return this.p;
    }

    public String toString() {
        return "[ event=" + event + ", age= " + age + ", p=" + p + ", confidence=" + confidence + " ]";
    }
}
