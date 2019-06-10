package edu.uci.ics.gmehta1.service.billing.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.billing.core.Item;
import edu.uci.ics.gmehta1.service.billing.logger.ServiceLogger;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_EMPTY) // Tells Jackson to ignore all fields with value of NULL
public class ItemsResponseModel {

    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ItemModel[] items;

    @JsonCreator
    public ItemsResponseModel
            (
                    @JsonProperty(value = "resultCode", required = true) int resultCode,
                    @JsonProperty(value = "message", required = true) String message,
                    @JsonProperty(value = "items") ItemModel[] items
            )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.items = items;
    }
    public static ItemsResponseModel buildModelFromList(ArrayList<Item> items) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");

        if (items.size() == 0) {
            ServiceLogger.LOGGER.info("No items list passed to model constructor.");
            return new ItemsResponseModel(312,"Shopping item does not exist.", null);
        }

        ServiceLogger.LOGGER.info("Students list is not empty...");
        int len = items.size();
        ItemModel[] array = new ItemModel[len];

        for (int i = 0; i < len; ++i)
        {
            ServiceLogger.LOGGER.info("Adding item " + items.get(i).getMovieId() + " to array.");
            // Convert each item in the arraylist to an ItemModel
            ItemModel im = ItemModel.buildModelFromObject(items.get(i));
            // If the new model has valid data, add it to array
            array[i] = im;
        }
        ServiceLogger.LOGGER.info("Finished building model. Array of items contains: ");
        for (ItemModel im : array) {
            ServiceLogger.LOGGER.info("\t" + im);
        }
        return new ItemsResponseModel(3130,"Shopping cart retrieved successfully.", array);
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("items")
    public ItemModel[] getItems() { return items; }
}
