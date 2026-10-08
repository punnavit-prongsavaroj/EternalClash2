import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDbCitiesUnique {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
        String user = "postgres.llyeafyezqnrllgqfkyt";
        String pass = "pGUFO7nirtee0vbm";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE cities DROP CONSTRAINT IF EXISTS uk_d4w275sr5ucwcgeym3iyy7sv8");
            System.out.println("cities.player_id unique constraint dropped successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
