package br.com.cpqd.asr.recognizer;

import br.com.cpqd.asr.protocol.WordDetail;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionResultCode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpeechServerTest {

    private static final String url = "ws://localhost:8025/asr-server/asr";
//    private static final String url = "wss://speech.cpqd.com.br/asr/ws/v2/recognize/8k";
    private static final String user = "dsda";
    private static final String passwd = "futuro";
    private static final String filename = "./src/test/resources/audio/pizza-veg-8k.wav";
    private static final String lmName = "builtin:slm/general";

    @Test
    public void basicGrammar() {
        String filename = "./src/test/resources/audio/cpf_8k.wav";
        String lmName = "builtin:grammar/cpf";

        SpeechRecognizer recognizer = null;
        try {
            recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd).
                    version("2.3").channelIdentifier("1234@asr").
                    recogConfig(RecognitionConfig.builder().
                            maxSentences(1).wordDetails(WordDetail.FIRST.getValue()).
                            inferAgeEnabled(true).inferEmotionEnabled(true).inferGenderEnabled(true).
                            build()).
                    build();
            AudioSource audio = new FileAudioSource(new File(filename));
            recognizer.recognize(audio, LanguageModelList.builder().addFromURI(lmName).build());
            List<RecognitionResult> results = recognizer.waitRecognitionResult();

            assertTrue("Number of alternatives is " + results.get(0).getAlternatives().size(),
                    results.get(0).getAlternatives().size() > 0);
            assertTrue("Score is higher than 90", results.get(0).getAlternatives().get(0).getConfidence() > 90);
            assertTrue("Result is recognized", results.get(0).getResultCode() == RecognitionResultCode.RECOGNIZED);
            assertTrue("Contains interpretation",
                    results.get(0).getAlternatives().get(0).getInterpretations().size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        } finally {
            try {
                if (recognizer != null)
                    recognizer.close();
            } catch (IOException e) {
            }
        }
    }
}
