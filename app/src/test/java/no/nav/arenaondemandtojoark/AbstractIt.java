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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_SHUTDOWN;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
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

	public static final String HENT_ONDEMAND_DOKUMENT_URL = "/ODBrevServlet?IDNR=%s&appID=AREQ1";
	public static final String OPPRETT_JOURNALPOST_URL = "/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=false";
	public static final String FERDIGSTILL_JOURNALPOST_URL = "/rest/journalpostapi/v1/journalpost/%s/ferdigstill";

	private static final String SCENARIO_STATE_2_ONDEMAND = "500FraOndemandFoersteGang";
	private static final String SCENARIO_STATE_3_ONDEMAND = "500FraOndemandAndreGang";
	private static final String SCENARIO_STATE_2_DOKARKIV = "500FraDokarkivFoersteGang";
	private static final String SCENARIO_STATE_3_DOKARKIV = "500FraDokarkivAndreGang";

	@Autowired
	public Path sshdPath;

	@BeforeEach
	void setup() throws Exception {
		var inbound = sshdPath.resolve("inbound");
		var outbound = sshdPath.resolve("outbound");
		preparePath(inbound);
		preparePath(outbound);
		stubAzure();

		journaldataRepository.deleteAll();
		avvikRepository.deleteAll();

		// mock ut shutdown så appen ikke skrur seg av før testen er ferdig
		mockEndpointAndSkipAt("start_operation", RUTE_SHUTDOWN);
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

	void stubHentOndemandDokumentMedStatusBadRequest(String ondemandId) {
		stubFor(get(HENT_ONDEMAND_DOKUMENT_URL.formatted(ondemandId))
				.willReturn(aResponse()
						.withStatus(BAD_REQUEST.value())));
	}

	void stubHentOndemandDokumentMedStatus(HttpStatus status) {
		stubFor(get(urlPathEqualTo("/ODBrevServlet"))
				.withQueryParam("IDNR", matching(".*"))
				.withQueryParam("appID", equalTo("AREQ1"))
				.willReturn(aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	void stubHentDokumentMedStatusInternalServerErrorToGanger(String ondemandId) {
		var SCENARIO_500_FRA_ONDEMAND = "500FraHentDokumentFraOndemand";

		stubFor(get(HENT_ONDEMAND_DOKUMENT_URL.formatted(ondemandId))
				.inScenario(SCENARIO_500_FRA_ONDEMAND)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_2_ONDEMAND));

		stubFor(get(HENT_ONDEMAND_DOKUMENT_URL.formatted(ondemandId))
				.inScenario(SCENARIO_500_FRA_ONDEMAND)
				.whenScenarioStateIs(SCENARIO_STATE_2_ONDEMAND)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_3_ONDEMAND));

		stubFor(get(HENT_ONDEMAND_DOKUMENT_URL.formatted(ondemandId))
				.inScenario(SCENARIO_500_FRA_ONDEMAND)
				.whenScenarioStateIs(SCENARIO_STATE_3_ONDEMAND)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBodyFile("ondemand/ODIQ613100900011.pdf")));
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
						.withStatus(CONFLICT.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("journalpost/happyresponse.json")));
	}

	void stubOpprettJournalpostMedStatusBadRequest() {
		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.willReturn(aResponse()
						.withStatus(BAD_REQUEST.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	void stubOpprettJournalpostMedStatusInternalServerErrorToGanger() {
		var SCENARIO_500_FRA_OPPRETT_JOURNALPOST = "500FraOpprettJournalpost";

		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.inScenario(SCENARIO_500_FRA_OPPRETT_JOURNALPOST)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_2_DOKARKIV));

		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.inScenario(SCENARIO_500_FRA_OPPRETT_JOURNALPOST)
				.whenScenarioStateIs(SCENARIO_STATE_2_DOKARKIV)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_3_DOKARKIV));

		stubFor(post(OPPRETT_JOURNALPOST_URL)
				.inScenario(SCENARIO_500_FRA_OPPRETT_JOURNALPOST)
				.whenScenarioStateIs(SCENARIO_STATE_3_DOKARKIV)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("journalpost/happyresponse.json")));
	}

	void stubFerdigstillJournalpost(String journalpostId) {
		stubFor(patch(urlPathEqualTo(FERDIGSTILL_JOURNALPOST_URL.formatted(journalpostId)))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("Journalpost ferdigstilt")));
	}

	void stubFerdigstillJournalpostMedStatusInternalServerErrorToGanger(String journalpostId) {
		var SCENARIO_500_FRA_FERDIGSTILL_JOURNALPOST = "500FraFerdigstillJournalpost";

		stubFor(patch(urlPathEqualTo(FERDIGSTILL_JOURNALPOST_URL.formatted(journalpostId)))
				.inScenario(SCENARIO_500_FRA_FERDIGSTILL_JOURNALPOST)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_2_DOKARKIV));

		stubFor(patch(urlPathEqualTo(FERDIGSTILL_JOURNALPOST_URL.formatted(journalpostId)))
				.inScenario(SCENARIO_500_FRA_FERDIGSTILL_JOURNALPOST)
				.whenScenarioStateIs(SCENARIO_STATE_2_DOKARKIV)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value()))
				.willSetStateTo(SCENARIO_STATE_3_DOKARKIV));

		stubFor(patch(urlPathEqualTo(FERDIGSTILL_JOURNALPOST_URL.formatted(journalpostId)))
				.inScenario(SCENARIO_500_FRA_FERDIGSTILL_JOURNALPOST)
				.whenScenarioStateIs(SCENARIO_STATE_3_DOKARKIV)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("Journalpost ferdigstilt")));
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
		copy(new ClassPathResource(zipfilename).getInputStream(), sshdPath.resolve("inbound").resolve(zipfilename));
	}

}