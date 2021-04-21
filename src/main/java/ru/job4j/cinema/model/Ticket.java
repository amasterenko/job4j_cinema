package ru.job4j.cinema.model;
/**
 * The class represents a ticket
 * @author AndrewMs
 * @version 1.0
 */
public class Ticket {
    private int id;
    private int sessionId;
    private int row;
    private int cell;
    private Account account;

    public Ticket(int id, int sessionId, int row, int cell, Account account) {
        this.id = id;
        this.sessionId = sessionId;
        this.row = row;
        this.cell = cell;
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getRow() {
        return row;
    }

    public int getCell() {
        return cell;
    }

    public Account getAccount() {
        return account;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCell(int cell) {
        this.cell = cell;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
