package br.com.cpqd.asr.recognizer.model.classifier;

import java.util.ArrayList;
import java.util.List;

public class GenderResult {
    private String event = "";
    private String gender = "";
    private List<Double> p = new ArrayList<>();

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return this.gender;
    }

    public void setP (List<Double> p) {
        this.p.clear();
        this.p.addAll(p);
    }
    public List<Double> getP() {
        return this.p;
    }

    public String toString() {
        return "[ event=" + event + ", gender= " + gender + ", p=" + p + " ]";
    }
}
