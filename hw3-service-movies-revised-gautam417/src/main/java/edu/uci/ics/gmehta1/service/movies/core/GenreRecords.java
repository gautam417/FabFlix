package edu.uci.ics.gmehta1.service.movies.core;

import edu.uci.ics.gmehta1.service.movies.MovieService;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.GenreAddRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.GenreAddResponseModel;
import edu.uci.ics.gmehta1.service.movies.models.GenreModel;
import edu.uci.ics.gmehta1.service.movies.models.GenreResponseModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GenreRecords {
    public static GenreResponseModel retrieveGenresFromDB() {
        try {
            String query = "SELECT * " +
                           "FROM genres;";

            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ServiceLogger.LOGGER.info("Trying query to search for all genres in DB:" + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded to retrieve all genres.");
            ArrayList<Genre> genres = new ArrayList<>();
            while (rs.next())
            {
                Genre s2 = new Genre (rs.getInt("id"),rs.getString("name"));
                genres.add(s2);
            }
            int len = genres.size();
            GenreModel[] array = new GenreModel[len];
            for (int i = 0; i < len; ++i) {
                ServiceLogger.LOGGER.info("Adding genre " + genres.get(i).getName() + " to array.");
                // Convert each student in the arraylist to a StudentModel
                GenreModel sm = GenreModel.buildModelFromObject(genres.get(i));
                // If the new model has valid data, add it to array
                array[i] = sm;
            }
            return new GenreResponseModel(219, "Genres successfully retrieved.", array);

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        return null; // this shouldnt be correct
    }
    public static GenreAddResponseModel addIntoDB(GenreAddRequestModel garm) {
        try {
            String query = "INSERT INTO genres (name) " +
                    "VALUES (?);";
            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setString(1, garm.getName());
            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            if (ps.executeUpdate() == 0)
            {
                return new GenreAddResponseModel(218,"Genre could not be added.");
            }
            ServiceLogger.LOGGER.info("Query succeeded to add genre.");
            return new GenreAddResponseModel(217,"Genre successfully added.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to add genre in genres.");
            e.printStackTrace();
        }
        //ELSE:
//        GenreAddResponseModel responseModel = new GenreAddResponseModel(221,"Could not add star.");
        return null; // this shouldnt be correct
    }
    public static GenreResponseModel retrieveGenresById(String id) {
        try {
            String query = "SELECT genres.id, name " +
                    "FROM movies, genres, genres_in_movies gm " +
                    "WHERE movies.id = gm.movieId AND gm.genreId = genres.id " +
                    "AND movies.id = ?;";
            PreparedStatement ps = MovieService.getCon().prepareStatement(query);// ResultSet.TYPE_SCROLL_INSENSITIVE,
//                ResultSet.CONCUR_READ_ONLY,
//                        ResultSet.HOLD_CURSORS_OVER_COMMIT
            ps.setString(1, id);

            ServiceLogger.LOGGER.info("Trying to get genres by id query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ArrayList<Genre> genres = new ArrayList<>();
            while (rs.next())
            {
                Genre s2 = new Genre (rs.getInt("id"),rs.getString("name"));
                genres.add(s2);
            }
            int len = genres.size();
            GenreModel[] array = new GenreModel[len];
            for (int i = 0; i < len; ++i) {
                ServiceLogger.LOGGER.info("Adding genre " + genres.get(i).getName() + " to array.");
                // Convert each student in the arraylist to a StudentModel
                GenreModel sm = GenreModel.buildModelFromObject(genres.get(i));
                // If the new model has valid data, add it to array
                array[i] = sm;
            }
            return new GenreResponseModel(219,"Genres successfully retrieved.",array);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Star query failed: Unable to retrieve movie records.");
            e.printStackTrace();
        }
        return new GenreResponseModel(211, "No movies found with search parameters.",null);
    }
}
