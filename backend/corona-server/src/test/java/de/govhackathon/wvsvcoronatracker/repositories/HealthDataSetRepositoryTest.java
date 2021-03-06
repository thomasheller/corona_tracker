package de.govhackathon.wvsvcoronatracker.repositories;

import de.govhackathon.wvsvcoronatracker.model.Contact;
import de.govhackathon.wvsvcoronatracker.model.HealthDataSet;
import de.govhackathon.wvsvcoronatracker.model.MedicalState;
import de.govhackathon.wvsvcoronatracker.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class HealthDataSetRepositoryTest {

    private final static String DEFAULT_TOKEN = "standard";

    private final static String DEFAULT_PHONE_HASH= "hash";

    private User defaultUser;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HealthDataSetRepository repository;

    @Autowired
    private UserRepository userRepository;


    public void beforeEach(){
        entityManager.clear();
        User user = new User();
        user.setToken(DEFAULT_TOKEN);
        user.setContactDetails(Contact.builder().phoneHash("hash").build());
        defaultUser = entityManager.persist(user);
    }


    @Test
    public void testFindByMedicalStateAndCreationDateBetween(){
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime earlier = now.minusDays(2);
        OffsetDateTime later = now.plusDays(3);
        OffsetDateTime earliest = now.minusDays(10);

        HealthDataSet healthDataSet1 = createByStateAndTimeStamp(MedicalState.INFECTED,now);
        HealthDataSet healthDataSet2 = createByStateAndTimeStamp(MedicalState.INFECTED,earlier);
        HealthDataSet healthDataSet3 = createByStateAndTimeStamp(MedicalState.INFECTED,later);
        HealthDataSet healthDataSet4 = createByStateAndTimeStamp(MedicalState.INFECTED,earliest);

        List<HealthDataSet> actual = repository.findByMedicalStateAndCreationDateBetween(MedicalState.INFECTED,earlier,later);

        Assertions.assertThat(actual).contains(healthDataSet1,healthDataSet2,healthDataSet3).doesNotContain(healthDataSet4);
    }



    @Test
    public void testFindByUserAndCreationDateBetween(){
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime earlier = now.minusDays(2);
        OffsetDateTime later = now.plusDays(3);
        OffsetDateTime earliest = now.minusDays(10);

        HealthDataSet healthDataSet1 = createByUserIdAndTimeStamp(DEFAULT_TOKEN,MedicalState.INFECTED,now);
        HealthDataSet healthDataSet2 = createByUserIdAndTimeStamp(DEFAULT_TOKEN,MedicalState.INFECTED,earlier);
        HealthDataSet healthDataSet3 = createByUserIdAndTimeStamp("wrong-id",MedicalState.INFECTED,later);
        HealthDataSet healthDataSet4 = createByUserIdAndTimeStamp(DEFAULT_TOKEN,MedicalState.INFECTED,earliest);



        List<HealthDataSet> actual = repository.findByUserAndMedicalStateAndCreationDateBetween(defaultUser,MedicalState.INFECTED,earlier,later);

        Assertions.assertThat(actual).contains(healthDataSet1,healthDataSet2).doesNotContain(healthDataSet3,healthDataSet4);
    }


    private HealthDataSet createByStateAndTimeStamp(MedicalState state, OffsetDateTime timestamp){
        HealthDataSet healthDataSet = new HealthDataSet();
        healthDataSet.setCreationDate(timestamp);
        healthDataSet.setMedicalState(state);
        entityManager.persist(healthDataSet);
        return healthDataSet;
    }

    private HealthDataSet createByUserIdAndTimeStamp(String userId,MedicalState state, OffsetDateTime timestamp){
        User user = defaultUser;
        if(!userId.equals(DEFAULT_TOKEN)) {
            user = new User();
            user.setToken(userId);
            user.setContactDetails(Contact.builder().phoneHash("hash").build());
            user = entityManager.persist(user);
        }
        HealthDataSet healthDataSet = new HealthDataSet();
        healthDataSet.setCreationDate(timestamp);
        healthDataSet.setMedicalState(state);
        healthDataSet.setUser(user);
        entityManager.persist(healthDataSet);
        return healthDataSet;
    }
}
