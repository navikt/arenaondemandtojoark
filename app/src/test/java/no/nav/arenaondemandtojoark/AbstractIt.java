package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.repository.AvvikRepository;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static wiremock.org.apache.commons.io.FileUtils.cleanDirectory;

@Slf4j
@SpringBootTest(
		classes = TestConfig.class,
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
@Transactional
@CamelSpringBootTest
@DirtiesContext
public abstract class AbstractIt {

	@Autowired
	public JournaldataRepository journaldataRepository;

	@Autowired
	public AvvikRepository avvikRepository;


	@Autowired
	private CamelContext camelContext;

	private static final String OPPRETT_JOURNALPOST_URL = "/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false";
	private static final String FERDIGSTILL_JOURNALPOST_URL = "/rest/journalpostapi/v1/journalpost/%s/ferdigstill";
	private static final String HENT_ONDEMAND_DOKUMENT_URL = "/ODBrevServlet?IDNR=%s&appID=AREP1";

	@Autowired
	public Path sshdPath;

	@BeforeEach
	void setup() throws Exception {

		preparePath(sshdPath);
		stubAzure();

		journaldataRepository.deleteAll();
		avvikRepository.deleteAll();

		// mock ut shutdown så appen ikke skrur seg av før testen er ferdig
		mockEndpointAndSkipAt("start_operation", "direct:shutdown");
	}

	public void mockEndpointAndSkipAt(String routeId, String endpointUri) throws Exception {
		AdviceWith.adviceWith(camelContext, routeId, a -> a.mockEndpointsAndSkip(endpointUri));
		camelContext.getEndpoint("mock:" + endpointUri, MockEndpoint.class);
	}


	public void commitAndBeginNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	void stubHentOndemandDokument(String ondemandId) {
		stubFor(get(HENT_ONDEMAND_DOKUMENT_URL.formatted(ondemandId))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBodyFile("ondemand/ODIQ613100900011.pdf")));
	}

	void stubHentOndemandDokumentMedStatus(HttpStatus status) {
		stubFor(get(urlPathEqualTo("/ODBrevServlet"))
				.withQueryParam("IDNR", matching(".*"))
				.withQueryParam("appID", equalTo("AREP1"))
				.willReturn(aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	void stubFerdigstillJournalpost(String journalpostId) {
		stubFor(patch(urlPathEqualTo(FERDIGSTILL_JOURNALPOST_URL.formatted(journalpostId)))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("Journalpost ferdigstilt")));
	}

	void stubOpprettJournalpost() {
		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("journalpost/happyresponse.json")));
	}

	void stubOpprettJournalpostMedStatusConflict() {
		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.willReturn(aResponse()
						.withStatus(HttpStatus.CONFLICT.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("journalpost/happyresponse.json")));
	}

	void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	void preparePath(Path path) throws IOException {
		if (!exists(path)) {
			createDirectory(path);
		} else {
			cleanDirectory(path.toFile());
		}
	}

	void copyFileFromClasspathToInngaaende(final String zipfilename, Path sshdPath) throws IOException {
		copy(new ClassPathResource(zipfilename).getInputStream(), sshdPath.resolve(zipfilename));
	}

}