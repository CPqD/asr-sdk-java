package br.com.cpqd.asr.recognizer.model.classifier;

public class ClassifierResult {
    private GenderResult genderResult;
    private AgeResult ageResult;
    private EmotionResult emotionResult;


    public void setGenderResult(GenderResult genderResult) {
        this.genderResult = genderResult;
    }

    public GenderResult getGenderResult() {
        return genderResult;
    }

    public void setAgeResult(AgeResult ageResult) {
        this.ageResult = ageResult;
    }

    public AgeResult getAgeResult() {
        return ageResult;
    }

    public void setEmotionResult(EmotionResult emotionResult) {
        this.emotionResult = emotionResult;
    }

    public EmotionResult getEmotionResult() {
        return emotionResult;
    }

    @Override
    public String toString() {
        String str = "";
        if (ageResult != null)
            str += ageResult.toString();
        if (emotionResult != null)
            str += " " + emotionResult.toString();
        if (genderResult != null)
            str += " " + genderResult.toString();
        return str;
    }
}
