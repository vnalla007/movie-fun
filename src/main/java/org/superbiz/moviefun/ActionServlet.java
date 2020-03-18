package org.superbiz.moviefun;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MoviesBean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
@Component
public class ActionServlet extends HttpServlet {

    private static final long serialVersionUID = -5832176047021911038L;

    public static int PAGE_SIZE = 5;

    @Autowired
    private final MoviesBean moviesBean;
    private final PlatformTransactionManager moviesTransactionManager;

    public ActionServlet(MoviesBean moviesBean, PlatformTransactionManager moviesTransactionManager) {
        this.moviesBean = moviesBean;
        this.moviesTransactionManager = moviesTransactionManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        TransactionTemplate transactionTemplate = new TransactionTemplate(moviesTransactionManager);

        if ("Add".equals(action)) {

            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String genre = request.getParameter("genre");
            int rating = Integer.parseInt(request.getParameter("rating"));
            int year = Integer.parseInt(request.getParameter("year"));

            Movie movie = new Movie(title, director, genre, rating, year);

            transactionTemplate.execute(status -> {
                moviesBean.addMovie(movie);
                return null;
            });

            response.sendRedirect("moviefun");
            return;

        } else if ("Remove".equals(action)) {

            String[] ids = request.getParameterValues("id");
            transactionTemplate.execute(status -> {
                for (String id : ids) {
                    moviesBean.deleteMovieId(new Long(id));
                }

                return null;
            });

            response.sendRedirect("moviefun");
            return;

        } else {
            String key = request.getParameter("key");
            String field = request.getParameter("field");

            int count = 0;

            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
                count = moviesBean.countAll();
                key = "";
                field = "";
            } else {
                count = moviesBean.count(field, key);
            }

            int page = 1;

            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (Exception e) {
            }

            int pageCount = (count / PAGE_SIZE);
            if (pageCount == 0 || count % PAGE_SIZE != 0) {
                pageCount++;
            }

            if (page < 1) {
                page = 1;
            }

            if (page > pageCount) {
                page = pageCount;
            }

            int start = (page - 1) * PAGE_SIZE;
            List<Movie> range;

            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
                range = moviesBean.findAll(start, PAGE_SIZE);
            } else {
                range = moviesBean.findRange(field, key, start, PAGE_SIZE);
            }

            int end = start + range.size();

            request.setAttribute("count", count);
            request.setAttribute("start", start + 1);
            request.setAttribute("end", end);
            request.setAttribute("page", page);
            request.setAttribute("pageCount", pageCount);
            request.setAttribute("movies", range);
            request.setAttribute("key", key);
            request.setAttribute("field", field);
        }

        request.getRequestDispatcher("WEB-INF/moviefun.jsp").forward(request, response);
    }

}