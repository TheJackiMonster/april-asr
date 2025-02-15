/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package aprilasr;

import com.sun.jna.Pointer;
import com.sun.jna.NativeLong;
import com.sun.jna.CallbackReference;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    @Test void canLoadModel() {
        Pointer model = AprilAsrNative.aam_create_model("/home/hp/Downloads/aprilv0_en-us.april");
        assertNotNull(model);

        AprilAsrNative.aam_free(model);
    }

    @Test void cantLoadFakeModel() {
        Pointer fake_model = AprilAsrNative.aam_create_model("/tmp/fake123");
        assertNull(fake_model);
    }

    @Test void testZoo() throws IOException {
        URL url = new URL("https://april.sapples.net/zoo.wav");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();
        assertEquals(status, 200);

        InputStream in = con.getInputStream();

        byte[] data = in.readAllBytes();
        short[] shorts = new short[data.length / 2];

        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] << 8));
        }

        Model wrapped_model = new Model("/home/hp/Downloads/aprilv0_en-us.april");
        assertEquals(16000, wrapped_model.getSampleRate());

        final String[] text = {""};
        Session.CallbackHandler handler = new Session.CallbackHandler() {
            @Override
            public void onPartialResult(Token[] tokens) {
                System.out.println("Partial: " + Token.concat(tokens));
            }

            @Override
            public void onFinalResult(Token[] tokens) {
                System.out.println("Final: " + Token.concat(tokens));
                text[0] += Token.concat(tokens) + "\n";
            }

            @Override
            public void onSilence() {
                System.out.println("Silence");
            }

            @Override
            public void onErrorCantKeepUp() {
                System.out.println("Error");
            }
        };

        Session wrapped_session = new Session(wrapped_model, handler);

        wrapped_session.feedPCM16(shorts, shorts.length);
        wrapped_session.flush();

        assertTrue(text[0].contains("ELEPHANT"));
        assertTrue(text[0].contains("COOL"));
    }

    @Test void canUseModel() throws IOException {
        Model wrapped_model = new Model("/home/hp/Downloads/aprilv0_en-us.april");
        assertEquals(16000, wrapped_model.getSampleRate());

        Session.CallbackHandler handler = new Session.CallbackHandler() {
            @Override
            public void onPartialResult(Token[] tokens) {
                System.out.println("Partial: " + Token.concat(tokens));
            }

            @Override
            public void onFinalResult(Token[] tokens) {
                System.out.println("Final: " + Token.concat(tokens));
            }

            @Override
            public void onSilence() {
                System.out.println("Silence");
            }

            @Override
            public void onErrorCantKeepUp() {
                System.out.println("Error");
            }
        };

        Session wrapped_session = new Session(wrapped_model, handler);

        short[] blank = new short[16000];
        wrapped_session.feedPCM16(blank, 1792);
    }
}
