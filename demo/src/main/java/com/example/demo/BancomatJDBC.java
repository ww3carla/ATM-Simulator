package com.example.demo;

import java.sql.*;
import java.io.*;

public class BancomatJDBC {
    public String url = "jdbc:mysql://localhost:3306/Bancomat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public String uid = "uid";
    public String pw = "PW";
    private BufferedReader reader;
    private Connection con;

    public static void main(String[] args) {
        BancomatJDBC app = new BancomatJDBC();
        app.init();
        app.run();
    }

    public void init() {
        // Înregistrează driverul MySQL și realizează conexiunea
        try {
            // Încarcă driverul MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e);
        }

        // Inițializează conexiunea
        con = null;
        try {
            con = DriverManager.getConnection(url, uid, pw);
            System.out.println("Conexiune reușită la baza de date!");
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex);
            System.exit(1);
        }

        // Setează reader-ul pentru input
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public String getQueryResults(String queryStr) {
        StringBuilder resultBuilder = new StringBuilder();

        try (Statement stmt = con.createStatement();
             ResultSet rst = stmt.executeQuery(queryStr)) {
            ResultSetMetaData rsmd = rst.getMetaData();
            int colCount = rsmd.getColumnCount();

            // Adaugă header-ul
            for (int i = 1; i <= colCount; i++) {
                resultBuilder.append(rsmd.getColumnName(i)).append("\t");
            }
            resultBuilder.append("\n");

            // Adaugă fiecare rând de rezultate
            while (rst.next()) {
                for (int i = 1; i <= colCount; i++) {
                    resultBuilder.append(rst.getString(i)).append("\t");
                }
                resultBuilder.append("\n");
            }
        } catch (SQLException ex) {
            resultBuilder.append("SQLException: ").append(ex);
        }

        return resultBuilder.toString();  // Returnează rezultatele ca un String
    }

