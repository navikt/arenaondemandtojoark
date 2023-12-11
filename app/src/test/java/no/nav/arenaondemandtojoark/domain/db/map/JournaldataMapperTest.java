package no.nav.arenaondemandtojoark.domain.db.map;

import no.nav.arenaondemandtojoark.exception.JournaldataMappingException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldata;
import static no.nav.arenaondemandtojoark.TestUtils.lagXmlJournaldata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JournaldataMapperTest {

	public static final String UGYLDIG_TIDSPUNKT = "2008-03-10 17:19:22";
	private static final String FILNAVN = "journaldata.xml";

	private final JournaldataMapper journaldataMapper = new JournaldataMapper();
	private Exchange exchange;

	@BeforeEach
	void beforeEach() {
		CamelContext context = new DefaultCamelContext();
		exchange = new DefaultExchange(context);
		exchange.setProperty(PROPERTY_FILNAVN, "journaldata.xml");
	}

	@Test
	void skalMappe() {
		var xmlJournaldata = lagXmlJournaldata();
		var expected = lagJournaldata();

		var actual = journaldataMapper.map(xmlJournaldata, exchange);

		assertThat(actual).usingRecursiveComparison()
				.ignoringFields("status", "filnavn").isEqualTo(expected);
		assertThat(actual.getFilnavn()).isEqualTo(FILNAVN);
	}

	@Test
	void skalMappeTilNullForEnumOgLocalDateTimeLikNull() {
		var xmlJournaldata = lagXmlJournaldata();

		xmlJournaldata.setJournalpostType(null);
		xmlJournaldata.setFagomraade(null);
		xmlJournaldata.setUtsendingskanal(null);
		xmlJournaldata.setDokumentkategori(null);
		xmlJournaldata.setJournaldato(null);
		xmlJournaldata.setSendtPrintDato(null);

		var resultat = journaldataMapper.map(xmlJournaldata, exchange);

		assertThat(resultat)
				.extracting("journalposttype",
						"fagomraade",
						"utsendingskanal",
						"dokumentkategori",
						"journaldato",
						"sendtPrintDato")
				.allSatisfy(verdi -> assertThat(verdi).isNull());
	}

	@Test
	void skalIkkeMappeUgyldigJournalpostType() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setJournalpostType("UGYLDIG_JOURNALPOSTTYPE");

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for journalposttype=UGYLDIG_JOURNALPOSTTYPE");
	}

	@Test
	void skalIkkeMappeUgyldigFagomraade() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setFagomraade("UGYLDIG_FAGOMRAADE");

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for fagomraade=UGYLDIG_FAGOMRAADE");
	}

	@Test
	void skalIkkeMappeUgyldigUtsendingskanal() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setUtsendingskanal("UGYLDIG_UTSENDINGSKANAL");

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for utsendingskanal=UGYLDIG_UTSENDINGSKANAL");
	}

	@Test
	void skalIkkeMappeUgyldigDokumentkategori() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setDokumentkategori("UGYLDIG_DOKUMENTKATEGORI");

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for dokumentkategori=UGYLDIG_DOKUMENTKATEGORI");
	}

	@Test
	void skalIkkeMappeUgyldigJournaldato() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setJournaldato(UGYLDIG_TIDSPUNKT);

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for journaldato=2008-03-10 17:19:22");
	}

	@Test
	void skalIkkeMappeUgyldigSendtPrintDato() {
		var xmlJournaldata = lagXmlJournaldata();
		xmlJournaldata.setSendtPrintDato(UGYLDIG_TIDSPUNKT);

		assertThatExceptionOfType(JournaldataMappingException.class)
				.isThrownBy(() -> journaldataMapper.map(xmlJournaldata, exchange))
				.withMessageContaining("Kan ikke mappe til journaldata. Ugyldig verdi for sendtprintdato=2008-03-10 17:19:22");
	}

}