package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;

@Slf4j
@Service
@Transactional
public class JournaldataService {

	private final JournaldataRepository journaldataRepository;

	public JournaldataService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	public Iterable<Journaldata> lagreJournaldata(ArrayList<Journaldata> journaldata) {
		log.info("Lagrer journaldataliste med ondemandId={}", journaldata.stream().map(Journaldata::getOnDemandId).toList());

		return journaldataRepository.saveAll(journaldata);
	}

	public List<Journaldata> hentJournaldata(String filnavn) {
		log.info("Henter journaldataliste med filnavn={}", filnavn);

		return journaldataRepository.getAllByFilnavnAndStatus(filnavn, INNLEST);
	}

	public List<Rapportelement> lagJournalpostrapport(String filnavn) {
		log.info("Henter journalpostrapportelement-liste for filnavn={}", filnavn);

		return journaldataRepository.getRapportdataByFilnavnAndStatus(filnavn, PROSESSERT);
	}
}
