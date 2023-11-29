package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@Transactional
public class JournaldataService {

	private final JournaldataRepository journaldataRepository;

	public JournaldataService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	public void lagreJournaldata(ArrayList<Journaldata> journaldata) {
		log.info("Lagrer journaldataliste med ondemandId={}", journaldata.stream().map(Journaldata::getOnDemandId).toList());

		journaldataRepository.saveAll(journaldata);
	}

	public void lagJournalpostrapport(String filnavn) {
		journaldataRepository.getAllByFilnavn(filnavn);
	}
}
