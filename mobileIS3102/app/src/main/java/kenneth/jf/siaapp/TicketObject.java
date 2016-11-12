package kenneth.jf.siaapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by User on 28/10/2016.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketObject {
    private String id;
    private String details;

    public TicketObject(){
        this.id = getTicketUUID();
        this.details = getTicketDetails();
    }


    @JsonProperty("ticketUUID")
    private String ticketUUID;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("start_date")
    private String start_date;

    @JsonProperty("end_date")
    private String end_date;

    @JsonProperty("purchase_date")
    private String purchase_date;

    @JsonProperty("ticketDetails")
    private String ticketDetails;

    public String getTicketUUID() {
        return ticketUUID;
    }

    public String getTicketDetails() {
        return ticketDetails;
    }

    public void setTicketDetails(String ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public String toString(){
        return ticketDetails;
    }


}