package codelets.learner;

import br.unicamp.cst.learning.QLearning;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class QLearningSQL  extends QLearning{

    private boolean showDebugMessages = false;
    private ArrayList<Integer> statesList;
    private ArrayList<String> actionsList;
    private String fileName = "Qtable.db"; // SQLite database file
    private HashMap<Integer, HashMap<String, Double>> Q;

    private double e = 0.1; // Probability of choosing the best action instead of a random one
    private double alpha = 0.5; // Learning rate parameter
    private double gamma = 0.9; // Discount factor
    private double b = 0.95; // Probability of random action choice deciding for the previous action
    private String a = "", al = "";
    private int s = -1, sl = -1;
    private double reward = 0;
    private Random r = new Random();

    public QLearningSQL(String fileName) {
        statesList = new ArrayList<>();
        actionsList = new ArrayList<>();
        this.fileName = fileName;
        Q = new HashMap<>();
        createTable();
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + this.fileName;
        return DriverManager.getConnection(url);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS qtable ("
                   + "state INTEGER NOT NULL,"
                   + "action TEXT NOT NULL,"
                   + "value REAL NOT NULL,"
                   + "PRIMARY KEY (state, action)"
                   + ");";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setQ(double Qval, int state, String action) {
    // Limit the in-memory Q table size
    int maxStatesInMemory = 100000;  // Adjust this based on available memory

    if (Q.size() > maxStatesInMemory) {
        // Optionally write to a temporary file or clear older entries
        // For example, clear least-used entries
        Q.clear(); // This clears the map to release memory. Customize as needed.
    }

    HashMap<String, Double> tempS = this.Q.get(state);
    if (tempS != null) {
        tempS.put(action, Qval);
    } else {
        HashMap<String, Double> tempNew = new HashMap<>();
        tempNew.put(action, Qval);
        statesList.add(state);
        this.Q.put(state, tempNew);
    }
    if (!actionsList.contains(action)) {
        actionsList.add(action);
    }
}


    public double getQ(int state, String action) {
        return Q.getOrDefault(state, new HashMap<>()).getOrDefault(action, 0.0);
    }

    public double maxQsl(int sl) {
        double maxQinSl = 0;
        HashMap<String, Double> tempSl = Q.get(sl);
        if (tempSl != null) {
            for (double val : tempSl.values()) {
                maxQinSl = Math.max(maxQinSl, val);
            }
        }
        return maxQinSl;
    }

    public void update(int stateIWas, String actionIDid, double rewardIGot) {
        this.sl = stateIWas;
        this.al = actionIDid;
        if (!a.equals("") && s != -1) {
            double Qas = this.getQ(s, a);
            double MaxQ = this.maxQsl(this.sl);
            double newQ = Qas + alpha * (rewardIGot + gamma * MaxQ - Qas);
            this.setQ(newQ, s, a);
        }
        a = this.al;
        s = this.sl;
        reward = rewardIGot;
    }

    public void storeQ() {
    String insertSQL = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, ?)";
    int batchSize = 5000; // Set batch size limit
    int count = 0;

    try (Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
        
        conn.setAutoCommit(false); // Start transaction

        for (Map.Entry<Integer, HashMap<String, Double>> entry : Q.entrySet()) {
            int state = entry.getKey();
            HashMap<String, Double> actions = entry.getValue();
            
            for (Map.Entry<String, Double> actionEntry : actions.entrySet()) {
                String action = actionEntry.getKey();
                double value = actionEntry.getValue();
                
                pstmt.setInt(1, state);
                pstmt.setString(2, action);
                pstmt.setDouble(3, value);
                pstmt.addBatch();
                
                count++;

                // Execute and clear batch when reaching the batch size limit
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    pstmt.clearBatch(); // Clear batch after execution
                }
            }
        }

        // Execute remaining batch entries
        pstmt.executeBatch();
        conn.commit();

    } catch (SQLException e) {
        System.out.println("Error storing Q-table: " + e.getMessage());
    }
}


    

    public void recoverQ() {
    String querySQL = "SELECT state, action, value FROM qtable";
    int maxRowsPerChunk = 100000; // Adjust this based on memory availability

    try (Connection conn = this.connect();
         Statement stmt = conn.createStatement()) {

        int offset = 0;
        boolean hasMoreData = true;

        while (hasMoreData) {
            String chunkedQuerySQL = querySQL + " LIMIT " + maxRowsPerChunk + " OFFSET " + offset;
            try (ResultSet rs = stmt.executeQuery(chunkedQuerySQL)) {
                hasMoreData = false; // Assume no more data unless we find rows

                while (rs.next()) {
                    int state = rs.getInt("state");
                    String action = rs.getString("action");
                    double value = rs.getDouble("value");

                    this.setQ(value, state, action);
                    hasMoreData = true;
                }

                offset += maxRowsPerChunk; // Move to next chunk
            }
        }

    } catch (SQLException e) {
        System.out.println("Error recovering Q-table: " + e.getMessage());
    }
}


    public void printQ() {
        System.out.println("------ Printed Q -------");
        for (Map.Entry<Integer, HashMap<String, Double>> entry : Q.entrySet()) {
            int state = entry.getKey();
            HashMap<String, Double> actions = entry.getValue();
            System.out.print("State(" + state + ") actions: ");
            for (Map.Entry<String, Double> actionEntry : actions.entrySet()) {
                String action = actionEntry.getKey();
                double value = actionEntry.getValue();
                System.out.print("[" + action + ": " + value + "] ");
            }
            System.out.println();
        }
        System.out.println("----------------------------");
    }
    
    public void setFilename(String file){
        this.fileName = file;
        
    }
}
