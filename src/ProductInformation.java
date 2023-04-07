import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Fetch product information from the database
 */
public class ProductInformation {
    private Connection connection;

    public ProductInformation(Connection connection) {
        this.connection = connection;
    }

    /**
     * Fetch the product information from the specified time period
     * @param document -- XML document instance
     * @param startDate -- Start date for product information
     * @param endDate -- End date for product information
     * @return --  The root element of the XML subtree
     */

    public Element fetch(Document document, String startDate, String endDate) {
        try {
            Statement statement = connection.createStatement();
            //Query to fetch product information within the given date range
            String query = "SELECT p.product_name, b.brand_name, c.category_name, s.store_name, SUM(oi.quantity) AS units_sold, p.product_id " +
                    "FROM products p " +
                    "JOIN brands b ON p.brand_id = b.brand_id " +
                    "JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN order_items oi ON p.product_id = oi.product_id " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "JOIN stores s ON o.store_id = s.store_id " +
                    "WHERE o.order_date BETWEEN '" + startDate + "' AND '" + endDate + "' " +
                    "GROUP BY p.product_id, s.store_id " +
                    "HAVING p.product_id = MIN(oi.product_id) " +
                    "ORDER BY p.product_id;";

            ResultSet resultSet = statement.executeQuery(query);

            Element productList = document.createElement("product_list");

            // Iterate through the result set and create XML elements for each product
            while (resultSet.next()) {
                Element newProduct = document.createElement("new_product");
                // Create and append product_name element
                Element productName = document.createElement("product_name");
                productName.setTextContent(resultSet.getString("product_name"));
                newProduct.appendChild(productName);

                // Create and append brand element
                Element brand = document.createElement("brand");
                brand.setTextContent(resultSet.getString("brand_name"));
                newProduct.appendChild(brand);
                // Create and append category element
                Element category = document.createElement("category");
                category.setTextContent(resultSet.getString("category_name"));
                newProduct.appendChild(category);

                Element storeSales = document.createElement("store_sales");

                // Create and append store_name element
                Element storeName = document.createElement("store_name");
                storeName.setTextContent(resultSet.getString("store_name"));
                storeSales.appendChild(storeName);

                // Create and append units_sold element
                Element unitsSold = document.createElement("units_sold");
                unitsSold.setTextContent(Integer.toString(resultSet.getInt("units_sold")));
                storeSales.appendChild(unitsSold);

                // Append store_sales to new_product and new_product to productList
                newProduct.appendChild(storeSales);

                productList.appendChild(newProduct);
            }

            resultSet.close();
            statement.close();

            return productList;

        } catch (Exception e) {
            return null;
        }
    }
}
