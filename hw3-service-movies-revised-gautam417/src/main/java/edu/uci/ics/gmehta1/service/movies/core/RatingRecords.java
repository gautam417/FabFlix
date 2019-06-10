package edu.uci.ics.gmehta1.service.movies.core;

import edu.uci.ics.gmehta1.service.movies.MovieService;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.RatingRequestModel;
import edu.uci.ics.gmehta1.service.movies.models.StarAddResponseModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RatingRecords {
    public static StarAddResponseModel updateRatingFromDB(RatingRequestModel rrm)
    {
        //USE THE /api/movies/get/{movieid}
        try {
                String query2 =
                        "SELECT numVotes, rating " +
                                "FROM ratings " +
                                "WHERE movieId LIKE ?;";
                // Create the prepared statement
                PreparedStatement ps2 = MovieService.getCon().prepareStatement(query2);
                ps2.setString(1, rrm.getId());
                ServiceLogger.LOGGER.info("Trying query: " + ps2.toString());
                ResultSet rs = ps2.executeQuery();
                rs.next();
                int votes = rs.getInt("numVotes");
                float rating = rs.getFloat("rating");
                float newRating = (votes*rating+rrm.getRating())/(votes+1);
                ServiceLogger.LOGGER.info("Newrating: " + newRating);
            // Construct the query
                try {

                String query = "UPDATE ratings " +
                        "SET numVotes = numVotes + 1, rating = " + newRating + " " +
                        "WHERE movieId = ?;"; //might be ratings.movieId
                // Create the prepared statement
                PreparedStatement ps = MovieService.getCon().prepareStatement(query);

                // Set the arguments
                ps.setString(1, rrm.getId());

                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                if (ps.executeUpdate() == 0)
                {
                    return new StarAddResponseModel(251,"Could not update rating.");
                }
                ServiceLogger.LOGGER.info("Query succeeded to update rating info .");
                return new StarAddResponseModel(250,"Rating successfully updated.");
                } catch (SQLException e)
                {
                    ServiceLogger.LOGGER.warning("Query failed: Unable to update rating from ratings table.");
                    e.printStackTrace();
                }
            }catch (SQLException e)
            {
                ServiceLogger.LOGGER.warning("Query failed: Unable to update rating from ratings table.");
                e.printStackTrace();
            }
        ServiceLogger.LOGGER.info("SOMETHING WEIRD HAPPENED.");
        return new StarAddResponseModel(211,"No movies found with search parameters.");
    }
}
