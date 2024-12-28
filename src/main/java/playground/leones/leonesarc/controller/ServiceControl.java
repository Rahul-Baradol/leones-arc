package playground.leones.leonesarc.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import playground.leones.leonesarc.dto.ServiceInfo;
import playground.leones.leonesarc.util.ServiceSelector;

import java.time.Instant;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class ServiceControl {

    ConcurrentHashMap<ServiceInfo, Instant> services = new ConcurrentHashMap<>();

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

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }
        return headers;
    }

    @GetMapping("/register")
    public ResponseEntity<Void> serviceCheck(HttpServletRequest request) {
        String clientIp = getClientServerIp(request);
        int clientPort = getClientServerPort(request);

        ServiceInfo serviceInfo = new ServiceInfo(clientIp, clientPort);
        services.put(serviceInfo, Instant.now());
        log.debug(serviceInfo.toString());

        return ResponseEntity.noContent().build();
    }

    @RequestMapping("/api/**")
    public ResponseEntity<?> forwardRequest(HttpServletRequest request,
                                            @RequestBody(required = false) String body) {
        try {
            Optional<ServiceInfo> service = ServiceSelector.roundRobin(services);
            if (service.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }

            String SERVICE_BASE_URL = service.get().getServiceIp() + ":" + service.get().getServicePort();

            String dynamicPath = request.getRequestURI().substring("/api".length());

            String targetUrl = "http://" + SERVICE_BASE_URL + dynamicPath;

            HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
            HttpHeaders headers = extractHeaders(request);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);

            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
        } catch (HttpClientErrorException httpClientErrorException) {
            return new ResponseEntity<>(httpClientErrorException.getResponseBodyAsString(), httpClientErrorException.getStatusCode());
        } catch (HttpServerErrorException httpServerErrorException) {
            return new ResponseEntity<>(httpServerErrorException.getMessage(), httpServerErrorException.getStatusCode());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body("Error forwarding request: " + e.getMessage());
        }
    }

}
