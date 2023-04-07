import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Scanner;

public class databaseTester {

    public static void main(String[] args) {

        // Get identity information

        Properties identity = new Properties();
        String username = "";
        String password = "";
        String propertyFilename = "src/databaseTester.prop";

        try {
            InputStream stream = new FileInputStream(propertyFilename);

            identity.load(stream);

            username = identity.getProperty("username");
            password = identity.getProperty("password");
        } catch (Exception e) {
            return;
        }


        Connection connect = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            //establish connection to database
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false", username, password );
            statement = connect.createStatement();
            statement.execute("use csci3901;");
            BicycleStore bicycleStore = new BicycleStore(connect);

            //take user input for start and end date, file name
            Scanner sc=new Scanner(System.in);

            System.out.println("Enter start date (YYYY-MM-DD):");
            String startDate= sc.nextLine();

            System.out.println("Enter end date (YYYY-MM-DD):");
            String endDate= sc.nextLine();

            System.out.println("Enter XML file name");
            String outputFile= sc.nextLine();
            bicycleStore.generateXmlOutput(startDate,endDate,outputFile);


            connect.close();
        } catch (Exception e) {
            System.out.println("Connection failed");
            System.out.println(e.getMessage());
        }
    }
}