    public void caLLGetCarduriNoiembrie2024(){
        String query="{CALL GetCarduriNoiembrie2024()}";

        try(CallableStatement stmt = con.prepareCall(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCnt = rsmd.getColumnCount();

                for (int i = 1; i <= colCnt; i++) {
                    System.out.println(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= colCnt; i++) {
                        System.out.println(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            }
        }catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
    }

    private void run() {
        String choice, sqlSt;
        choice = "1";
        while (!choice.equalsIgnoreCase("X")) {
            printMenu();
            choice = getLine();

            switch (choice) {
                case "1": // List all persons
                    sqlSt = "SELECT * FROM Persoane;";
                    doQuery(sqlSt);
                    break;
                case "S": // List all persons
                    System.out.println("Enter person id (between 1 and infinite number):");
                    String pid = getLine();
                    sqlSt = "SELECT * FROM Conturi WHERE idpers = "+ pid +";";
                    doQuery(sqlSt);
                    break;
                case "2": // List all accounts for a person
                    System.out.print("Enter card number: ");
                    String persid = getLine();
                    sqlSt = "SELECT * FROM C WHERE idpers = '" + persid + "' ;";
                    System.out.println(sqlSt);
                    doQuery(sqlSt);
                    break;
                case "3": // List all transactions for a card
                    System.out.print("Enter card number: ");
                    String cardNumber = getLine();
                    sqlSt = "SELECT * FROM Miscari WHERE nrcard = '" + cardNumber + "';";
                    doQuery(sqlSt);
                    break;
                case "4": // List all cards for a person
                    System.out.print("Enter person id: ");
                    String personid = getLine();
                    sqlSt = "SELECT * FROM Carduri WHERE nrcont IN (SELECT nrcont FROM Conturi WHERE idpers = '" + personid + "');";
                    doQuery(sqlSt);
                    break;
                case "5"://query card details november 2024
                    sqlSt = "SELECT * FROM Carduri WHERE MONTH(data_la)=11 AND YEAR(data_la) = 2024 ORDER BY data_la ASC;";
                    doQuery(sqlSt);
                    break;
                case "6"://query movements containing alimentare
                    sqlSt = "SELECT * FROM Miscari WHERE scop LIKE '%alimentare%' ORDER BY valoare ASC, scop DESC;";
                    doQuery(sqlSt);
                    break;
                case "7": //query master card negative balance
                    sqlSt = "SELECT P.nume, C.* FROM Persoane P JOIN Conturi C ON P.idpers=C.idpers JOIN Carduri CA ON C.nrcont=CA.nrcont WHERE CA.tip = 'MASTERCARD' AND C.sold<0;";
                    doQuery(sqlSt);
                    break;
                case "8"://querry accounts with same balance
                    sqlSt = "SELECT C1.idpers, C2.idpers FROM Conturi C1 JOIN Conturi C2 ON C1.sold = C2.sold AND C1.idpers<C2.idpers;";
                    doQuery(sqlSt);
                    break;
                case "9"://querry persons with max limit card
                    sqlSt = "SELECT P.nume FROM Persoane P WHERE EXISTS (SELECT 1 FROM Carduri C WHERE C.limita = (SELECT MAX(limita) FROM Carduri) AND C.nrcont IN (SELECT nrcont FROM Conturi WHERE idpers = P.idpers));";
                    doQuery(sqlSt);
                    break;
                case "10"://query cards with same movement date
                    System.out.print("Enter card number: ");
                    String cardnumber = getLine();
                    sqlSt = "SELECT DISTINCT nrcard FROM Miscari M1 WHERE EXISTS (SELECT 1 FROM Miscari M2 WHERE M2.nrcard = '" + cardnumber + "' AND M1.data_ora = M2.data_ora AND M1.nrcard != M2.nrcard);";
                    doQuery(sqlSt);
                    break;
                case "11"://count valid cards by gender
                    sqlSt = "SELECT P.gen, COUNT(*) AS numar_carduri FROM Persoane P JOIN Conturi C ON P.idpers = C.idpers JOIN Carduri CA ON C.nrcont = CA.nrcont WHERE CA.data_la >= CURRENT_DATE GROUP BY P.gen;";
                    doQuery(sqlSt);
                    break;
                case "12"://query spending stats by category
                    sqlSt = "SELECT CA.categorie, MIN(M.valoare) AS min_valoare, AVG(M.valoare) AS medie_valoare, MAX(M.valoare) AS max_valoare " +
                            "FROM Carduri CA JOIN Miscari M ON CA.nrcard = M.nrcard " +
                            "WHERE M.data_ora BETWEEN '2024-01-01' AND '2024-03-31' " +
                            "GROUP BY CA.categorie;";
                    doQuery(sqlSt);
                    break;
                case "13":
                    caLLGetCarduriNoiembrie2024();
                    break;
                case "A": // Add person
                    addPerson();
                    break;
                case "D": // Delete person
                    deletePerson();
                    break;
                case "U": // Update person
                    updatePerson();
                    break;
                case "X":
                    System.out.println("Exiting!");
                    closeConnection();
                    return;
                default:
                    System.out.println("Invalid input!");
                    break;
            }
        }
    }

    private void addPerson() {
        try {
            System.out.print("Enter person's id: ");
            String pid = getLine();
            System.out.print("Enter person's name: ");
            String pname = getLine();
            pname = convertSQLString(pname);

            System.out.print("Enter person's address: ");
            String address = getLine();
            address = convertSQLString(address);

            System.out.print("Enter person's gender: ");
            String gender = getLine();

            System.out.print("Enter person's birthdate (YYY-MM-DD): ");
            String birth = getLine();

            String sqlSt = "INSERT INTO Persoane (idpers, nume, adresa, gen, data_nasterii) VALUES ('" + pid + "', '" + pname + "', '" + address + "', '" + gender + "', '" + birth + "');";
            doUpdate(sqlSt);
        } catch (Exception e) {
            System.out.println("Failed to add person: " + e);
        }
    }
    private void updatePerson() {
        try {
            System.out.print("Enter person's id: ");
            String pid = getLine();
            System.out.print("Enter new person's name: ");
            String pname = getLine();
            pname = convertSQLString(pname);

            String sqlSt = "UPDATE Persoane set nume  = '"+pname+"' where idpers ="+ pid + ";";
            doUpdate(sqlSt);
        } catch (Exception e) {
            System.out.println("Failed to update person " + e);
        }
    }

    private void deletePerson() {
        System.out.print("Enter person's id to delete: ");
        String pid = getLine();
        String sqlSt = "DELETE FROM Persoane WHERE idpers = '" + pid + "';";
        doUpdate(sqlSt);
    }

    private void doUpdate(String updateStr) {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(updateStr);
            System.out.println("Operation successful!");
        } catch (SQLException e) {
            System.out.println("Operation failed: " + e);
        }
    }

    public void doQuery(String queryStr) {
        try (Statement stmt = con.createStatement();
             ResultSet rst = stmt.executeQuery(queryStr)) {
            ResultSetMetaData rsmd = rst.getMetaData();
            int colCount = rsmd.getColumnCount();

            // Print header
            for (int i = 1; i <= colCount; i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();

            // Print rows
            while (rst.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(rst.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex);
        }
    }

    private String getLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
            return null;
        }
    }

    private String convertSQLString(String st) {
        return st.replaceAll("'", "''");
    }

    private void printMenu() {
        System.out.println("\n\nSelect one of these options: ");
        System.out.println("  1 - List all persons");
        System.out.println("  2 - List all accounts for a person");
        System.out.println("  3 - List all transactions for a card");
        System.out.println("  4 - List all cards for a person");
        System.out.println("  5 - Query card details expiring in November 2024");
        System.out.println("  6 - Query movements containing 'alimentare' ");
        System.out.println("  7 - Query Mastercard holders with negative balance");
        System.out.println("  8 - Query accounts with same balance");
        System.out.println("  9 - Query persons with cards having maximum limit");
        System.out.println("  10 - Query cards with movements on the same date as specific card");
        System.out.println("  11 - Count valid cards by gender");
        System.out.println("  12 - Spending statistics by category for the first trimester of 2024");
        System.out.println("  13 - Query cards expiring in November 2024 using procedure");
        System.out.println("  A - Add a person");
        System.out.println("  D - Delete a person");
        System.out.println("  X - Exit application");
        System.out.print("Your choice: ");
    }

    private void closeConnection() {
        try {
            if (con != null) {
                con.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException ex) {
            System.err.println("Exception during connection close: " + ex);
        }
    }
}

