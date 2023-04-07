import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Fetch customer information from the database
 */
public class CustomerInformation {
    private Connection connection;

    public CustomerInformation(Connection connection) {
        this.connection = connection;
    }

    /**
     * Fetch the customer information for the given time period
     * @param document -- XML document instance
     * @param startDate -- Start date for the summary
     * @param endDate -- End date for the summary
     * @return --  The root element of the XML subtree
     */
    public Element fetch(Document document, String startDate, String endDate) {
        try {
            Statement statement = connection.createStatement();
            // SQL query to fetch customer information for the given time period
            String query ="SELECT c.customer_id, c.first_name, c.last_name, c.street, c.city, c.state, c.zip_code, SUM(oi.quantity) as bicycles_purchased, SUM(oi.list_price * oi.quantity * (1 - oi.discount)) as order_value " +
                    "FROM customers c " +
                    "JOIN orders o ON c.customer_id = o.customer_id " +
                    "JOIN order_items oi ON o.order_id = oi.order_id " +
                    "WHERE o.order_date IN (" +
                    "    SELECT MIN(o2.order_date) " +
                    "    FROM orders o2 " +
                    "    WHERE o2.customer_id = c.customer_id " +
                    "    GROUP BY o2.customer_id " +
                    ") " +
                    "AND o.order_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                    "GROUP BY c.customer_id " +
                    "ORDER BY c.customer_id;";



            ResultSet resultSet = statement.executeQuery(query);

            // Create the root element for the customer list
            Element customerList = document.createElement("customer_list");

            // Iterate through each customer in the result set
            while (resultSet.next()) {
                // Get relevant customer information
                String fullName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                double orderValue = resultSet.getDouble("order_value");
                int bicyclesPurchased = resultSet.getInt("bicycles_purchased");

                // Create customer element
                Element customer = document.createElement("customer");
                Element customerName = document.createElement("customer_name");
                customerName.setTextContent(fullName);
                customer.appendChild(customerName);

                // Create address element
                Element address = document.createElement("address");
                Element streetAddress = document.createElement("street_address");
                streetAddress.setTextContent(resultSet.getString("street"));
                address.appendChild(streetAddress);

                //create city element
                Element city = document.createElement("city");
                city.setTextContent(resultSet.getString("city"));
                address.appendChild(city);

                //create state element
                Element state = document.createElement("state");
                state.setTextContent(resultSet.getString("state"));
                address.appendChild(state);
                //create zipcode element
                Element zipCode = document.createElement("zip_code");
                zipCode.setTextContent(resultSet.getString("zip_code"));
                address.appendChild(zipCode);

                customer.appendChild(address);

                // Create order value and bicycles purchased elements
                Element orderValueElement = document.createElement("order_value");
                orderValueElement.setTextContent(Double.toString(orderValue));
                customer.appendChild(orderValueElement);

                Element bicyclesPurchasedElement = document.createElement("bicycles_purchased");
                bicyclesPurchasedElement.setTextContent(Integer.toString(bicyclesPurchased));
                customer.appendChild(bicyclesPurchasedElement);

                // Append the customer element to the customer list
                customerList.appendChild(customer);
            }

            resultSet.close();
            statement.close();

            return customerList;

        } catch (Exception e) {
            //return null if error occurs.
            return null;
        }
    }
}
