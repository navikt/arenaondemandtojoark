package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVLEVERT;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;

@Slf4j
@Service
@Transactional
public class JournaldataService {

	private static final List<JournaldataStatus> HENT_JOURNALDATA_STATUSER = List.of(INNLEST, AVVIK);

	private final JournaldataRepository journaldataRepository;

	public JournaldataService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	public void lagreJournaldata(ArrayList<Journaldata> journaldata) {
		log.info("Lagrer {} journaldata", journaldata.size());

		journaldataRepository.saveAll(journaldata);
	}

	public List<Journaldata> hentJournaldata(String filnavn) {
		log.info("Henter journaldataliste med filnavn={}", filnavn);

		List<Journaldata> result = journaldataRepository.getAllByFilnavnAndStatuses(filnavn, HENT_JOURNALDATA_STATUSER);
		log.info("Hentet {} journaldata-elementer med filnavn={}", result.size(), filnavn);

		return result;
	}

	public void lagOppsummering(String filnavn) {
		var antallInnlest = journaldataRepository.countJournaldataByFilnavnAndStatus(filnavn, INNLEST);
		var antallProsessert = journaldataRepository.countJournaldataByFilnavnAndStatus(filnavn, PROSESSERT);
		var antallAvvik = journaldataRepository.countJournaldataByFilnavnAndStatus(filnavn, AVVIK);
		var antallAvlevert = journaldataRepository.countJournaldataByFilnavnAndStatus(filnavn, AVLEVERT);

		log.info("Oppsummering for fil={}: {}={}, {}={}, {}={}, {}={}", filnavn,
				INNLEST, antallInnlest,
				PROSESSERT, antallProsessert,
				AVVIK, antallAvvik,
				AVLEVERT, antallAvlevert);
	}

	public List<Rapportelement> lagJournalpostrapport(String filnavn) {
		log.info("Henter journalpostrapportelement-liste for filnavn={}", filnavn);

		List<Rapportelement> result = journaldataRepository.getRapportdataByFilnavnAndStatus(filnavn, PROSESSERT);
		log.info("Hentet {} journalpostrapportelement med filnavn={}", result.size(), filnavn);

		return result;
	}

	public void oppdaterStatusTilAvlevert(String filnavn) {
		log.info("Oppdaterer status til AVLEVERT for filnavn={}", filnavn);

		journaldataRepository.updateStatusToAvlevert(filnavn);
	}
}
