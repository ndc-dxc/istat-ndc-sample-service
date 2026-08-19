package it.istat.ndc.sample;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The whole service. /health is what the probes and the pipeline's smoke test call; it echoes
 * the environment and the image tag so that a deploy can be told apart from the one before it.
 */
@RestController
public class HealthController {

    @Value("${ENVIRONMENT:unknown}")
    private String environment;

    @Value("${IMAGE_TAG:unknown}")
    private String imageTag;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "sample-service",
            "environment", environment,
            "imageTag", imageTag);
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return health();
    }
}
