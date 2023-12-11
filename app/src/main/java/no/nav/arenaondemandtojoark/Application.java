package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.config.AzureTokenProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.springframework.boot.WebApplicationType.NONE;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties({
		ArenaondemandtojoarkProperties.class,
		AzureTokenProperties.class
})
public class Application {
	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.web(NONE)
				.run(args);
	}
}
