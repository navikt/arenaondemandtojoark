package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.Handler;

public class JournaldataService {

	private final JournaldataRepository journaldataRepository;

	public JournaldataService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	@Handler
	public void lagreJournaldata(Journaldata journaldata) {
//		no.nav.arenaondemandtojoark.domain.db.Journaldata journaldata1 = mapToJournaldataEntity(journaldata);
//
//		journaldataRepository.save(journaldata1);
	}
}
