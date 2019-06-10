package edu.uci.ics.gmehta1.service.movies.core;

import edu.uci.ics.gmehta1.service.movies.MovieService;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;
import edu.uci.ics.gmehta1.service.movies.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StarRecords {
    public static int count = 1;
    public static StarAddResponseModel addStarsIn(StarsInRequestModel sirm) {
        //FIRST QUERY TO SEE IF MOVIE EXISTS
        //USE THE /api/movies/get/{movieid}
        //THIS WILL RETURN Case 211: No movies found with search parameters.
        //THEN QUERY TO SEE IF STAR EXISTS IN STAR
        //IF THATS TRUE THEN TRY
         try {
            String query = "INSERT INTO stars_in_movies (starId, movieId) " +
                    "VALUES (?, ?);";

            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setString(1, sirm.getStarid());
            ps.setString(2,sirm.getMovieId());


            try{
                String query2 = "SELECT COUNT(*) " +
                        "FROM movies " +
                        "WHERE id = ?;";

                PreparedStatement ps2 = MovieService.getCon().prepareStatement(query2);
                ps2.setString(1, sirm.getMovieId());
                ResultSet rs = ps2.executeQuery();
                rs.next();
                int count = rs.getInt("COUNT(*)");
                if (count <=0 ){
                    return new StarAddResponseModel(211,"No movies found with search parameters.");

                }
            }catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to add a star into starsin.");
                e.printStackTrace();
            }

            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            //CHECK TO SEE IF THERE WAS A CHANGE IN THE DB, IF NOT THEN THAT MEANS IT ALREADY EXISTS
             if (ps.executeUpdate() == 0)
             {
                 return new StarAddResponseModel(232,"Star already exists in movie.");
             }
//             ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded to add star record.");

            return new StarAddResponseModel(230,"Star successfully added to movie.");

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to add a star into starsin.");
            e.printStackTrace();
        }
         //ELSE:
        StarAddResponseModel responseModel = new StarAddResponseModel(231,"Could not add star to movie.");
        return responseModel;
    }
    public static StarAddResponseModel addStar1(StarAddRequestModel sarm) {
        try {
            String query = "INSERT INTO stars (id, name, birthYear) " +
                    "VALUES (?, ?, ?);";
            String original = "00000000";
            String incremented = String.format("%0" + original.length() + "d",
                    Integer.parseInt(original) + count++);
            PreparedStatement ps = MovieService.getCon().prepareStatement(query);
            ps.setString(1, "ss"+incremented);
            ServiceLogger.LOGGER.info("New Id:" + "ss"+incremented);
            ps.setString(2, sarm.getName());
            //DO THE CHECK HERE
            if (sarm.getBirthYear()> 2019 )
            {
                ps.setString(3, null);
            }
            ps.setInt(3, sarm.getBirthYear());

            ServiceLogger.LOGGER.info("Trying query:" + ps.toString());
            if (ps.executeUpdate() == 0 ){
                return new StarAddResponseModel(222,"Star already exists.");
            }
            try{
                String query2 = "SELECT COUNT(*) " +
                        "FROM stars " +
                        "WHERE name = ? AND birthYear = ?;";

                PreparedStatement ps2 = MovieService.getCon().prepareStatement(query2);
                ps2.setString(1, sarm.getName());
                ps2.setInt(2,sarm.getBirthYear());
                ResultSet rs = ps2.executeQuery();
                rs.next();
                ServiceLogger.LOGGER.info("Trying query to see if already exists:" + ps2.toString());
                int count = rs.getInt("COUNT(*)");
                ServiceLogger.LOGGER.warning("Count here: "+count);

                if (count >= 1)
                {
                    ServiceLogger.LOGGER.warning("Count: "+count);
                    return new StarAddResponseModel(222,"Star already exists.");
                }
            }catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to check if star exists.");
                e.printStackTrace();
            }

            ServiceLogger.LOGGER.info("Query succeeded to retrieve movie records.");
            return new StarAddResponseModel(220,"Star successfully added.");

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to add a star in stars.");
            e.printStackTrace();
        }
        //ELSE:
        StarAddResponseModel responseModel = new StarAddResponseModel(221,"Could not add star.");
        return responseModel;
    }
    public static StarResponseModel retrieveMoviesById(String id) {
            try {
                String query = "SELECT id, name, birthYear " +
                        "FROM stars " +
                        "WHERE id = ?;";
                PreparedStatement ps = MovieService.getCon().prepareStatement(query);// ResultSet.TYPE_SCROLL_INSENSITIVE,
//                ResultSet.CONCUR_READ_ONLY,
//                        ResultSet.HOLD_CURSORS_OVER_COMMIT
                ps.setString(1, id);

                ServiceLogger.LOGGER.info("Trying to get star by id query: " + ps.toString());
                ResultSet rs3 = ps.executeQuery();
                ArrayList<Star> stars = new ArrayList<>();
//                int len1 = 0;
//                if (rs3 != null)
//                {
//                    rs3.last();
//                    len1 = rs3.getRow();
//                }
//                rs3.beforeFirst();
                StarModel[] s = new StarModel[1];
                while (rs3.next())
                {
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
                if (stars.size() >= 1)
                {
                    return new StarResponseModel(212,"Found stars with search parameters.",s);
                }
            } catch (SQLException e)
            {
                ServiceLogger.LOGGER.warning("Star query failed: Unable to retrieve movie records.");
                e.printStackTrace();
            }
            return new StarResponseModel(213, "No stars found with search parameters.",null);
        }
        public static StarResponseModel retrieveStarsFromDB(StarRequestModel srm) {
            try {
                String query = "SELECT DISTINCT s.id, name, birthYear " +
                        "FROM movies, stars s, stars_in_movies sm " +
                        "WHERE movies.id = sm.movieId AND sm.starId = s.id " +
                        "AND s.birthYear LIKE \'%" + srm.getBirthYear() + "%\' " +
                        "AND movies.title LIKE \'%"+ srm.getMovieTitle() + "%\' " +
                        "AND s.name LIKE \'%" +srm.getName() + "%\' " +
                        "ORDER BY " + srm.getOrderby() + " " +
                        srm.getDirection() + " " +
                        "LIMIT ?, ?;";

                PreparedStatement ps = MovieService.getCon().prepareStatement(query);
                ps.setInt(1, srm.getOffset());
                ps.setInt(2,srm.getLimit());

                ServiceLogger.LOGGER.info("Trying query to search for a star:" + ps.toString());
                ResultSet rs = ps.executeQuery();
                ServiceLogger.LOGGER.info("Query succeeded to retrieve movie records.");
                ArrayList<Star> stars = new ArrayList<>();
                while (rs.next())
                {
                    Star s2 = new Star (rs.getString("id"),rs.getString("name"),rs.getInt("birthYear"));
                    stars.add(s2);
                }
                int len = stars.size();
                StarModel[] array = new StarModel[len];
                for (int i = 0; i < len; ++i) {
                    ServiceLogger.LOGGER.info("Adding star " + stars.get(i).getName() + " to array.");
                    // Convert each student in the arraylist to a StudentModel
                    StarModel sm = StarModel.buildModelFromObject(stars.get(i));
                    // If the new model has valid data, add it to array
                    array[i] = sm;
                }
                if (len >=1){
                    return new StarResponseModel(212, "Found stars with search parameters.", array);
                }
            } catch (SQLException e) {
                ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie records.");
                e.printStackTrace();
            }
            StarResponseModel responseModel = new StarResponseModel(213,"No stars found with search parameters.",null);
            return responseModel;
        }
}
