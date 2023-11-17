package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("itest")
public class XmlTest {

	@Test
	void shouldUnmarshallXml() throws JAXBException, FileNotFoundException {

		FileReader fileReader = new FileReader("src/test/resources/testdata.xml");
		JAXBContext context = JAXBContext.newInstance(Innlasting.class);

		var result = (Innlasting) context.createUnmarshaller()
				.unmarshal(fileReader);

		assertThat(result.getInnlasting().size()).isEqualTo(3);
	}
}
