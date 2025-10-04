import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class XmlParserScript {

    // The URL of the XML file to parse.
    private static final String XML_URL = "https://www.tidetimes.org.uk/galway-tide-times-7.rss";
    
    // The XML elements we are interested in.
    private static final String ITEM_ELEMENT = "item";
    private static final String DESCRIPTION_ELEMENT = "description";

    public static void main(String[] args) {
        System.out.println("Fetching and parsing XML from: " + XML_URL);
        
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        try (InputStream in = new URL(XML_URL).openStream()) {
            reader = factory.createXMLStreamReader(in);

            boolean inItem = false;
            int dayCounter = 1;

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if (ITEM_ELEMENT.equalsIgnoreCase(elementName)) {
                            inItem = true;
                        } else if (DESCRIPTION_ELEMENT.equalsIgnoreCase(elementName) && inItem) {
                            String descriptionContent = reader.getElementText();
                            System.out.println("\n--- Day " + dayCounter++ + " Tide Information ---");
                            parseAndPrintDescription(descriptionContent);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (ITEM_ELEMENT.equalsIgnoreCase(reader.getLocalName())) {
                            inItem = false;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred during XML processing:");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Suppress close exception
                }
            }
        }
    }

    /**
     * Parses the description content to extract and print tide times,
     * similar to the logic in the Android app.
     * @param description The raw content from the <description> tag.
     */
    private static void parseAndPrintDescription(String description) {
        // This regex is adapted from your TidesService to find tide information.
        Pattern ptrn = Pattern.compile("(\\d{2}:\\d{2}\\s-\\s)(Low|High)(\\sTide\\s\\(\\d\\.\\d{1,2}m\\))");
        Matcher mtchr = ptrn.matcher(description);
        
        boolean found = false;
        while (mtchr.find()) {
            System.out.println(mtchr.group());
            found = true;
        }

        if (!found) {
            System.out.println("No tide times found in description.");
        }
    }
}
