package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("itest")
public class XmlTest {

	@Test
	void shouldUnmarshallXml() throws JAXBException, FileNotFoundException {

		FileReader fileReader = new FileReader("src/test/resources/journaldata.xml");
		JAXBContext context = JAXBContext.newInstance(Innlasting.class);

		var result = (Innlasting) context.createUnmarshaller()
				.unmarshal(fileReader);

		assertThat(result.getJournaldataList().size()).isEqualTo(3);
	}

	@Test
	void shouldMarshallXml() throws JAXBException {

		var element1 = new JournalpostrapportElement("123", "456", "789");
		var element2 = new JournalpostrapportElement("aaa", "bbb", "ccc");
		var element3 = new JournalpostrapportElement("***", "---", "###");

		var liste = new Journalpostrapport();
		liste.setJournalpostList(List.of(element1, element2, element3));

		JAXBContext context = JAXBContext.newInstance(Journalpostrapport.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(liste, System.out);

	}
}
