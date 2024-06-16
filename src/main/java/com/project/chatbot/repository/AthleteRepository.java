package com.project.chatbot.repository;

import com.project.chatbot.service.Athlete;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface AthleteRepository extends MongoRepository<Athlete, String> {

    @Query("{ 'preferredlastname' : { $regex: ?0, $options: 'i' }, 'preferredfirstname' : { $regex: ?1, $options: 'i' } }")
    Optional<Athlete> findByPreferredlastnameAndPreferredfirstname(String preferredlastname, String preferredfirstname);
}
