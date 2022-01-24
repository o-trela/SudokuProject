package pl.comp.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import org.javatuples.Pair;

import java.sql.*;

public class JdbcSudokuBoardDao implements Dao<SudokuBoard>{

    final int SAVED_BOARDS_COUNT = 5;
    final String DB_NAME = "sudoku_boards.db";
    final String DB_PATH = FilesManager.getPath(DB_NAME);
    final String CONNECTION_URL = "jdbc:sqlite:" + DB_PATH;

    private static final Logger logger = LoggerFactory.getLogger(JdbcSudokuBoardDao.class);

    private boolean initialized = false;

    @Override
    public SudokuBoard read() {
        return null;
    }

    public Pair<SudokuBoard, SudokuBoard> readBoth(int index) {
        var board = new SudokuBoard();
        var originalBoard = new SudokuBoard();

        String queues = "SELECT board, originalBoard FROM Boards WHERE id = "
                + index + ";";

        try (Connection conn = DriverManager.getConnection(CONNECTION_URL)) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(queues);

            String boardFields = rs.getString("board");
            String originalBoardFields = rs.getString("originalBoard");
            for (int i = 0; i < 81; i++) {
                board.set(i / 9, i % 9,
                        Character.getNumericValue(boardFields.charAt(i)));
                originalBoard.set(i / 9, i % 9,
                        Character.getNumericValue(originalBoardFields.charAt(i)));
            }

            logger.info("SudokuBoards read.");

        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }

        return Pair.with(board, originalBoard);
    }

    @Override
    public void write(SudokuBoard obj) {
        String queues = "INSERT INTO Boards(name, board) VALUES(?,?)";

        var boardFields = new StringBuilder(81);

        for (int i = 0; i < 81; i++) {
            boardFields.append(obj.getField(i / 9, i % 9).getFieldValue());
        }

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(queues);
            pstmt.setString(1, "SudokuBoard(" + LocalDateTime.now() + ")");
            pstmt.setString(2, boardFields.toString());
            pstmt.executeUpdate();
            logger.info("Record added.");

        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    public void write(SudokuBoard modified, SudokuBoard original) {
        String queues = "INSERT INTO Boards(name, board, originalBoard) VALUES(?,?,?)";

        var boardFields = new StringBuilder(81);
        var originalBoardFields = new StringBuilder(81);

        for (int i = 0; i < 81; i++) {
            boardFields.append(modified.getField(i / 9, i % 9).getFieldValue());
            originalBoardFields.append(original.getField(i / 9, i % 9).getFieldValue());
        }

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(queues);
            pstmt.setString(1, "SudokuBoard(" + LocalDateTime.now() + ")");
            pstmt.setString(2, boardFields.toString());
            pstmt.setString(3, originalBoardFields.toString());
            pstmt.executeUpdate();
            logger.info("Record added.");

        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    public void updateBoard(int index, SudokuBoard modified, SudokuBoard original) throws IllegalArgumentException {
        if (indexOutOfRange(index)) throw new IllegalArgumentException("pupa hihihi"); //TODO
        
        String queues = "UPDATE Boards SET name = ?, board = ?, originalBoard = ? WHERE id = ?";

        var boardFields = new StringBuilder(81);
        var originalBoardFields = new StringBuilder(81);

        for (int i = 0; i < 81; i++) {
            boardFields.append(modified.getField(i / 9, i % 9).getFieldValue());
            originalBoardFields.append(original.getField(i / 9, i % 9).getFieldValue());
        }

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            PreparedStatement pstmt = conn.prepareStatement(queues);
            pstmt.setString(1, "SB(" + LocalDateTime.now() + ")");
            pstmt.setString(2, boardFields.toString());
            pstmt.setString(3, originalBoardFields.toString());
            pstmt.setInt(4, index);
            pstmt.executeUpdate();
            logger.info("Record modified.");

        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    public void selectAll(){
        String queues = "SELECT * FROM Boards";

        try (var conn = DriverManager.getConnection(CONNECTION_URL)){
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(queues);

            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" +
                        rs.getString("name") + "\t" +
                        rs.getString("board"));
            }
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void initialize() {
        if (initialized) return;
        this.ensureTableValidity();
        initialized = true;
    }

    public boolean isRecordEmpty(int index) {
        return getName(index).equals("empty");
    }

    private String getName(int index) {
        if (indexOutOfRange(index)) throw new IllegalArgumentException("pupa hihihi");

        String queues = """ 
                            SELECT *
                            FROM Boards
                            WHERE id = ?;
                        """;

        try (var conn = DriverManager.getConnection(CONNECTION_URL)){
            PreparedStatement pstmt = conn.prepareStatement(queues);
            pstmt.setInt(1, index);
            ResultSet rs = pstmt.executeQuery();

            return rs.getString("name");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }

        return null;
    }

    private void ensureTableValidity() {

        if (checkTableExists()) {
            if (checkTableValidity()) {
                logger.info("Table exists and is valid");
            }
            else {
                logger.info("Table exists but is not valid!\nCreating new table and filling with defaults");
                dropTable();
                createTable();
                fillTableWithDefaults();
            }
        }
        else {
            logger.info("Table deos not exist!\nCreating new table and filling with defaults");
            createTable();
            fillTableWithDefaults();
        }
    }

    private boolean checkTableValidity() {
        String queues = "SELECT * FROM Boards";

        try (var conn = DriverManager.getConnection(CONNECTION_URL)){
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(queues);

            int count = 0;
            while (rs.next()) {
                int index = rs.getInt("id");
                if (indexOutOfRange(index)) {
                    return false;
                }
                count++;
            }
            if (count != 5) {
                return false;
            }
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private boolean checkTableExists() {
        String queues = "SELECT * FROM Boards";

        try (var conn = DriverManager.getConnection(CONNECTION_URL)){
            Statement statement = conn.createStatement();
            statement.executeQuery(queues);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void fillTableWithDefaults() {
        String queues = """
                INSERT INTO Boards VALUES
                    (0, "empty", "", ""),
                    (1, "empty", "", ""),
                    (2, "empty", "", ""),
                    (3, "empty", "", ""),
                    (4, "empty", "", "");
                """;

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            Statement statement = conn.createStatement();
            statement.execute(queues);
            logger.info("Filled Table");

        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    private void createTable() {
        String queues = """
                CREATE TABLE Boards (
                     id integer PRIMARY KEY,
                     name text NOT NULL,
                     board text,
                     originalBoard text
                );
                """;

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            Statement statement = conn.createStatement();
            statement.execute(queues);
            logger.info("Table created");
        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    private void dropTable() {
        String queues = """
                DROP TABLE Boards
                """;

        try (var conn = DriverManager.getConnection(CONNECTION_URL)) {
            Statement statement = conn.createStatement();
            statement.execute(queues);
            logger.info("Table Dropped");
        } catch (SQLException exception) {
            logger.error(exception.getLocalizedMessage());
        }
    }

    private boolean indexOutOfRange(int index) {
        return (index < 0 || index >= SAVED_BOARDS_COUNT);
    }
}





