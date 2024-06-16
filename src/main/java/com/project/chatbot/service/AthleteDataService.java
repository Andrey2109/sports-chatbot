package com.project.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chatbot.repository.AthleteRepository;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AthleteDataService {

    @Autowired
    private AthleteRepository athleteRepository;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    private static final Logger logger = Logger.getLogger(AthleteDataService.class.getName());

    @Autowired
    public AthleteDataService(ObjectMapper objectMapper) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = objectMapper;
    }

    public List<Athlete> fetchAthletes() {
        List<Athlete> athletes = athleteRepository.findAll();
        logger.log(Level.FINE, "Fetched {0} athletes from the database", athletes.size());
        return athletes;
    }

    public String fetchCompetitionDetails(String athleteId) throws IOException {
        Request request = new Request.Builder()
                .url("https://www.gymnastics.sport/site/athletes/bio_detail.php?id=" + athleteId)
                .get()
                .headers(buildCommonHeaders())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Request failed: " + response);
            }

            return response.body().string();
        }
    }

    @PostConstruct
    public void populateAthletesFromApi() throws IOException {
        List<Athlete> athletes = fetchAthletesFromApi();
        athleteRepository.saveAll(athletes);
    }

    private List<Athlete> fetchAthletesFromApi() throws IOException {
        Request request = new Request.Builder()
                .url("https://www.gymnastics.sport/api/athletes.php?function=searchBios&discipline=&country=ISR&lastname=&status=")
                .get()
                .headers(buildCommonHeaders())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonData = response.body().string();
            Athlete[] athletesArray = objectMapper.readValue(jsonData, Athlete[].class);
            return List.of(athletesArray);
        }
    }

    private Headers buildCommonHeaders() {
        return new Headers.Builder()
                .add("accept", "*/*")
                .add("accept-language", "en-US,en;q=0.9,he-IL;q=0.8,he;q=0.7,ru;q=0.6")
                .add("referer", "https://www.gymnastics.sport/site/athletes/bio_view.php")
                .add("sec-ch-ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .add("sec-ch-ua-mobile", "?0")
                .add("sec-ch-ua-platform", "\"Windows\"")
                .add("sec-fetch-dest", "empty")
                .add("sec-fetch-mode", "cors")
                .add("sec-fetch-site", "same-origin")
                .add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .add("x-requested-with", "XMLHttpRequest")
                .build();
    }
}



