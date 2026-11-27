package use_case.speak_message;

import java.util.Objects;

public final class SpeakMessageOutputData {

    private final String spokenText;

    public SpeakMessageOutputData(String spokenText) {
        this.spokenText = Objects.requireNonNull(spokenText, "spokenText cannot be null");
    }

    public String getSpokenText() {
        return spokenText;
    }
}