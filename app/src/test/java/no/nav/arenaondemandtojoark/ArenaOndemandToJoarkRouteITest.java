package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import wiremock.org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

public class ArenaOndemandToJoarkRouteITest extends AbstractIt {

	@Autowired
	private Path sshdPath;

	@BeforeEach
	void beforeEach() throws IOException {
		preparePath(sshdPath);
	}

	@Test
	public void skalLeseFraFilomraade() throws IOException {
		var ondemandId = "ODAP08031000123";
		var journalpostId = "467010363";

		stubAzure();
		stubHentOndemandDokument(ondemandId);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(journalpostId);

		copyFileFromClasspathToInngaaende("journaldata-ett-element.xml");

		await().atMost(15, SECONDS).untilAsserted(() -> {
			fail();
		});
	}

	@Test
	public void skalLeseFraFilomraadeMedFlereFiler() throws IOException {
		var ondemandId = "ODAP08031000123";
		var journalpostId = "467010363";
		var ondemandId2 = "ODAP08031000456";
		var ondemandId3 = "ODAP08031000789";

		stubAzure();
		stubHentOndemandDokument(ondemandId);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(journalpostId);
		stubHentOndemandDokument(ondemandId2);
		stubHentOndemandDokument(ondemandId3);

		copyFileFromClasspathToInngaaende("journaldata.xml");

		await().atMost(15, SECONDS).untilAsserted(() -> {
			fail();
		});
	}

	private void preparePath(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		} else {
			FileUtils.cleanDirectory(path.toFile());
		}
	}

	private void copyFileFromClasspathToInngaaende(final String zipfilename) throws IOException {
		Files.copy(new ClassPathResource(zipfilename).getInputStream(), sshdPath.resolve(zipfilename));
	}

}
