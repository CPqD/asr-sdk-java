package br.com.cpqd.asr.recognizer.model.classifier;

public class EmotionClassResult {
    private String event = "";
    private String emotion = "";

    public EmotionClassResult() {
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

    public String toString() {
        return "[ event=" + event + ", emotion= " + emotion + " ]";
    }

}
