package ru.job4j.cinema.servlets;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.model.Account;
import ru.job4j.cinema.model.Ticket;
import ru.job4j.cinema.store.PsqlStore;
import ru.job4j.cinema.store.Store;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * The servlet returns all the tickets as a json object using doGet.
 * The servlet receives POST-requests with a ticket data as a json object and
 * replies with a message|code.
 * If the seat with the specified session and row was already bought
 * the servlet replies with an error message|code.
 * If the message is successful, code 1 is used, on errors - 0.
 *
 * @author AndrewMs
 * @version 1.0
 *
 */
public class HallServlet extends HttpServlet {
    private static int rows;
    private static int cells;
    private static int sessionId;
    private static int refreshInterval;
    private static final Logger LOG = LoggerFactory.getLogger(HallServlet.class.getName());
    private final Store store = PsqlStore.instOf();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            Collection<Ticket> tickets = store.findAllTickets();
            List<String> occupSeats = new ArrayList<>();
            tickets.forEach(t -> occupSeats.add(t.getRow() + "-" + t.getCell()));
            JSONObject jsonObj = new JSONObject()
                    .put("rows", rows)
                    .put("seats", cells)
                    .put("refreshInterval", refreshInterval)
                    .put("occupiedSeats", occupSeats);
            PrintWriter writer = resp.getWriter();
            writer.print(jsonObj);
            writer.flush();
        } catch (Exception e) {
            LOG.error("Exception occurred: ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JSONObject jsonReq;
        try {
            jsonReq = new JSONObject(req.getParameterNames().asIterator().next());
            Account account = new Account(0,
                    jsonReq.getString("username"),
                    jsonReq.getString("email"),
                    jsonReq.getString("phone")
            );
            Ticket ticket = store.save(new Ticket(
                    0,
                    sessionId,
                    Integer.parseInt(jsonReq.getString("row")),
                    Integer.parseInt(jsonReq.getString("seat")),
                    account)
            );
            String msg = "К сожалению, место не может быть куплено.";
            int code = 0;
            if (ticket != null) {
                msg = "Место успешно оплачено!";
                code = 1;
            }
            JSONObject jsonResp = new JSONObject();
            jsonResp.put("message", msg);
            jsonResp.put("code", code);
            PrintWriter writer = resp.getWriter();
            writer.print(jsonResp);
            writer.flush();
        } catch (Exception e) {
            LOG.error("Exception occurred: ", e);
        }
    }

    /**
     * rows - the number of rows in the cinema hall,
     * seats - the number of seats in the cinema hall,
     * sessionId - the movie session id,
     * refreshInterval - the data retry interval on the client (ms)
     */
    @Override
    public void init() {
        rows = Integer.parseInt(getServletContext().getInitParameter("rows"));
        cells = Integer.parseInt(getServletContext().getInitParameter("cells"));
        sessionId = Integer.parseInt(getServletContext().getInitParameter("sessionId"));
        refreshInterval = Integer.parseInt(getServletContext().getInitParameter("refreshInterval"));
    }
}