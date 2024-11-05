package codelets.learner;

import br.unicamp.cst.learning.QLearning;
import java.sql.*;
import java.util.*;
import org.sqlite.SQLiteErrorCode;

public class QLearningSQL extends QLearning {
    private List<QUpdate> pendingUpdates = new ArrayList<>();
    private final int BATCH_SIZE = 10000;
    int maxRowsPerChunk = 300000;
    private boolean showDebugMessages = false;
    private LinkedHashMap<Integer, HashMap<String, Double>> Q = new LinkedHashMap<Integer, HashMap<String, Double>>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 1000; // Limit cache size to prevent memory overload
        }
    };
    private String fileName;
    private ArrayList<String> actionsList;
    private double alpha = 0.5;
    private double gamma = 0.9;
    private String a = "", al = "";
    private int s = -1, sl = -1;
    private double reward = 0;
    private Random r = new Random();
    private final double UPDATE_THRESHOLD = 1e-6; // Minimal update threshold to prevent unnecessary writes

    // Inner class for storing pending updates
    private static class QUpdate {
        int state;
        String action;
        double qValue;

        QUpdate(int state, String action, double qValue) {
            this.state = state;
            this.action = action;
        }
    }

    // Constructor
    public QLearningSQL(String fileName, ArrayList<String> allActionsList) {
        super();
        this.fileName = fileName;
        this.actionsList = allActionsList;
        createTable();
        enableWALMode();
        initializeQTable();
    }

    
    public void setFilename(String file) {
        this.fileName = file;

    }
    
    /**
     *
     */
    @Override
    public void recoverQ() {
        System.out.println("Recovering Q-table from " + this.fileName);
        String querySQL = "SELECT state, action, value FROM qtable";
        int maxRowsPerChunk = this.maxRowsPerChunk;  // Adjust this value based on memory and performance needs

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {

            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                String chunkedQuerySQL = querySQL + " LIMIT " + maxRowsPerChunk + " OFFSET " + offset;
                try (ResultSet rs = stmt.executeQuery(chunkedQuerySQL)) {
                    hasMoreData = false;  // Assume no more data unless rows are found

                    // Store rows in Q cache without calling setQ to avoid adding to pendingUpdates
                    while (rs.next()) {
                        int state = rs.getInt("state");
                        String action = rs.getString("action");
                        double value = rs.getDouble("value");

                        Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, value);
                        hasMoreData = true;  // More data exists if at least one row is found
                    }

                    offset += maxRowsPerChunk;  // Move to the next chunk
                }
            }

        } catch (SQLException e) {
            System.out.println("Error recovering Q-table: " + e.getMessage());
        }
    }


    // Database connection method
    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + this.fileName;
        //System.out.println(url);
        return DriverManager.getConnection(url);
    }

    // Create the Q-table in the database if it doesn't exist
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS qtable ("
                   + "state INTEGER NOT NULL, action TEXT NOT NULL, value REAL NOT NULL,"
                   + "PRIMARY KEY (state, action));";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println(e.getMessage());
        }
    }

        // Optimized initialization method with progress tracking
        private void initializeQTable() {
        int totalRows = getTotalRowCount();
        if (totalRows == 0) return;  // Exit early if no data to load

        String querySQL = "SELECT state, action, value FROM qtable";
        int rowsProcessed = 0;
        int lastProgress = -1;  // Start at -1 to ensure progress is printed at 0%

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {

            while (rs.next()) {
                int state = rs.getInt("state");
                String action = rs.getString("action");
                double value = rs.getDouble("value");

                // Directly add to cache to reduce redundant calls
                Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, value);
                rowsProcessed++;

                // Calculate progress as an integer percentage
                int progress = (int) ((rowsProcessed / (double) totalRows) * 100);
                if (progress >= lastProgress + 10) {  // Update every 10%
                    System.out.println(this.fileName + " initialization progress: " + progress + "%");
                    lastProgress = progress;
                }
            }

            // Ensure a final print at 100% if not already done
            if (lastProgress < 100) {
                System.out.println(this.fileName + " initialization progress: 100%");
            }

        } catch (SQLException e) {
            System.out.println("Error during Q-table initialization: " + e.getMessage());
        }
    }



        public void initializeQTableBulk(List<String> allActionsList, int num_tables, int batteryMax, int salMax, double[] dsValues, double[] dcValues) {
        int totalStates = num_tables == 1 ? batteryMax * dsValues.length * dcValues.length * salMax : batteryMax * dsValues.length * salMax;
        int processedStates = 0;
        int lastProgress = 0;

        String insertSQL = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, 0)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            conn.setAutoCommit(false); // Batch insertion transaction

            for (int batteryV = 0; batteryV < batteryMax; batteryV++) {
                if (num_tables == 1) {
                    for (double dc : dcValues) {
                        int dcIndex = (int) (dc * 5);
                        for (double ds : dsValues) {
                            int dsIndex = (int) (ds * 5);
                            for (int sal = 0; sal < salMax; sal++) {
                                int stateIndex = (batteryV * 6 * 6 * 65536) + (dcIndex * 6 * 65536) + (dsIndex * 65536) + sal;
                                for (String action : allActionsList) {
                                    pstmt.setInt(1, stateIndex);
                                    pstmt.setString(2, action);
                                    pstmt.addBatch();
                                }
                                processedStates++;
                                // Track progress every 10% completion
                                int progress = (processedStates * 100) / totalStates;
                                if (progress >= lastProgress + 10) {
                                    System.out.println(this.fileName+" bulk initialization progress: " + progress + "%");
                                    lastProgress = progress;
                                }
                            }
                        }
                    }
                } else {
                    for (double ds : dsValues) {
                        int dsIndex = (int) (ds * 5);
                        for (int sal = 0; sal < salMax; sal++) {
                            int stateIndex = (batteryV * 65536) + (dsIndex * 65536) + sal;
                            for (String action : allActionsList) {
                                pstmt.setInt(1, stateIndex);
                                pstmt.setString(2, action);
                                pstmt.addBatch();
                            }
                            processedStates++;
                            int progress = (processedStates * 100) / totalStates;
                            if (progress >= lastProgress + 10) {
                                System.out.println(this.fileName+" bulk initialization progress: " + progress + "%");
                                lastProgress = progress;
                            }
                        }
                    }
                }
                pstmt.executeBatch(); // Commit the current batch
                conn.commit();
            }
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println("Error during Q-table bulk initialization: " + e.getMessage());
        }
    }


    // Helper method to get the total row count in Qtable
    private int getTotalRowCount() {
        String countSQL = "SELECT COUNT(*) AS total FROM qtable";
        int totalRows = 0;
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            if (rs.next()) {
                totalRows = rs.getInt("total");
            }
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println("Error counting rows in "+this.fileName+  e.getMessage());
        }
        return totalRows;
    }

    public void setQ(double Qval, int state, String action) {
        // Check if the value has changed significantly before adding it to pending updates
        double currentQVal = getQ(state, action);
        if (Math.abs(currentQVal - Qval) >= UPDATE_THRESHOLD) {
            pendingUpdates.add(new QUpdate(state, action, Qval));
            Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, Qval);

            if (pendingUpdates.size() >= BATCH_SIZE) {
                commitQUpdates();
            }
        }
    }

    public void commitQUpdates() {
        if (pendingUpdates.isEmpty()) return;
        String sql = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, ?)";
        int retryCount = 0;

        while (retryCount < 3) {
            try (Connection conn = this.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);

                for (QUpdate update : pendingUpdates) {
                    pstmt.setInt(1, update.state);
                    pstmt.setString(2, update.action);
                    pstmt.setDouble(3, update.qValue);
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                pendingUpdates.clear();
                break;

            } catch (SQLException e) {
                if (e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code) {
                    retryCount++;
                    try { Thread.sleep((long) Math.pow(2, retryCount) * 50); } catch (InterruptedException ignored) {}
                } else {
                    if (showDebugMessages) System.out.println("Error committing Q-value updates: " + e.getMessage());
                    break;
                }
            }
        }
    }

    public double getQ(int state, String action) {
        if (Q.containsKey(state) && Q.get(state).containsKey(action)) {
            return Q.get(state).get(action);
        }

        double qValue = 0.0;
        String querySQL = "SELECT value FROM qtable WHERE state = ? AND action = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setInt(1, state);
            pstmt.setString(2, action);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    qValue = rs.getDouble("value");
                    Q.computeIfAbsent(state, k -> new HashMap<>()).put(action, qValue);
                }
            }
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println("Error retrieving Q-value: " + e.getMessage());
        }
        return qValue;
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

    // Find max Q-value for a given state
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

    public String getAction(int state) {
        String selectedAction = null;

        if (actionsList.isEmpty()) {
            System.out.println("Error: actionsList is empty. Please initialize actionsList with available actions.");
            return null;
        }

        double rd = r.nextDouble();
        if (showDebugMessages) System.out.println("rd:" + rd + " QLearning E: " + this.getE());

        if (rd <= this.getE()) {
            selectedAction = selectRandomAction(actionsList);
            if (showDebugMessages) System.out.println("Exploring: Selected a random action " + selectedAction);
            return selectedAction;
        }

        HashMap<String, Double> actionsQ = Q.get(state);

        if (actionsQ == null) {
            actionsQ = loadStateActionsFromDB(state);
            if (actionsQ != null) {
                Q.put(state, actionsQ);
            } else {
                actionsQ = new HashMap<>();
            }
        }

        selectedAction = selectRandomAction(actionsList);
        double bestQval = -Double.POSITIVE_INFINITY;

        for (String action : actionsList) {
            double qVal = actionsQ.getOrDefault(action, 0.0);
            if (qVal > bestQval) {
                bestQval = qVal;
                selectedAction = action;
            }
        }
        if (showDebugMessages) System.out.println("Exploiting: Selected best action based on Q-values: " + selectedAction);
        return selectedAction;
    }

    private HashMap<String, Double> loadStateActionsFromDB(int state) {
        HashMap<String, Double> actionsQ = new HashMap<>();
        String querySQL = "SELECT action, value FROM qtable WHERE state = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setInt(1, state);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String action = rs.getString("action");
                    double value = rs.getDouble("value");
                    actionsQ.put(action, value);
                }
            }
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println("Error loading state actions from database: " + e.getMessage());
        }

        return actionsQ;
    }

    private String selectRandomAction(ArrayList<String> localActionsList) {
        String actionR = this.a;
        double pseudoRandomNumber = r.nextDouble();
        if ((pseudoRandomNumber >= this.getB()) || actionR == null || actionR.equals("")) {
            int actionI = r.nextInt(localActionsList.size());
            actionR = localActionsList.get(actionI);
        }
        return actionR;
    }

    void enableWALMode() {
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            if (showDebugMessages) System.out.println("WAL mode enabled for SQLite.");
        } catch (SQLException e) {
            if (showDebugMessages) System.out.println("Failed to enable WAL mode: " + e.getMessage());
        }
    }
    
    public void storeQ() {
    String insertSQL = "INSERT OR REPLACE INTO qtable (state, action, value) VALUES (?, ?, ?)";
    int batchSize = 100000; // Set batch size limit
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

}
