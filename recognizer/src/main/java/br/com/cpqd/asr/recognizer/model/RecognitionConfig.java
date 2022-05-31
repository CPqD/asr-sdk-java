/*******************************************************************************
 * Copyright 2018 CPqD. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package br.com.cpqd.asr.recognizer.model;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents the recognition process configuration parameters.
 */
public class RecognitionConfig {

    private HashMap<String, String> map = new HashMap<>();

    public Boolean getNoInputTimeoutEnabled() {
        return getBoolean("noInputTimeout.enabled");
    }

    private void setNoInputTimeoutEnabled(Boolean noInputTimeoutEnabled) {
        put("noInputTimeout.enabled", noInputTimeoutEnabled);
    }

    public Integer getNoInputTimeout() {
        return getInteger("noInputTimeout.value");
    }

    private void setNoInputTimeout(Integer noInputTimeout) {
        put("noInputTimeout.value", noInputTimeout);
    }

    public Boolean getRecognitionTimeoutEnabled() {
        return getBoolean("recognitionTimeout.enabled");
    }

    private void setRecognitionTimeoutEnabled(Boolean recognitionTimeoutEnabled) {
        put("recognitionTimeout.enabled", recognitionTimeoutEnabled);
    }

    public Integer getRecognitionTimeout() {
        return getInteger("recognitionTimeout.value");
    }

    private void setRecognitionTimeout(Integer recognitionTimeout) {
        put("recognitionTimeout.value", recognitionTimeout);
    }

    public Boolean getStartInputTimers() {
        return getBoolean("decoder.startInputTimers");
    }

    private void setStartInputTimers(Boolean decoderStartInputTimers) {
        put("decoder.startInputTimers", decoderStartInputTimers);
    }

    public Integer getMaxSentences() {
        return getInteger("decoder.maxSentences");
    }

    private void setMaxSentences(Integer decoderMaxSentences) {
        put("decoder.maxSentences", decoderMaxSentences);
    }

    public Integer getHeadMarginMiliseconds() {
        return getInteger("endpointer.headMargin");
    }

    private void setHeadMarginMiliseconds(Integer endpointerHeadMargin) {
        put("endpointer.headMargin", endpointerHeadMargin);
    }

    public Integer getTailMarginMiliseconds() {
        return getInteger("endpointer.tailMargin");
    }

    private void setTailMarginMiliseconds(Integer endpointerTailMargin) {
        put("endpointer.tailMargin", endpointerTailMargin);
    }

    public Integer getWaitEndMiliseconds() {
        return getInteger("endpointer.waitEnd");
    }

    private void setWaitEndMiliseconds(Integer endpointerWaitEnd) {
        put("endpointer.waitEnd", endpointerWaitEnd);
    }

    public Integer getEndpointerLevelThreshold() {
        return getInteger("endpointer.levelThreshold");
    }

    private void setEndpointerLevelThreshold(Integer endpointerLevelThreshold) {
        put("endpointer.levelThreshold", endpointerLevelThreshold);
    }

    public Integer getEndpointerLevelMode() {
        return getInteger("endpointer.levelMode");
    }

    private void setEndpointerLevelMode(Integer endpointerLevelMode) {
        put("endpointer.levelMode", endpointerLevelMode);
    }

    public Integer getEndpointerAutoLevelLen() {
        return getInteger("endpointer.autoLevelLen");
    }

    private void setEndpointerAutoLevelLen(Integer endpointerAutoLevelLen) {
        put("endpointer.autoLevelLen", endpointerAutoLevelLen);
    }

    public Boolean getContinuousMode() {
        return getBoolean("decoder.continuousMode");
    }

    private void setContinuousMode(Boolean continuousMode) {
        put("decoder.continuousMode", continuousMode);
    }

    public Integer getConfidenceThreshold() {
        return getInteger("decoder.confidenceThreshold");
    }

    private void setConfidenceThreshold(Integer decoderConfidenceThreshold) {
        put("decoder.confidenceThreshold", decoderConfidenceThreshold);
    }

    public Integer getWordDetails() {
        return getInteger("decoder.wordDetails");
    }

    public void setWordDetails(Integer wordDetails) {
        put("decoder.wordDetails", wordDetails);
    }

    public String getWordHints() {
        return getString("hints.words");
    }

    public void setWordHints(String wordHints) {
        put("hints.words", wordHints);
    }

    public String getTextifyFormattingRules() {
        return getString("textify.formatting.rules");
    }

    public void setTextifyFormattingRules(String textifyFormattingRules) {
        put("textify.formatting.rules", textifyFormattingRules);
    }

