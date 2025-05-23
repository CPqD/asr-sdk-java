package br.com.cpqd.asr.recognizer.model.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AgeResult {
    private String event = "";
    private Integer version = 1;
    private Integer age = 0;
    private Map<String, Double> p = new HashMap<>();
    private String confidence = "";
    private String group = "";
    private String stage = "";
    public AgeResult() {
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
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
        if (version.equals("2"))
            return "[ event=" + event + ", version= " + version +  ", age= " + age + ", group=" + group + ", stage=" + stage + " ]";
        else
            return "[ event=" + event + ", version= " + version +  ", age= " + age + ", p=" + p + ", confidence=" + confidence + " ]";
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStage() {
        return this.stage;
    }
}
