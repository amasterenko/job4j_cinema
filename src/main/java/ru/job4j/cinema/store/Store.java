package ru.job4j.cinema.store;

import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.util.Collection;
import java.util.Optional;

/**
 * The interface represents a store for managing accounts and tickets.
 *
 * @author AndrewMs
 * @version 1.0
 */
public interface Store {

    static Store instOf() {
        return null;
    }

    Optional<Account> save(Account account);

    Optional<Ticket> save(Ticket ticket);

    Collection<Ticket> findAllTickets();
}
