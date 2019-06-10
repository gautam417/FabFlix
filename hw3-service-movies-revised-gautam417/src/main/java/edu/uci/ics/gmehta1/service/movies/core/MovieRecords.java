package edu.uci.ics.gmehta1.service.movies.core;

import edu.uci.ics.gmehta1.service.movies.MovieService;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MovieRecords {
    public static int count =1;
    public static AddResponseModel addMovie(AddRequestModel arm) {
        try {
            String query = "INSERT INTO movies (id, title, director, year, backdrop_path, budget, overview, poster_path, revenue) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            String original = "00000000";
            String incremented = String.format("%0" + original.length() + "d",
                    Integer.parseInt(original) + count++);
            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setString(1, "cs"+incremented);
            ServiceLogger.LOGGER.info("New Id:" + "cs"+incremented);
            ps.setString(2, arm.getTitle());
            ps.setString(3,arm.getDirector());
            ps.setInt(4,arm.getYear());
            ps.setString(5,arm.getBackdrop_path());
            ps.setInt(6,arm.getBudget());
            ps.setString(7,arm.getOverview());
            ps.setString(8,arm.getPoster_path());
            ps.setInt(9,arm.getRevenue());


            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new AddResponseModel(216,"Movie already exists.",null,null);
            }
            ServiceLogger.LOGGER.info("Query succeeded to add a movie records.");
            try {
                String query3 = "INSERT INTO ratings (movieId, rating, numVotes) " +
                        "VALUES (?, 0.0, 0);";
                PreparedStatement ps3 = MovieService.getCon().prepareStatement(query3);
                ps3.setString(1, "cs"+incremented);
                ServiceLogger.LOGGER.info("Query succeeded to add a movie into rating table.");
                ps3.executeUpdate();
                ServiceLogger.LOGGER.info("Trying query:" + ps3.toString());

            } catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to add a movies rating in ratings.");
                e.printStackTrace();
            }
//            try {
//                String query2 = "INSERT INTO genres_in_movies (genreId, movieId) " +
//                        "VALUES (?, ?);";
//                PreparedStatement ps2 = MovieService.getCon().prepareStatement(query2);
//                ps.setInt(1, arm.getGenres());
//                ps.setString(2, "cs"+incremented);
//
//
//                ServiceLogger.LOGGER.info("Query succeeded to add a movie records.");
//                return new AddResponseModel(214,"Movie successfully added.", "cs"+incremented, );
//
//            } catch (SQLException e) {
//                ServiceLogger.LOGGER.warning("Query failed: Unable to add a movie in movies.");
//                e.printStackTrace();
//            }
            return new AddResponseModel(214,"Movie successfully added.","cs"+incremented, arm.getGenres());

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to add a movie in movies.");
            e.printStackTrace();
        }
        AddResponseModel responseModel = new AddResponseModel(215,"Could not add movie.",null,null);
        return responseModel;
    }
    public static SearchResponseModel retrieveMoviesFromDB(SearchRequestModel srm) {
//        String secondaryDirection = "";
//        String secondaryOrderby = "";
//
//        if (srm.getDirection() == "desc"){
//            secondaryDirection = "asc";
//        }
//        else if (srm.getDirection() == "asc"){
//            secondaryDirection = "desc";
//        }
//        else if (srm.getOrderby() == "rating"){
//            secondaryOrderby = "title";
//        }
//        else if (srm.getOrderby() == "title"){
//            secondaryOrderby = "rating";
//        }
        try {
            String query = "SELECT DISTINCT movies.id, title, director, year, rating, numVotes " +
                    "FROM movies JOIN ratings ON ratings.movieId = movies.id, genres g, genres_in_movies gm " +
                    "WHERE movies.id = gm.movieId AND gm.genreId = g.id " +
                    "AND movies.year LIKE \'%" + srm.getYear() + "%\' AND movies.director LIKE \'%" + srm.getDirector() + "%\' " +
                    "AND movies.title LIKE \'%"+ srm.getTitle() + "%\' " +
                    "AND g.name LIKE \'%" +srm.getGenre() + "%\' " +
                    "ORDER BY " + srm.getOrderby() + " " +
                     srm.getDirection() + ", " +
                    "title ASC " +
                    "LIMIT ?, ?;";

            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setInt(1, srm.getOffset());
            ps.setInt(2,srm.getLimit());

            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve movie records.");
            ArrayList<Movie> movies = new ArrayList<>();
            while (rs.next())
            {
                Movie m = new Movie(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("director"),
                        rs.getInt("year"),
                        rs.getFloat("rating"),
                        rs.getInt("numVotes"),
                        false
                );
                movies.add(m);
            }
            return SearchResponseModel.buildModelFromList(movies);

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        SearchResponseModel responseModel = new SearchResponseModel(211,"No movies found with search parameters.",null);
        return responseModel;
    }
    public static GetResponseModel getMoviesFromDB(String movieId) throws NullPointerException {
        try {
            String query2 = "SELECT DISTINCT g.id, g.name " +
                    "FROM movies, genres g, genres_in_movies gm " +
                    "WHERE movies.id = gm.movieId AND gm.genreId = g.id " +
                    "AND movies.id = ?;";
            PreparedStatement ps2 = MovieService.getCon().prepareStatement(query2,ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);
            ps2.setString(1, movieId);

            ServiceLogger.LOGGER.info("Trying genres query: " + ps2.toString());
            ResultSet rs2 = ps2.executeQuery();
            ArrayList<Genre> genres = new ArrayList<>();
//            ArrayList<Star> stars = new ArrayList<>();
//            StarModel[] s = new StarModel[len];
            int len = 0;
            if (rs2 != null)
            {
                rs2.last();
                len = rs2.getRow();
            }
            rs2.beforeFirst();
            GenreModel[] g = new GenreModel[len];
            while (rs2.next())
            {
                Genre g2 = new Genre (rs2.getInt("id"), rs2.getString("name"));
//                Star s2 = new Star (rs.getString("id"),rs.getString("name"));
                genres.add(g2);
//                stars.add(s2);
            }
            for (int i = 0; i < genres.size(); ++i)
            {
                ServiceLogger.LOGGER.info("Adding genre " + genres.get(i).getName() + " to genre array.");
//                ServiceLogger.LOGGER.info("Adding star " + stars.get(i).getName() + " to star array.");
                GenreModel gm = GenreModel.buildModelFromObject(genres.get(i));
                g[i] = gm;
            }            try {
                String query3 = "SELECT DISTINCT s.id, s.name, birthYear " +
                        "FROM movies, stars s, stars_in_movies sm " +
                        "WHERE movies.id = sm.movieId AND sm.starId = s.id " +
                        "AND movies.id = ?;";
                PreparedStatement ps3 = MovieService.getCon().prepareStatement(query3,ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT);
                ps3.setString(1, movieId);

                ServiceLogger.LOGGER.info("Trying stars query: " + ps3.toString());
                ResultSet rs3 = ps3.executeQuery();
                ArrayList<Star> stars = new ArrayList<>();
                int len1 = 0;
                if (rs3 != null)
                {
                    rs3.last();
                    len1 = rs3.getRow();
                }
                rs3.beforeFirst();
                StarModel[] s = new StarModel[len1];

                while (rs3.next())
                {
//                    if ((Integer) rs3.getObject("birthYear") == null){
//                        Integer a = null;
//                        Star s2 = new Star (rs3.getString("id"),rs3.getString("name"),a);
//                        stars.add(s2);
//                    }
                    Star s2 = new Star (rs3.getString("id"),rs3.getString("name"),rs3.getInt("birthYear"));
                    stars.add(s2);
                }
                for (int i = 0; i < stars.size(); ++i)
                {
                ServiceLogger.LOGGER.info("Adding star " + stars.get(i).getName() + " to star array.");
                    // Convert each genre in the arraylist to a Model
                    StarModel sm = StarModel.buildModelFromObject(stars.get(i));
                    s[i] = sm;
                }
            try {
                String query = "SELECT DISTINCT movies.id, title, director, year, backdrop_path, budget, overview, poster_path, revenue, rating, numVotes " +
                        "FROM movies JOIN ratings ON ratings.movieId = movies.id " +
                        "WHERE movies.id = ?;";

                PreparedStatement ps = MovieService.getCon().prepareStatement(query);
                ps.setString(1,movieId);

                ServiceLogger.LOGGER.info("Trying movies query:" + ps.toString());
                ResultSet rs = ps.executeQuery();
                ServiceLogger.LOGGER.info("Query succeeded to retrieve movie records.");

                ArrayList<GetMovie> movies = new ArrayList<>();
                rs.next();
                {
                    GetMovie m = new GetMovie(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("director"),
                            rs.getInt("year"),
                            rs.getString("backdrop_path"),
                            rs.getInt("budget"),
                            rs.getString("overview"),
                            rs.getString("poster_path"),
                            rs.getInt("revenue"),
                            rs.getFloat("rating"),
                            rs.getInt("numVotes"),
                            g,
                            s
                    );
                    movies.add(m);
                }
                return GetResponseModel.buildModelFromList(movies);

            } catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Movies Query failed: Unable to retrieve movie records.");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Star query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Genre query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        GetResponseModel responseModel = new GetResponseModel(211,"No movies found with search parameters.",null);
        return responseModel;
    }
    public static DeleteResponseModel deleteMovie(String movieId) {
        if (!doesMovieExist(movieId)){
            return new DeleteResponseModel(241, "Could not remove movie.");
        }
        try {
            String query = "UPDATE movies " +
                    "SET hidden = 1 " +
                    "WHERE id LIKE ?;";

            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setString(1, movieId);

            ServiceLogger.LOGGER.info("Trying query to delete movie:" + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new DeleteResponseModel(241, "Could not remove movie.");

            }
            ServiceLogger.LOGGER.info("Query succeeded to delete movies.");

            return new DeleteResponseModel(240, "Movie successfully removed.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        ServiceLogger.LOGGER.info("Already tried to remove this movie ");
        return new DeleteResponseModel(242,"Movie has been already removed.");
    }
    public static boolean doesMovieExist(String movieId) {
        try {
            String query = "SELECT id " +
                    "FROM movies " +
                    "WHERE id = " + movieId + ";";

            PreparedStatement ps = MovieService.getCon().prepareStatement(query);

            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve movie records.");
            ArrayList<Movie> movies = new ArrayList<>();
            while (rs.next())
            {
                Movie m = new Movie(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("director"),
                        rs.getInt("year"),
                        rs.getFloat("rating"),
                        rs.getInt("numVotes"),
                        false
                );
                movies.add(m);
            }
            if (movies.size() > 0){
                return true;
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        SearchResponseModel responseModel = new SearchResponseModel(211,"No movies found with search parameters.",null);
        return false;
    }
}