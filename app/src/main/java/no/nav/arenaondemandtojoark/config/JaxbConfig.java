package no.nav.arenaondemandtojoark.config;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

@Configuration
public class JaxbConfig {

	@Bean
	public JaxbDataFormat rapporteringDataFormat() throws JAXBException {
		var jaxbDataFormat = new JaxbDataFormat(JAXBContext.newInstance(Journalpostrapport.class));
		jaxbDataFormat.setEncoding(ISO_8859_1.name());

		return jaxbDataFormat;
	}
}
