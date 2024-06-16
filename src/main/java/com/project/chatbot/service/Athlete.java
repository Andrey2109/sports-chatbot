package com.project.chatbot.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "athletes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Athlete {

    @Id
    private String id;
    private String preferredlastname;
    private String preferredfirstname;
    private String country;
    private String discipline;
    private String status;

    // Getters and Setters
    public String getId() {
        return id;
    }


    public String getPreferredlastname() {
        return preferredlastname;
    }



    public String getPreferredfirstname() {
        return preferredfirstname;
    }



    public String getCountry() {
        return country;
    }



    public String getDiscipline() {
        return discipline;
    }


    public String getStatus() {
        return status;
    }


}
