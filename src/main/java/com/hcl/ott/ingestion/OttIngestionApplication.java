package com.hcl.ott.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OttIngestionApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(OttIngestionApplication.class, args);
    }

}
