package uk.gov.justice.digital.prisoneraccounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class PrisonerAccounts {

    public static void main(String[] args) {
        SpringApplication.run(PrisonerAccounts.class, args);
    }

}
