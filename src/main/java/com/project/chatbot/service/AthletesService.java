package com.project.chatbot.service;

import com.project.chatbot.repository.AthleteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AthletesService {

    @Autowired
    private AthleteDataService athleteDataService;

    @Autowired
    private HtmlProcessingService htmlProcessingService;

    @Autowired
    private ChatGPTService chatGPTService;

    @Autowired
    private AthleteRepository athleteRepository;

    private static final Logger logger = Logger.getLogger(AthletesService.class.getName());

    public String fetchCompetitionDetails(String athleteName) throws IOException {
        List<Athlete> athletes = athleteDataService.fetchAthletes();
        logger.log(Level.FINE, "Fetched {0} athletes from the database", athletes.size());

        String athleteId = findAthleteIdByName(athleteName);
        logger.log(Level.FINE, "Found athlete ID: {0}", athleteId);

        if (athleteId == null) {
            return "Athlete not found";
        }

        String competitionDetailsHtml = athleteDataService.fetchCompetitionDetails(athleteId);
        return htmlProcessingService.processHtml(competitionDetailsHtml);
    }

    public String askChatGPTAboutAthletes(String prompt) throws IOException {
        List<Athlete> athletes = athleteDataService.fetchAthletes();
        String athleteName = extractAthleteName(prompt, athletes);

        if (athleteName == null) {
            return "Athlete name not found in the prompt.";
        }

        String competitionDetailsHtml = athleteDataService.fetchCompetitionDetails(findAthleteIdByName(athleteName));
        String competitionDetails = htmlProcessingService.processHtml(competitionDetailsHtml);

        String combinedPrompt = prompt + "\n\nHere are the competition details for " + athleteName + ":\n" + competitionDetails;
        return chatGPTService.sendToChatGPT(combinedPrompt);
    }

    public String findAthleteIdByName(String athleteName) {
        String normalizedPromptName = athleteName.toLowerCase().trim();
        logger.log(Level.FINE, "Searching for normalized name: {0}", normalizedPromptName);
        String[] nameParts = normalizedPromptName.split(" ");

        if (nameParts.length == 2) {
            String firstName = nameParts[0];
            String lastName = nameParts[1];

            Optional<Athlete> athleteOptional = athleteRepository.findByPreferredlastnameAndPreferredfirstname(lastName, firstName);
            if (athleteOptional.isPresent()) {
                logger.log(Level.FINE, "Found athlete with first name: {0} and last name: {1}", new Object[]{firstName, lastName});
                return athleteOptional.get().getId();
            }

            // Try swapping the order
            athleteOptional = athleteRepository.findByPreferredlastnameAndPreferredfirstname(firstName, lastName);
            if (athleteOptional.isPresent()) {
                logger.log(Level.FINE, "Found athlete with first name: {0} and last name: {1}", new Object[]{lastName, firstName});
                return athleteOptional.get().getId();
            }
        }

        logger.log(Level.WARNING, "Invalid athlete name format or athlete not found: {0}", athleteName);
        return null;
    }

    private String normalizeName(String name) {
        String normalized = name.toLowerCase().trim();
        logger.log(Level.FINE, "Normalized name: {0}", normalized);
        return normalized;
    }

    private String extractAthleteName(String prompt, List<Athlete> athletes) {
        prompt = prompt.toLowerCase();
        for (Athlete athlete : athletes) {
            String fullName = athlete.getPreferredlastname() + " " + athlete.getPreferredfirstname();
            String fullNameLower = fullName.toLowerCase();
            String lastNameLower = athlete.getPreferredlastname().toLowerCase();
            String firstNameLower = athlete.getPreferredfirstname().toLowerCase();

            if (prompt.contains(fullNameLower) || prompt.contains(lastNameLower) || prompt.contains(firstNameLower)) {
                return fullName;
            }
        }
        return null;
    }
}


