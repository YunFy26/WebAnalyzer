package org.example.llm;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingType;


public class Tokenizer {

    private static final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    private static Encoding getEncoding(EncodingType encodingType){
        return registry.getEncoding(encodingType);
    }

    public static int countTokens(String text, EncodingType encodingType){
        Encoding encoding = getEncoding(encodingType);
        return encoding.encode(text).size();
    }

}
