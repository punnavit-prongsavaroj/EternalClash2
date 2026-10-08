import urllib.request
with open('FixDb2.java', 'w', encoding='utf-8') as f:
    f.write('''import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDb2 {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
        String user = "postgres.llyeafyezqnrllgqfkyt";
        String pass = "pGUFO7nirtee0vbm";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE games DROP CONSTRAINT IF EXISTS games_status_check");
            System.out.println("Constraint dropped successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
''')
