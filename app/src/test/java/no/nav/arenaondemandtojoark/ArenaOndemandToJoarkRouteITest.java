package no.nav.arenaondemandtojoark;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata-ett-element.xml"}
)
public class ArenaOndemandToJoarkRouteITest extends AbstractIt {

	@Value("${arenaondemandtojoark.filnavn}")
	String filnavn;

}
