package playground.leones.leonesarc.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import playground.leones.leonesarc.dto.ServiceInfo;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class ServiceControl {

    ConcurrentHashMap<ServiceInfo, Boolean> services = new ConcurrentHashMap<>();

    private String getClientServerIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }
        return ipAddress;
    }

    private int getClientServerPort(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-Forwarded-Port"));
    }

    @GetMapping("/register")
    public ResponseEntity<Void> serviceCheck(HttpServletRequest request) {
        String clientIp = getClientServerIp(request);
        int clientPort = getClientServerPort(request);

        ServiceInfo serviceInfo = new ServiceInfo(clientIp, clientPort);
        services.put(serviceInfo, true);

        log.debug(services.toString());

        return ResponseEntity.noContent().build();
    }

}
