package ru.job4j.cinema.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The class implements Store and uses JDBC for the DB accessing.
 *
 * @author AndrewMs
 * @version 1.1
 */

public class PsqlStore implements Store {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        URL res = getClass().getClassLoader().getResource("db.properties");
        try (BufferedReader io = new BufferedReader(
                new FileReader(Paths.get(res.toURI()).toFile()));
        ) {
            cfg.load(io);
        } catch (Exception e) {
            LOG.error("An exception occurred: ", e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            LOG.error("Exception occurred: ", e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    /**
     *
     * @param account Account object
     * @return Optional<Account> with empty value if the account already exists in the db
     * or with Account object saved in the db.
     */
    @Override
    public Optional<Account> save(Account account) {
        Optional<Account> rsl = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(
                     "INSERT INTO account(username, email, phone) VALUES (?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    account.setId(id.getInt(1));
                    rsl = Optional.of(account);
                }
            }
        } catch (SQLException e) {
            LOG.error("An exception occurred: ", e);
        }
        return rsl;
    }

    /**
     * If the ticket's account already presents in the db the method
     * tries to find existed account using the "phone" field.
     *
     * @param ticket Ticket object to save in the db.
     * @return Optional<Ticket> with empty value null if the ticket already presents
     * in the db (unique sessionId, row, seat) or Ticket object saved in the db.
     */
    @Override
    public Optional<Ticket> save(Ticket ticket) {
        Optional<Ticket> rsl = Optional.empty();
        Account account =  save(ticket.getAccount())
                                .orElseGet(() -> findAccountByPhone(ticket.getAccount().getPhone())
                                    .orElse(null));
        if (account == null) {
            return rsl;
        }
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(
                     "INSERT INTO ticket(session_id, row, cell, account_id) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, ticket.getSessionId());
            ps.setInt(2, ticket.getRow());
            ps.setInt(3, ticket.getCell());
            ps.setInt(4, account.getId());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    ticket.setId(id.getInt(1));
                    ticket.setAccount(account);
                    rsl = Optional.of(ticket);
                }
            }
        } catch (Exception e) {
            LOG.error("An exception occurred: ", e);
        }
        return rsl;
    }

    @Override
    public Collection<Ticket> findAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM ticket");
        ) {
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    tickets.add(new Ticket(
                                    result.getInt("id"),
                                    result.getInt("session_id"),
                                    result.getInt("row"),
                                    result.getInt("cell"),
                                    findAccountById(result.getInt("account_id")).orElse(null)
                            )
                    );
                }
            }
        } catch (Exception e) {
            LOG.error("An exception occurred: ", e);
        }
        return tickets;
    }

    private Optional<Account> findAccountByPhone(String phone) {
        Optional<Account> rsl = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT * FROM account WHERE phone=?");
        ) {
            ps.setString(1, phone);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    rsl = Optional.of(new Account(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("email"),
                            result.getString("phone")
                    ));
                }
            }
        } catch (Exception e) {
            LOG.error("An exception occurred: ", e);
        }
        return rsl;
    }

    private Optional<Account> findAccountById(int id) {
        Optional<Account> rsl = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM account WHERE id=?");
        ) {
            ps.setInt(1, id);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    rsl = Optional.of(
                            new Account(
                                    result.getInt("id"),
                                    result.getString("username"),
                                    result.getString("email"),
                                    result.getString("phone")
                            )
                    );
                }
            }
        } catch (Exception e) {
            LOG.error("An exception occurred: ", e);
        }
        return rsl;
    }
}
