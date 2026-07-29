package com.williamrocha.codearena;

import org.springframework.boot.SpringApplication;

public class TestCodeArenaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CodeArenaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
