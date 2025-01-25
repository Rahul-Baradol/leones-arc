package playground.leones.leonesarc.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import playground.leones.leonesarc.dto.ServiceInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ServiceSelectorService {

    int counter = 0;

    @Value("${serviceSelectionAlgorithm}")
    private String serviceSelectionAlgorithm;

    @PostConstruct
    public void init() {
        log.debug("Choosing {}", serviceSelectionAlgorithm);
    }

    private Optional<ServiceInfo> randomService(ConcurrentHashMap<ServiceInfo, Instant> services) {
        List<ServiceInfo> serviceList = new ArrayList<>(services.keySet());

        Random random = new Random();

        int count = 0;

        while (count <= 10) {
            int serviceIndex = random.nextInt(serviceList.size());
            ServiceInfo service = serviceList.get(serviceIndex);

            Instant currentInstant = Instant.now();
            Instant lastContactInstant = services.get(service);
            Duration duration =  Duration.between(currentInstant, lastContactInstant);
            if (Math.abs(duration.getSeconds()) <= 10) {
                return Optional.of(service);
            }
            count++;
        }

        return Optional.empty();
    }

    private synchronized Optional<ServiceInfo> roundRobin(ConcurrentHashMap<ServiceInfo, Instant> services) {
        List<ServiceInfo> serviceList = new ArrayList<>(services.keySet());
        counter = (counter + 1) % services.size();
        return Optional.of(serviceList.get(counter));
    }

    public Optional<ServiceInfo> select(ConcurrentHashMap<ServiceInfo, Instant> services) {
        return switch (serviceSelectionAlgorithm) {
            case "random" -> randomService(services);
            default -> roundRobin(services);
        };
    }

}
