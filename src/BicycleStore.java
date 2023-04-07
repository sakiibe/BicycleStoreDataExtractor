import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BicycleStore {
    private Connection connection;

    public BicycleStore(Connection connection) {
        this.connection = connection;
    }

    public boolean generateXmlOutput(String startDate, String endDate, String outputFile) {

        // if start or end date is null, return false
        if (startDate == null || endDate == null) {
            return false;
        }

        //if date is not in correct format (YYYY-MM-DD), return false
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDateObj;
        Date endDateObj;
        try {
            startDateObj = sdf.parse(startDate);
            endDateObj = sdf.parse(endDate);
        } catch (ParseException e) {
            // Return false if error occurs.
            return false;
        }

        //if startDate comes after endDate, return false
        if (startDateObj.after(endDateObj)){
            return false;
        }

        //if output file is null or empty string, return false
        if (outputFile==null || outputFile.isEmpty()){
            return false;
        }


        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // Create the root element
            Element activitySummary = document.createElement("activity_summary");
            document.appendChild(activitySummary);

            // Create and append time_span element
            Element timeSpan = document.createElement("time_span");
            activitySummary.appendChild(timeSpan);

            Element startDateElement = document.createElement("start_date");
            startDateElement.setTextContent(startDate);
            timeSpan.appendChild(startDateElement);

            Element endDateElement = document.createElement("end_date");
            endDateElement.setTextContent(endDate);
            timeSpan.appendChild(endDateElement);

            // Fetch and append customer, product, and store information
            Element customerList = new CustomerInformation(connection).fetch(document, startDate, endDate);
            if (customerList != null) {
                activitySummary.appendChild(customerList);
            }

            Element productList = new ProductInformation(connection).fetch(document, startDate, endDate);
            if (productList != null) {
                activitySummary.appendChild(productList);
            }

            Element storeList = new StoreInformation(connection).fetch(document, startDate, endDate);
            if (storeList != null) {
                activitySummary.appendChild(storeList);
            }

            // Write the content to the XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(outputFile));

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(domSource, streamResult);

        } catch (Exception e) {
            //return false if error occurs
            return false;
        }
        return true;
    }
}
