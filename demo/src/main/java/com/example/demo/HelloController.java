package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import com.example.demo.BancomatJDBC;
import javafx.scene.layout.VBox;

import java.sql.*;

public class HelloController {

    private BancomatJDBC bancomatJDBC = new BancomatJDBC();

    @FXML
    private TextArea resultArea;

    @FXML
    private TextField cardNumberField;

    @FXML
    private VBox additionalButtons;

    @FXML
    public void initialize() {
        bancomatJDBC.init();
    }

    @FXML
    protected  void onExitMenuClick() {
        System.exit(0);
    }

    @FXML
    protected void onAboutMenuClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Despre");
        alert.setHeaderText("Bancomat");
        alert.setContentText("Aceasta aplicatie interactioneaza cu baza de date Bancomat pentru a execute diverse interogari.");
        alert.showAndWait();
    }

    @FXML
    protected void onCase5ButtonClick() {
        String query = "SELECT * FROM Carduri WHERE MONTH(data_la)=11 AND YEAR(data_la) = 2024 ORDER BY data_la ASC;";
        displayQueryResults(query);
    }

    @FXML
    protected void onCase6ButtonClick() {
        String query = "SELECT * FROM Miscari WHERE scop LIKE '%alimentare%' ORDER BY valoare ASC, scop DESC;";;
        displayQueryResults(query);
    }

    @FXML
    protected void onCase7ButtonClick() {
        String query = "SELECT P.nume, C.* FROM Persoane P JOIN Conturi C ON P.idpers=C.idpers JOIN Carduri CA ON C.nrcont=CA.nrcont WHERE CA.tip = 'MASTERCARD' AND C.sold<0;";;
        displayQueryResults(query);
    }

    @FXML
    protected void onCase8ButtonClick() {
        String query = "SELECT * FROM Conturi C1 JOIN Conturi C2 ON C1.sold = C2.sold AND C1.idpers<C2.idpers;";
        displayQueryResults(query);
    }

    @FXML
    protected void onCase9ButtonClick() {
        String query = "SELECT P.nume FROM Persoane P WHERE EXISTS (SELECT 1 FROM Carduri C WHERE C.limita = (SELECT MAX(limita) FROM Carduri) AND C.nrcont IN (SELECT nrcont FROM Conturi WHERE idpers = P.idpers));";
        displayQueryResults(query);
    }

    @FXML
    protected void onSearchCardMovementsButtonClick() {
        String cardNumber = cardNumberField.getText();
        if(cardNumber.isEmpty()){
            resultArea.setText("Introduceți un număr de card în formatul xxxx-xxxx-xxxx-xxxx");
            return;
        }

        String query="SELECT DISTINCT M2.nrcard, M2.data_ora, M2.valoare, M2.scop " +
                     "FROM Miscari M1 "+
                     "JOIN Miscari M2 ON M1.data_ora=M2.data_ora "+
                     "WHERE M1.nrcard = '"+cardNumber+"'" +
                     "AND M1.nrcard != M2.nrcard " +
                     "ORDER BY M2.data_ora;";
        displayQueryResults(query);
    }

    @FXML
    protected void onCase10ButtonClick() {
        String cardNumber = cardNumberField.getText();
        if(cardNumber.isEmpty()){
            resultArea.setText("Introduceți un număr de card în formatul xxxx-xxxx-xxxx-xxxx");
            return;
        }
        String query = "SELECT DISTINCT M1.* " +
                "FROM Miscari M1 "+
                "WHERE EXISTS ( "+
                "  SELECT 1 FROM Miscari M2 " +
                "  WHERE M2.nrcard = '"+cardNumber+"'" +
                "  AND M1.data_ora=M2.data_ora " +
                "  AND M1.nrcard != M2.nrcard " +
                ") " +
                "ORDER BY M1.data_ora;";;
        displayQueryResults(query);

        additionalButtons.setVisible(true);
    }

    @FXML
    protected void onCase11ButtonClick() {
        String query = "SELECT P.gen, COUNT(*) AS numar_carduri FROM Persoane P JOIN Conturi C ON P.idpers = C.idpers JOIN Carduri CA ON C.nrcont = CA.nrcont WHERE CA.data_la >= CURRENT_DATE GROUP BY P.gen;";
        displayQueryResults(query);
    }

    @FXML
    protected void onCase12ButtonClick() {
        String query = "SELECT CA.categorie, MIN(M.valoare) AS min_valoare, AVG(M.valoare) AS medie_valoare, MAX(M.valoare) AS max_valoare " +
                "FROM Carduri CA JOIN Miscari M ON CA.nrcard = M.nrcard " +
                "WHERE M.data_ora BETWEEN '2024-01-01' AND '2024-03-31' " +
                "GROUP BY CA.categorie;";
        displayQueryResults(query);
    }

    @FXML
    protected void onCase13ButtonClick() {
        StringBuilder rez = new StringBuilder();
        String query = "{CALL GetCarduriNoiembrie2024()}";

        try (Connection con = DriverManager.getConnection(bancomatJDBC.url, bancomatJDBC.uid, bancomatJDBC.pw);
             CallableStatement stmt = con.prepareCall(query)) {

            try(ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCnt = rsmd.getColumnCount();

                for(int i=1;i<=colCnt;i++){
                    rez.append(rsmd.getColumnName(i)).append("\t");
                }
                rez.append("\n");

                while(rs.next()){
                    for(int i=1;i<=colCnt;i++){
                        rez.append(rs.getString(i)).append("\t");
                    }
                    rez.append("\n");
                }
            }
        }catch (Exception e) {
            rez.append("SQLException: ").append(e.getMessage());
        }
        resultArea.setText(rez.toString());
    }

    private void displayQueryResults(String query){
        String rez = bancomatJDBC.getQueryResults(query);
        resultArea.setText(rez);
    }

}