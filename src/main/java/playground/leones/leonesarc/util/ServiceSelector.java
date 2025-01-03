package playground.leones.leonesarc.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import playground.leones.leonesarc.dto.ServiceInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@Slf4j
public class ServiceSelector {

    int counter = 0;

    public Optional<ServiceInfo> randomService(ConcurrentHashMap<ServiceInfo, Instant> services) {
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

    public synchronized Optional<ServiceInfo> roundRobin(ConcurrentHashMap<ServiceInfo, Instant> services) {
        List<ServiceInfo> serviceList = new ArrayList<>(services.keySet());

        log.debug("Selected service index: {}", counter);
        counter = (counter + 1) % services.size();
        return Optional.of(serviceList.get(counter));
    }

}
