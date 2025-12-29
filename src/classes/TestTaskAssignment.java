package classes;

import service.TaskAssignmentService;
import service.AssignmentResult;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de test pour l'algorithme d'affectation automatique des tâches
 * Teste tous les scénarios décrits dans le sujet du projet
 */
public class TestTaskAssignment {

        private static String repeat(String str, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                        sb.append(str);
                }
                return sb.toString();
        }

        public static void main(String[] args) {
                System.out.println("╔══════════════════════════════════════════════════════════════╗");
                System.out.println("║   TEST DE L'ALGORITHME D'AFFECTATION AUTOMATIQUE DES TÂCHES  ║");
                System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

                // PRÉPARATION: Créer l'environnement de test
                TestEnvironment env = setupTestEnvironment();

                // SCÉNARIO 1: Affectation automatique initiale
                System.out.println("\n" + repeat("=", 70));
                System.out.println("SCÉNARIO 1: AFFECTATION AUTOMATIQUE INITIALE");
                System.out.println(repeat("=", 70));
                testInitialAssignment(env);

                // SCÉNARIO 2: Détection de surcharge
                System.out.println("\n" + repeat("=", 70));
                System.out.println("SCÉNARIO 2: DÉTECTION DE SURCHARGE");
                System.out.println(repeat("=", 70));
                testOverloadDetection(env);

                // SCÉNARIO 3: Ajout de tâche urgente en cours de projet
                System.out.println("\n" + repeat("=", 70));
                System.out.println("SCÉNARIO 3: AJOUT DE TÂCHE URGENTE");
                System.out.println(repeat("=", 70));
                testUrgentTaskAddition(env);

                // SCÉNARIO 4: Vérification de l'équilibre des charges
                System.out.println("\n" + repeat("=", 70));
                System.out.println("SCÉNARIO 4: ÉQUILIBRE DES CHARGES");
                System.out.println(repeat("=", 70));
                testLoadBalance(env);

                // SCÉNARIO 5: Génération du rapport
                System.out.println("\n" + repeat("=", 70));
                System.out.println("SCÉNARIO 5: RAPPORT DE RÉPARTITION");
                System.out.println(repeat("=", 70));
                testReportGeneration(env);

                System.out.println("\n" + repeat("=", 70));
                System.out.println("✓ TOUS LES TESTS TERMINÉS AVEC SUCCÈS!");
                System.out.println(repeat("=", 70) + "\n");
        }

        /**
         * Configure l'environnement de test avec membres, compétences et tâches
         */
        private static TestEnvironment setupTestEnvironment() {
                System.out.println("⚙ Configuration de l'environnement de test...\n");

                TestEnvironment env = new TestEnvironment();

                // 1. Créer les compétences
                Skill java = new Skill(1, "Java");
                Skill python = new Skill(2, "Python");
                Skill design = new Skill(3, "UI/UX Design");
                Skill testing = new Skill(4, "Testing");
                Skill devops = new Skill(5, "DevOps");

                // 2. Créer une équipe
                Team team = new Team(1, "Équipe Alpha");

                // 3. Créer 5 membres avec compétences variées
                Member alice = new Member(1, "Alice", "Dupont", "alice@example.com", "password123", "Developer", 0.0,
                                true, new ArrayList<>(), new ArrayList<>(), team);
                MemberSkill aliceJava = new MemberSkill(1, alice.getId(), java.getId(), 5);
                MemberSkill alicePython = new MemberSkill(2, alice.getId(), python.getId(), 4);
                alice.getMemberSkills().add(aliceJava);
                alice.getMemberSkills().add(alicePython);

                Member bob = new Member(2, "Bob", "Martin", "bob@example.com", "password123", "Designer", 0.0,
                                true, new ArrayList<>(), new ArrayList<>(), team);
                MemberSkill bobDesign = new MemberSkill(3, bob.getId(), design.getId(), 5);
                bob.getMemberSkills().add(bobDesign);

                Member charlie = new Member(3, "Charlie", "Dubois", "charlie@example.com", "password123", "Tester", 0.0,
                                true, new ArrayList<>(), new ArrayList<>(), team);
                MemberSkill charlieTesting = new MemberSkill(4, charlie.getId(), testing.getId(), 4);
                MemberSkill charlieJava = new MemberSkill(5, charlie.getId(), java.getId(), 3);
                charlie.getMemberSkills().add(charlieTesting);
                charlie.getMemberSkills().add(charlieJava);

                Member diana = new Member(4, "Diana", "Lambert", "diana@example.com", "password123", "DevOps", 0.0,
                                true, new ArrayList<>(), new ArrayList<>(), team);
                MemberSkill dianaDevops = new MemberSkill(6, diana.getId(), devops.getId(), 5);
                MemberSkill dianaPython = new MemberSkill(7, diana.getId(), python.getId(), 3);
                diana.getMemberSkills().add(dianaDevops);
                diana.getMemberSkills().add(dianaPython);

                Member eve = new Member(5, "Eve", "Rousseau", "eve@example.com", "password123", "Full-Stack", 0.0,
                                true, new ArrayList<>(), new ArrayList<>(), team);
                MemberSkill eveJava = new MemberSkill(8, eve.getId(), java.getId(), 4);
                MemberSkill eveDesign = new MemberSkill(9, eve.getId(), design.getId(), 3);
                eve.getMemberSkills().add(eveJava);
                eve.getMemberSkills().add(eveDesign);

                env.members.add(alice);
                env.members.add(bob);
                env.members.add(charlie);
                env.members.add(diana);
                env.members.add(eve);

                // 4. Créer 10 tâches avec différentes priorités et durées
                env.tasks.add(new Task(1, "Backend API REST", "Développer l'API REST principale", 40.0,
                                LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 15), "Haute", "Planifiée"));

                env.tasks.add(new Task(2, "Base de données MySQL", "Concevoir et implémenter la BD", 30.0,
                                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 2, 1), "Haute", "Planifiée"));

                env.tasks.add(new Task(3, "Interface utilisateur", "Concevoir l'UI/UX", 35.0,
                                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 1), "Haute", "Planifiée"));

                env.tasks.add(new Task(4, "Tests unitaires Backend", "Écrire tests unitaires", 25.0,
                                LocalDate.of(2025, 2, 15), LocalDate.of(2025, 3, 10), "Moyenne", "Planifiée"));

                env.tasks.add(new Task(5, "Tests d'intégration", "Tests d'intégration complets", 30.0,
                                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 25), "Moyenne", "Planifiée"));

                env.tasks.add(new Task(6, "Configuration CI/CD", "Setup pipeline CI/CD", 20.0,
                                LocalDate.of(2025, 1, 20), LocalDate.of(2025, 2, 10), "Moyenne", "Planifiée"));

                env.tasks.add(new Task(7, "Documentation API", "Documenter l'API (Swagger)", 15.0,
                                LocalDate.of(2025, 3, 15), LocalDate.of(2025, 4, 1), "Basse", "Planifiée"));

                env.tasks.add(new Task(8, "Optimisation performance", "Optimiser les requêtes", 25.0,
                                LocalDate.of(2025, 3, 20), LocalDate.of(2025, 4, 10), "Basse", "Planifiée"));

                env.tasks.add(new Task(9, "Sécurité et authentification", "Implémenter JWT", 35.0,
                                LocalDate.of(2025, 2, 5), LocalDate.of(2025, 2, 28), "Haute", "Planifiée"));

                env.tasks.add(new Task(10, "Déploiement production", "Déployer en production", 20.0,
                                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 15), "Moyenne", "Planifiée"));

                System.out.println("✓ Environnement configuré:");
                System.out.println("  - " + env.members.size() + " membres créés");
                System.out.println("  - " + env.tasks.size() + " tâches créées");

                return env;
        }

        /**
         * Teste l'affectation automatique initiale
         */
        private static void testInitialAssignment(TestEnvironment env) {
                TaskAssignmentService service = new TaskAssignmentService(env.members, env.tasks);

                System.out.println("\n📋 Lancement de l'affectation automatique...\n");

                AssignmentResult result = service.assignTasksAutomatically();

                System.out.println(result);

                System.out.println("\n📊 État final des membres:");
                for (Member member : env.members) {
                        System.out.println("  • " + member.getFirstName() + " " + member.getLastName() +
                                        ": " + member.getCurrentLoad() + "h (" +
                                        member.getAssignedTasks().size() + " tâches)");
                }
        }

        /**
         * Teste la détection de surcharge
         */
        private static void testOverloadDetection(TestEnvironment env) {
                System.out.println("\n🔄 Simulation d'une surcharge manuelle...\n");

                // Surcharger Alice manuellement
                Member alice = env.members.get(0);
                Task extraTask1 = new Task(101, "Tâche supplémentaire 1", "Extra", 60.0,
                                LocalDate.now(), LocalDate.now().plusDays(30), "Haute", "Planifiée");
                Task extraTask2 = new Task(102, "Tâche supplémentaire 2", "Extra", 50.0,
                                LocalDate.now(), LocalDate.now().plusDays(30), "Haute", "Planifiée");

                alice.getAssignedTasks().add(extraTask1);
                alice.setCurrentLoad(alice.getCurrentLoad() + 60.0);
                alice.getAssignedTasks().add(extraTask2);
                alice.setCurrentLoad(alice.getCurrentLoad() + 50.0);

                System.out.println("Alice surchargée: " + alice.getCurrentLoad() + "h");

                // Détecter les surcharges
                TaskAssignmentService service = new TaskAssignmentService(env.members, env.tasks);
                List<Alert> alerts = service.detectOverloadedMembers();

                System.out.println("\n🚨 Alertes détectées: " + alerts.size());
                for (Alert alert : alerts) {
                        System.out.println("  [" + alert.getSeverityLevel() + "] " + alert.getMessage());
                }

                // Restaurer l'état
                alice.getAssignedTasks().remove(extraTask1);
                alice.getAssignedTasks().remove(extraTask2);
                alice.setCurrentLoad(alice.getCurrentLoad() - 110.0);
        }

        /**
         * Teste l'ajout d'une tâche urgente
         */
        private static void testUrgentTaskAddition(TestEnvironment env) {
                System.out.println("\n⚡ Ajout d'une tâche urgente en cours de projet...\n");

                Task urgentTask = new Task(201, "BUG CRITIQUE - Correction de sécurité",
                                "Corriger vulnérabilité critique", 15.0,
                                LocalDate.now(), LocalDate.now().plusDays(2), "Haute", "Planifiée");

                TaskAssignmentService service = new TaskAssignmentService(env.members, env.tasks);
                AssignmentResult result = service.reassignUrgentTask(urgentTask);

                System.out.println(result);
        }

        /**
         * Teste l'équilibre des charges
         */
        private static void testLoadBalance(TestEnvironment env) {
                System.out.println("\n⚖ Analyse de l'équilibre des charges...\n");

                double totalLoad = 0;
                for (Member member : env.members) {
                        totalLoad += member.getCurrentLoad();
                        System.out.println("  " + member.getFirstName() + ": " + member.getCurrentLoad() + "h");
                }

                double average = totalLoad / env.members.size();
                System.out.println("\n  Charge moyenne: " + String.format("%.2f", average) + "h");

                // Calculer l'écart-type
                double variance = 0;
                for (Member member : env.members) {
                        variance += Math.pow(member.getCurrentLoad() - average, 2);
                }
                variance /= env.members.size();
                double stdDev = Math.sqrt(variance);

                System.out.println("  Écart-type: " + String.format("%.2f", stdDev) + "h");

                if (stdDev < 20) {
                        System.out.println("\n  ✓ Charge bien équilibrée!");
                } else if (stdDev < 40) {
                        System.out.println("\n  ⚠ Équilibre acceptable");
                } else {
                        System.out.println("\n  ✗ Déséquilibre important détecté!");
                }
        }

        /**
         * Teste la génération du rapport
         */
        private static void testReportGeneration(TestEnvironment env) {
                TaskAssignmentService service = new TaskAssignmentService(env.members, env.tasks);
                String report = service.generateAssignmentReport();

                System.out.println("\n" + report);
        }

        /**
         * Classe helper pour regrouper l'environnement de test
         */
        static class TestEnvironment {
                List<Member> members = new ArrayList<>();
                List<Task> tasks = new ArrayList<>();
        }
}
