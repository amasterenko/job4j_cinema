package ru.job4j.cinema.store;

import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;

import java.util.Collection;
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

    Account save(Account account);

    Ticket save(Ticket ticket);

    Collection<Ticket> findAllTickets();
}
