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

		copyFileFromClasspathToInngaaende("testdata.xml");

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
