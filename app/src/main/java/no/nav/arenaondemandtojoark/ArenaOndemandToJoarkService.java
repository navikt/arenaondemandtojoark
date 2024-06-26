package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.DokarkivConsumer;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostResponse;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.FerdigstillJournalpostRequestMapper;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.OpprettJournalpostRequestMapper;
import no.nav.arenaondemandtojoark.consumer.ondemandbrev.OndemandBrevConsumer;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.exception.DokarkivNonRetryableException;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.Handler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;

@Slf4j
@Service
public class ArenaOndemandToJoarkService {

	private final OndemandBrevConsumer ondemandBrevConsumer;
	private final DokarkivConsumer dokarkivConsumer;
	private final JournaldataRepository journaldataRepository;
	private static final byte[] dummypdf;

	public ArenaOndemandToJoarkService(OndemandBrevConsumer ondemandBrevConsumer,
									   DokarkivConsumer dokarkivConsumer,
									   JournaldataRepository journaldataRepository) {
		this.ondemandBrevConsumer = ondemandBrevConsumer;
		this.dokarkivConsumer = dokarkivConsumer;
		this.journaldataRepository = journaldataRepository;
	}

	static {
		byte[] pdf;
		try {
			Resource resource = new ClassPathResource("/MigreringMislyktes.pdf");
			pdf = FileCopyUtils.copyToByteArray(resource.getInputStream());
		} catch (IOException e) {
			pdf = null;
		}
		dummypdf = pdf;
	}

	@Handler
	public void prosesserJournaldata(Journaldata journaldata) {

		log.info("Starter prosessering av journaldata med onDemandId={}", journaldata.getOnDemandId());

		byte[] pdfDocument = hentDokument(journaldata.getOnDemandId());

		OpprettJournalpostRequest journalpost = OpprettJournalpostRequestMapper.map(journaldata, pdfDocument);
		OpprettJournalpostResponse opprettJournalpostResponse = dokarkivConsumer.opprettJournalpost(journalpost);

		if (opprettJournalpostResponse.dokumenter().size() != 1)
			throw new DokarkivNonRetryableException("Forventet akkurat ett dokument i opprettJournalpostResponse");

		var journalpostId = opprettJournalpostResponse.journalpostId();
		var dokumentInfoId = opprettJournalpostResponse.dokumenter().get(0).dokumentInfoId();

		log.info("Har opprettet journalpost for journaldata med onDemandId={}, journalpostId={} og dokumentInfoId={}",
				journaldata.getOnDemandId(), journalpostId, dokumentInfoId);

		FerdigstillJournalpostRequest ferdigstillJournalpostRequest = FerdigstillJournalpostRequestMapper.map(journaldata);
		dokarkivConsumer.ferdigstillJournalpost(journalpostId, ferdigstillJournalpostRequest);

		log.info("Har ferdigstilt journalpost for journaldata med onDemandId={}, journalpostId={} og dokumentInfoId={}",
				journaldata.getOnDemandId(), journalpostId, dokumentInfoId);

		log.info("Har prosessert ferdig journaldata med onDemandId={}", journaldata.getOnDemandId());

		journaldata.setJournalpostId(journalpostId);
		journaldata.setDokumentInfoId(dokumentInfoId);
		journaldata.setStatus(PROSESSERT);

		if (journaldata.getAvvik() != null) {
			journaldata.setAvvik(null);
		}

		journaldataRepository.save(journaldata);
	}

	private byte[] hentDokument(String ondemandId) {
		try {
			return ondemandBrevConsumer.hentPdf(ondemandId);
		} catch (Exception e) {
			log.info("Fant ikke dokument for onDemandId={}, bruker dummyPdf", ondemandId);
			return dummypdf;
		}
	}
}
