/*
 * Copyright (C) 2021 RCM.
 * All rights reserved. This software is the confidential and proprietary information of RCM.
 * You shall not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement you entered into with RCM.
 */

package pl.comp.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.comp.exceptions.DaoException;
import pl.comp.exceptions.FailedFileOperationException;

public class FileSudokuBoardDao implements Dao<SudokuBoard>, AutoCloseable {

    private final String filename;
    private static final Logger logger = LoggerFactory.getLogger(FileSudokuBoardDao.class);

    public FileSudokuBoardDao(String filename) {
        this.filename = filename;
    }

    public SudokuBoard read() throws DaoException {
        SudokuBoard sudokuBoard = null;

        try (var fileInputStream = new FileInputStream(filename);
             var objectInputStream = new ObjectInputStream(fileInputStream)) {
            sudokuBoard = (SudokuBoard) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            DaoException exception = new FailedFileOperationException("File operation failed", e);
            logger.error(exception + "\nCaused by", exception.getCause());
            throw exception;
        }
        return sudokuBoard;
    }

    public void write(SudokuBoard obj) throws DaoException {
        try (var fileOutputStream = new FileOutputStream(filename);
             var objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(obj);
        } catch (IOException e) {
            DaoException exception = new FailedFileOperationException("File operation failed", e);
            logger.error(exception + "\nCaused by", exception.getCause());
            throw exception;
        }
    }

    @Override
    public void close() {
        logger.info("FileSudokuBoardDao closed!");
    }

}
