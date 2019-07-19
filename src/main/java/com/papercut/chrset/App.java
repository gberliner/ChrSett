package com.papercut.chrset;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        try {
            BufferedReader br = detectEncodingAndCreateReader(
                    new FileInputStream(new File("myfile"))
            );
            br.readLine();
        } catch (Exception e) {

        }
    }
    static private BufferedReader detectEncodingAndCreateReader(InputStream inStream) throws IOException {

        // Copy the input to an byte array so we can look at the content and then re-read it.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(inStream, baos);

        // Use basic heuristic to look for UTF16.  We look for null bytes.  The header of the topup card wizard
        // has the field names (i.e. schema) ... so we know it's UTF16 encoding will have nulls every 2nd byte.
        byte[] srcBytes = baos.toByteArray();
        final int testBytes = Math.min(50, srcBytes.length);
        int nullCount = 0;
        for (int i = 0; i < testBytes; i++) {
            if (srcBytes[i] == 0) {
                nullCount++;
            }
        }

        boolean isUTF16 = false;
        if (nullCount > 0) {
            // looks like it's UTF-16, try it.
            System.out.println("Input contains null characters, testing for UTF16.");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(srcBytes), "UTF-16"))) {
                // Now check if the first line matches our schema/field definition.
                String firstLine = br.readLine();
                if (firstLine != null && firstLine.startsWith("\0377\0376")) {
                    // yup, looks good - so is definitely UTF16.
                    isUTF16 = true;
                } else {
                    System.out.println("But doesn't start with our expected header, might not be UTF-16, " +
                            "try default encoding: " + Charset.defaultCharset());
                }
            } catch (UnsupportedEncodingException e) {
                // should never happen - use default encoding.
            }
        }

        // Now we know the encoding, setup the reader.
        ByteArrayInputStream stream = new ByteArrayInputStream(srcBytes);
        InputStreamReader reader = null;
        if (isUTF16) {
            try {
                reader = new InputStreamReader(stream, "UTF-16");
            } catch (UnsupportedEncodingException e) {
                // should never happen - use default encoding.
            }
        }

        if (reader == null) {
            reader = new InputStreamReader(stream);
        }
        return new BufferedReader(reader);
    }
}
