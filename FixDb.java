import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
        String user = "postgres.llyeafyezqnrllgqfkyt";
        String pass = "pGUFO7nirtee0vbm";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE cities ALTER COLUMN player_id DROP NOT NULL");
            System.out.println("Constraint dropped successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