    public Boolean getTextifyFormattingEnabled() {
        return getBoolean("textify.formatting.enabled");
    }

    public void setTextifyFormattingEnabled(Boolean textifyFormattingEnabled) {
        put("textify.formatting.enabled", textifyFormattingEnabled);
    }

    public Boolean getTextifyEnabled() {
        return getBoolean("textify.enabled");
    }

    public void setTextifyEnabled(Boolean textifyEnabled) {
        put("textify.enabled", textifyEnabled);
    }

    public String getLoggingTag() {
        return getString("loggingTag");
    }

    private void setLoggingTag(String loggingTag) {
        put("loggingTag", loggingTag);
    }

    public String getAccountTag() {
        return getString("license.manager.accountTag");
    }

    private void setAccountTag(String accountTag) {
        put("license.manager.accountTag", accountTag);
    }

    public void setVerifyBuffer(Boolean verifyBuffer) {
        put("Verify-Buffer-Utterance", verifyBuffer);
    }

    public Boolean getVerifyBuffer() {
        return getBoolean("Verify-Buffer-Utterance");
    }

    public Boolean getInferAgeEnabled() {
        return getBoolean("Infer-age-enabled");
    }

    public void setInferAgeEnabled(Boolean inferAgeEnabled) {
        put("Infer-age-enabled", inferAgeEnabled);
    }

    public Boolean getInferEmotionEnabled() {
        return getBoolean("Infer-emotion-enabled");
    }

    public void setInferEmotionEnabled(Boolean inferEmotionEnabled) {
        put("Infer-emotion-enabled", inferEmotionEnabled);
    }

    public Boolean getInferGenderEnabled() {
        return getBoolean("Infer-gender-enabled");
    }

    public void setInferGenderEnabled(Boolean inferGenderEnabled) {
        put("Infer-gender-enabled", inferGenderEnabled);
    }

    private void put(String key, Object value) {
        if (value != null)
            this.map.put(key, value.toString());
    }

    private Boolean getBoolean(String key) {
        String value = this.map.get(key);
        return (value == null ? null : Boolean.parseBoolean(value));
    }

    private Integer getInteger(String key) {
        String value = this.map.get(key);
        return (value == null ? null : Integer.parseInt(value));
    }

    private String getString(String key) {
        return this.map.get(key);
    }

    public HashMap<String, String> getParameterMap() {
        return this.map;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("RecognitionConfig [");
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            if (map.get(key) != null && map.get(key).trim().length() > 0) {
                str.append(key + "=" + map.get(key));
            }

            if (it.hasNext()) {
                str.append(", ");
            }
        }

