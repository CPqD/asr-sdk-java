/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package br.com.cpqd.asr.recognizer.model.diarization;

/**
 *
 * @author fabiano
 */
public class DiarizationResult {
    private String speaker;
    private float confidence;
    public DiarizationResult() {
        this.speaker = "";
        this.confidence = 0.0F;
    }
    public String getSpeaker() {
        return speaker;
    }
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }
    public float getConfidence() {
        return confidence;
    }
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    @Override
    public String toString() {
        return "DiarizationResult {speaker=" + speaker + ", confidence=" + confidence +"}";
    }
}
