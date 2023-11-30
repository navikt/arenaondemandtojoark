package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class JournaldataService {

	private static final String STATUS_INNLEST = "INNLEST";
	private static final String STATUS_PROSESSERT = "PROSESSERT";

	private final JournaldataRepository journaldataRepository;

	public JournaldataService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	public void lagreJournaldata(ArrayList<Journaldata> journaldata) {
		log.info("Lagrer journaldataliste med ondemandId={}", journaldata.stream().map(Journaldata::getOnDemandId).toList());

		journaldataRepository.saveAll(journaldata);
	}

	public void hentJournaldata(String filnavn) {
		log.info("Henter journaldataliste med filnavn={}", filnavn);

		journaldataRepository.getAllByFilnavnAndStatus(filnavn, STATUS_INNLEST);
	}

	public List<Rapportelement> lagJournalpostrapport(String filnavn) {
		log.info("Henter journalpostrapportelement-liste for filnavn={}", filnavn);

		return journaldataRepository.getRapportdataByFilnavnAndStatus(filnavn, STATUS_PROSESSERT);
	}
}