        str.append("]");
        return str.toString();
    }

    /**
     * Creates a new instance of the object builder.
     * 
     * @return the Builder object.
     */
    public static RecognitionConfig.Builder builder() {
        return new RecognitionConfig.Builder();
    }

    /**
     * The Builder object.
     *
     */
    public static class Builder {

        private Boolean continuousMode;
        private Integer confidenceThreshold;
        private Integer maxSentences;
        private Integer noInputTimeoutMilis;
        private Integer recognitionTimeoutSeconds;
        private Integer headMarginMilis;
        private Integer tailMarginMilis;
        private Integer waitEndMilis;
        private Boolean recognitionTimeoutEnabled;
        private Boolean noInputTimeoutEnabled;
        private Boolean startInputTimers;
        private Integer endPointerAutoLevelLen;
        private Integer endPointerLevelMode;
        private Integer endPointerLevelThreshold;
        private Integer wordDetails;
        private String wordHints;
        private Boolean textifyEnabled;
        private Boolean textifyFormattingEnabled;
        private String textifyFormattingRules;
        private String loggingTag;
        private String accountTag;
        private Boolean verifyBuffer;

        // speech server
        private Boolean inferAgeEnabled;
        private Boolean inferEmotionEnabled;
        private Boolean inferGenderEnabled;

        /**
         * Creates a new instance of the RecognitionConfig object.
         * 
         * @return a RecognitionConfig instance.
         */
        public RecognitionConfig build() {
            RecognitionConfig config = new RecognitionConfig();
            config.setContinuousMode(continuousMode);
            config.setConfidenceThreshold(confidenceThreshold);
            config.setMaxSentences(maxSentences);
            config.setNoInputTimeout(noInputTimeoutMilis);
            config.setRecognitionTimeout(recognitionTimeoutSeconds);
            config.setHeadMarginMiliseconds(headMarginMilis);
            config.setTailMarginMiliseconds(tailMarginMilis);
            config.setWaitEndMiliseconds(waitEndMilis);
            config.setStartInputTimers(startInputTimers);
            config.setEndpointerAutoLevelLen(endPointerAutoLevelLen);
            config.setEndpointerLevelMode(endPointerLevelMode);
            config.setEndpointerLevelThreshold(endPointerLevelThreshold);
            config.setNoInputTimeoutEnabled(noInputTimeoutEnabled);
            config.setRecognitionTimeoutEnabled(recognitionTimeoutEnabled);
            config.setWordDetails(wordDetails);
            config.setWordHints(wordHints);
            config.setTextifyEnabled(textifyEnabled);
            config.setTextifyFormattingEnabled(textifyFormattingEnabled);
            config.setTextifyFormattingRules(textifyFormattingRules);
            config.setLoggingTag(loggingTag);
            config.setAccountTag(accountTag);
            config.setVerifyBuffer(verifyBuffer);

            config.setInferAgeEnabled(inferAgeEnabled);
            config.setInferEmotionEnabled(inferEmotionEnabled);
            config.setInferGenderEnabled(inferGenderEnabled);

            return config;
        }

        public RecognitionConfig.Builder continuousMode(Boolean value) {
            this.continuousMode = value;
            return this;
        }

        public RecognitionConfig.Builder confidenceThreshold(Integer value) {
            this.confidenceThreshold = value;
            return this;
        }

        public RecognitionConfig.Builder maxSentences(Integer value) {
            this.maxSentences = value;
            return this;
        }

        public RecognitionConfig.Builder noInputTimeoutMilis(Integer value) {
            this.noInputTimeoutMilis = value;
            return this;
        }

        public RecognitionConfig.Builder recognitionTimeoutSeconds(Integer value) {
            this.recognitionTimeoutSeconds = value;
            return this;
        }

        public RecognitionConfig.Builder headMarginMilis(Integer value) {
            this.headMarginMilis = value;
            return this;
        }

        public RecognitionConfig.Builder tailMarginMilis(Integer value) {
            this.tailMarginMilis = value;
            return this;
        }

        public RecognitionConfig.Builder waitEndMilis(Integer value) {
            this.waitEndMilis = value;
            return this;
        }

        public RecognitionConfig.Builder startInputTimers(Boolean value) {
            this.startInputTimers = value;
            return this;
        }

        public RecognitionConfig.Builder endPointerAutoLevelLen(Integer value) {
            this.endPointerAutoLevelLen = value;
            return this;
        }

        public RecognitionConfig.Builder endPointerLevelMode(Integer value) {
            this.endPointerLevelMode = value;
            return this;
        }

        public RecognitionConfig.Builder endPointerLevelThreshold(Integer value) {
            this.endPointerLevelThreshold = value;
            return this;
        }

        public RecognitionConfig.Builder noInputTimeoutEnabled(Boolean value) {
            this.noInputTimeoutEnabled = value;
            return this;
        }

        public RecognitionConfig.Builder recognitionTimeoutEnabled(Boolean value) {
            this.recognitionTimeoutEnabled = value;
            return this;
        }

        public RecognitionConfig.Builder textifyEnabled(Boolean textifyEnabled) {
            this.textifyEnabled = textifyEnabled;
            return this;
        }

        public RecognitionConfig.Builder textifyFormattingEnabled(Boolean textifyFormattingEnabled) {
            this.textifyFormattingEnabled = textifyFormattingEnabled;
            return this;
        }

        public RecognitionConfig.Builder textifyFormattingRules(String textifyFormattingRules) {
            this.textifyFormattingRules = textifyFormattingRules;
            return this;
        }

        public RecognitionConfig.Builder wordDetails(int wordDetails) {
            this.wordDetails = wordDetails;
            return this;
        }

        public RecognitionConfig.Builder wordHints(String wordHints) {
            this.wordHints = wordHints;
            return this;
        }

        public RecognitionConfig.Builder loggingTag(String value) {
            this.loggingTag = value;
            return this;
        }

        public RecognitionConfig.Builder accountTag(String value) {
            this.accountTag = value;
            return this;
        }

        public RecognitionConfig.Builder verifyBuffer(boolean value) {
            this.verifyBuffer = value;
            return this;
        }

        public RecognitionConfig.Builder inferAgeEnabled(Boolean value) {
            this.inferAgeEnabled = value;
            return this;
        }

        public RecognitionConfig.Builder inferEmotionEnabled(Boolean value) {
            this.inferEmotionEnabled = value;
            return this;
        }

        public RecognitionConfig.Builder inferGenderEnabled(Boolean value) {
            this.inferGenderEnabled = value;
            return this;
        }
    }

}
