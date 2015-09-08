
import org.enhydra.jdbc.pool.StandardPoolDataSource;
import org.enhydra.jdbc.standard.StandardConnectionPoolDataSource;

import java.util.Properties;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;

public class StandardPoolDataSourceExample {
    private static final String SQL_REQUEST = "select id, foo from testdata";
    private static final String SQL_QUERY = "update testdata set foo = ? where id=1";

    Connection conn;
    String login = null;
    String password = null;
    String url = null;
    String driver = null;

    public StandardPoolDataSourceExample() throws Exception {
        Properties prop = new Properties();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("spy.properties"));
        } catch (Exception e) {
            System.err.println("problem to load properties.");
            e.printStackTrace();
            System.exit(1);
        }

        login = prop.getProperty("login");
        password = prop.getProperty("password");
        url = prop.getProperty("url");
        driver = prop.getProperty("driver");

        StandardConnectionPoolDataSource connect = new StandardConnectionPoolDataSource();

        connect.setUrl(url);
        connect.setDriverName(driver);
        connect.setUser(login);
        connect.setPassword(password);

        // second, we create the pool of connection with the previous object
        StandardPoolDataSource spds = new StandardPoolDataSource(connect);
        spds.setMaxSize(5);
        spds.setMinSize(2);
        spds.setUser(login);
        spds.setPassword(password);

        conn = spds.getConnection(login, password);
        printTable();
        conn.close();
    }

    public void printTable() {
        try {
            PreparedStatement pstmt = conn.prepareStatement(SQL_REQUEST);
            ResultSet rset = pstmt.executeQuery();
            int numcols = rset.getMetaData().getColumnCount();
            for (int i = 1; i <= numcols; i++) {
                System.out.print("\t" + rset.getMetaData().getColumnName(i));
            }
            System.out.println();
            while (rset.next()) {
                for (int i = 1; i <= numcols; i++) {
                    System.out.print("\t" + rset.getString(i));
                }
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTable(int newValue) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
            pstmt.setInt(1, newValue);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void main(String [] argv) throws Exception{
        StandardPoolDataSourceExample spdse = new StandardPoolDataSourceExample();
        System.exit(1);
    }
}
