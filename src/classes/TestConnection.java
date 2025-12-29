package classes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test de connexion à la base de données
 * Exécuter ce fichier pour vérifier que la connexion fonctionne
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("   TEST DE CONNEXION À LA BASE DE DONNÉES");
        System.out.println("═══════════════════════════════════════════════════\n");

        try {
            // Tester la connexion
            Connection conn = Connect.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion à la base de données réussie!\n");

                // Tester les tables
                System.out.println("📊 Vérification des tables:\n");
                Statement stmt = conn.createStatement();

                // Test 1: Compter les membres
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM member");
                if (rs.next()) {
                    int memberCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'member': " + memberCount + " membres trouvés");
                }
                rs.close();

                // Test 2: Compter les tâches
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM task");
                if (rs.next()) {
                    int taskCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'task': " + taskCount + " tâches trouvées");
                }
                rs.close();

                // Test 3: Compter les compétences
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM skill");
                if (rs.next()) {
                    int skillCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'skill': " + skillCount + " compétences trouvées");
                }
                rs.close();

                // Test 4: Compter les équipes
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM team");
                if (rs.next()) {
                    int teamCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'team': " + teamCount + " équipes trouvées");
                }
                rs.close();

                // Test 5: Compter les projets
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM project");
                if (rs.next()) {
                    int projectCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'project': " + projectCount + " projets trouvés");
                }
                rs.close();

                // Test 6: Compter les alertes
                rs = stmt.executeQuery("SELECT COUNT(*) as count FROM alert");
                if (rs.next()) {
                    int alertCount = rs.getInt("count");
                    System.out.println("   ✓ Table 'alert': " + alertCount + " alertes trouvées");
                }
                rs.close();

                // Test 7: Afficher quelques membres
                System.out.println("\n👥 Exemples de membres:");
                rs = stmt.executeQuery("SELECT first_name, last_name, role FROM member LIMIT 5");
                while (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String role = rs.getString("role");
                    System.out.println("   • " + firstName + " " + lastName + " - " + role);
                }
                rs.close();

                // Test 8: Afficher quelques tâches
                System.out.println("\n📋 Exemples de tâches:");
                rs = stmt.executeQuery("SELECT name, priority, status FROM task LIMIT 5");
                while (rs.next()) {
                    String name = rs.getString("name");
                    String priority = rs.getString("priority");
                    String status = rs.getString("status");
                    System.out.println("   • " + name + " [" + priority + "] - " + status);
                }
                rs.close();

                stmt.close();

                System.out.println("\n═══════════════════════════════════════════════════");
                System.out.println("   ✅ TOUS LES TESTS SONT PASSÉS AVEC SUCCÈS!");
                System.out.println("═══════════════════════════════════════════════════");
                System.out.println("\n💡 La base de données est prête à être utilisée!");
                System.out.println("   Vous pouvez maintenant:");
                System.out.println("   1. Déployer sur Tomcat");
                System.out.println("   2. Tester les endpoints API");
                System.out.println("   3. Lancer l'affectation automatique");

            } else {
                System.err.println("❌ Échec de la connexion à la base de données");
                System.err.println("\n🔧 Vérifiez:");
                System.err.println("   • MySQL est démarré");
                System.err.println("   • La base 'project_management' existe");
                System.err.println("   • User/password dans Connect.java sont corrects");
            }

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR lors du test de connexion:");
            System.err.println("   " + e.getMessage());
            System.err.println("\n🔧 Solutions possibles:");
            System.err.println("   1. Vérifier que MySQL est démarré");
            System.err.println("   2. Exécuter le script: sql/database_complete.sql");
            System.err.println("   3. Vérifier user/password dans Connect.java");
            System.err.println("   4. Vérifier que le port 3306 est disponible");
            System.err.println("\n📖 Voir DATABASE_SETUP.md pour plus de détails");
            e.printStackTrace();
        }
    }
}
