package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

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

		var liste = new Journalpostrapport(List.of(element1, element2, element3));

		JAXBContext context = JAXBContext.newInstance(Journalpostrapport.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
		marshaller.marshal(liste, System.out);
	}
}
