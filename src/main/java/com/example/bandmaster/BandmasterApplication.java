package com.example.bandmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class BandmasterApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // Não quebra em produção (onde usaremos variáveis reais)
				.systemProperties()
				.load();
		SpringApplication.run(BandmasterApplication.class, args);
	}

}
