package frontend;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import brave.Tracer;
import brave.Tracing;
import brave.http.HttpTracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class HelloController {
	@Autowired
	private YAMLConfig myConfig;
	private static final Logger LOG = Logger.getLogger(HelloController.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	Tracer tracer;

	@Bean
	HttpTracing httpTracing(final Tracing tracing) {
		return HttpTracing.create(tracing);
	}

	@RequestMapping("/")
	public String index(@RequestHeader Map<String, String> headers) throws UnknownHostException {
		headers.forEach((key,value) ->{
            LOG.info("Header Name: "+key+" Header Value: "+value);
        });
		return String.format("Frontend %s, using backend: %s", 
			InetAddress.getLocalHost().getHostAddress(),
			myConfig.getBackend());
	}

	@RequestMapping("/callbackend") public String callBackendPath(@RequestParam String path) { 
		LOG.info("Calling backend");
		String callPath = String.format("%s/%s", myConfig.getBackend(), path);
		return restTemplate.getForObject(callPath, String.class); 
	}
}
