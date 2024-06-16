package com.project.chatbot.controller;

import com.project.chatbot.service.Athlete;
import com.project.chatbot.service.AthleteDataService;
import com.project.chatbot.service.AthletesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RestController
public class AthleteController {

    @Autowired
    private AthleteDataService athleteDataService;

    @Autowired
    private AthletesService athletesService;

    @GetMapping("/getAthletesList")
    public ResponseEntity<List<Athlete>> getAthletes() {
        List<Athlete> athletes = athleteDataService.fetchAthletes();
        return new ResponseEntity<>(athletes, HttpStatus.OK);
    }

    @GetMapping("/getAthleteSummary")
    public ResponseEntity<String> getAthleteSummary(@RequestParam String athleteName) throws IOException {
        String summary = athletesService.fetchCompetitionDetails(athleteName);
        if (summary.equals("Athlete not found")) {
            return new ResponseEntity<>("Athlete not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    @GetMapping("/askChatGPTAboutAthletes")
    public ResponseEntity<String> askChatGPTAboutAthletes(@RequestParam String prompt) throws IOException {
        String response = athletesService.askChatGPTAboutAthletes(prompt);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/bot")
    public ResponseEntity<BotResponse> getBotResponse(@RequestBody BotQuery query) throws IOException {
        HashMap<String, String> params = query.getQueryResult().getParameters();
        String res = "Not found";
        if (params.containsKey("question")) {
            res = athletesService.askChatGPTAboutAthletes(params.get("question"));
        }
        return new ResponseEntity<>(BotResponse.of(res), HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BotQuery {
        private QueryResult queryResult;

        public QueryResult getQueryResult() {
            return queryResult;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QueryResult {
        private HashMap<String, String> parameters;

        public HashMap<String, String> getParameters() {
            return parameters;
        }
    }


    public static class BotResponse {
        private String fulfillmentText;
        private final String source = "BOT";

        public String getFulfillmentText() {
            return fulfillmentText;
        }

        public String getSource() {
            return source;
        }


        public static BotResponse of(String fulfillmentText) {
            BotResponse res = new BotResponse();
            res.fulfillmentText = fulfillmentText;
            return res;
        }
    }
}

