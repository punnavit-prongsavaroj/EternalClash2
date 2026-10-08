import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDbArmies {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
        String user = "postgres.llyeafyezqnrllgqfkyt";
        String pass = "pGUFO7nirtee0vbm";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE armies ALTER COLUMN target_player_id DROP NOT NULL");
            System.out.println("armies.target_player_id constraint dropped successfully!");
            
            try {
                stmt.execute("ALTER TABLE battles ALTER COLUMN defender_player_id DROP NOT NULL");
                System.out.println("battles.defender_player_id constraint dropped successfully!");
            } catch (Exception e) {}
            
            try {
                stmt.execute("ALTER TABLE turn_actions ALTER COLUMN target_player_id DROP NOT NULL");
                System.out.println("turn_actions.target_player_id constraint dropped successfully!");
            } catch (Exception e) {}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
