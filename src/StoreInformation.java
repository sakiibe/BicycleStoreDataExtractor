import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
/**
 * Fetch store information from the database
 */
public class StoreInformation {
    private Connection connection;

    public StoreInformation(Connection connection) {
        this.connection = connection;
    }
    /**
     * Fetch the product information from the specified time period
     * @param document -- XML document instance
     * @param startDate -- Start date for customer information
     * @param endDate -- End date for customer information
     * @return --  The root element of the XML subtree
     */
    public Element fetch(Document document, String startDate, String endDate) {
        try {

            Statement statement = connection.createStatement();
            //query to fetch store information for the specified time period
            String query = "SELECT s.store_id, s.store_name, s.city, COUNT(DISTINCT st.staff_id) AS employee_count, COUNT(DISTINCT o.customer_id) AS customers_served " +
                    "FROM stores s " +
                    "LEFT JOIN staffs st ON s.store_id = st.store_id " +
                    "LEFT JOIN orders o ON s.store_id = o.store_id " +
                    "WHERE o.order_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                    "GROUP BY s.store_id " +
                    "ORDER BY s.store_id;";

            ResultSet resultSet = statement.executeQuery(query);

            Element storeList = document.createElement("store_list");
            // Iterate through the results and create elements for each store
            while (resultSet.next()) {
                Element store = document.createElement("store");

                // Create the elements for the store information
                Element storeName = document.createElement("store_name");
                storeName.setTextContent(resultSet.getString("store_name"));
                store.appendChild(storeName);

                Element storeCity = document.createElement("store_city");
                storeCity.setTextContent(resultSet.getString("city"));
                store.appendChild(storeCity);

                Element employeeCount = document.createElement("employee_count");
                employeeCount.setTextContent(resultSet.getString("employee_count"));
                store.appendChild(employeeCount);

                Element customersServed = document.createElement("customers_served");
                customersServed.setTextContent(resultSet.getString("customers_served"));
                store.appendChild(customersServed);

                // Retrieve the store ID to fetch customer sales information for that store
                int storeId = resultSet.getInt("store_id");

                Statement customerStatement = connection.createStatement();

                // SQL query to fetch customer sales for the store within the specified time period
                String customerQuery = "SELECT c.customer_id, c.first_name, c.last_name, SUM(oi.list_price * oi.quantity * (1 - oi.discount)) as customer_sales_value " +
                        "FROM customers c " +
                        "JOIN orders o ON c.customer_id = o.customer_id " +
                        "JOIN order_items oi ON o.order_id = oi.order_id " +
                        "JOIN staffs st ON o.staff_id = st.staff_id " +
                        "WHERE o.order_date BETWEEN '" + startDate + "' AND '" + endDate + "' AND st.store_id = " + storeId + " " +
                        "GROUP BY c.customer_id " +
                        "ORDER BY c.customer_id;";

                ResultSet customerResultSet = customerStatement.executeQuery(customerQuery);

                // Create elements for each customer's sales information and append to the store element
                while (customerResultSet.next()) {
                    Element customerSales = document.createElement("customer_sales");

                    Element customerName = document.createElement("customer_name");
                    customerName.setTextContent(customerResultSet.getString("first_name") + " " + customerResultSet.getString("last_name"));
                    customerSales.appendChild(customerName);

                    Element customerSalesValue = document.createElement("customer_sales_value");
                    customerSalesValue.setTextContent(Double.toString(customerResultSet.getDouble("customer_sales_value")));
                    customerSales.appendChild(customerSalesValue);

                    store.appendChild(customerSales);
                }

                customerResultSet.close();
                customerStatement.close();

                storeList.appendChild(store);
            }

            resultSet.close();
            statement.close();

            return storeList;

        } catch (Exception e) {
            //if error occurs return null
            return null;
        }

    }


}


