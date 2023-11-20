package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.DokarkivConsumer;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostResponse;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.FerdigstillJournalpostRequestMapper;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.OpprettJournalpostRequestMapper;
import no.nav.arenaondemandtojoark.consumer.ondemandbrev.OndemandBrevConsumer;
import no.nav.arenaondemandtojoark.domain.journaldata.Journaldata;
import no.nav.arenaondemandtojoark.exception.OndemandDokumentIkkeFunnetException;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class ArenaOndemandToJoarkService {

	private final OndemandBrevConsumer ondemandBrevConsumer;
	private final DokarkivConsumer dokarkivConsumer;
	private static final byte[] dummypdf;

	public ArenaOndemandToJoarkService(OndemandBrevConsumer ondemandBrevConsumer,
									   DokarkivConsumer dokarkivConsumer) {
		this.ondemandBrevConsumer = ondemandBrevConsumer;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	static {
		byte[] pdf;
		try {
			File resource = new ClassPathResource("/MigreringMisslyktes.pdf").getFile();
			pdf = Files.readAllBytes(resource.toPath());
		} catch (IOException e) {
			pdf = null;
		}
		dummypdf = pdf;
	}

	@Handler
	public void processJournaldata(@Body Journaldata journaldata) {
		byte[] pdfDocument = hentDokument(journaldata.getOnDemandId());

		OpprettJournalpostRequest journalpost = OpprettJournalpostRequestMapper.map(journaldata, pdfDocument);
		OpprettJournalpostResponse response = dokarkivConsumer.opprettJournalpost(journalpost);

		FerdigstillJournalpostRequest ferdigstillJournalpostRequest = FerdigstillJournalpostRequestMapper.map(journaldata);

		dokarkivConsumer.ferdigstillJournalpost(response.journalpostId(), ferdigstillJournalpostRequest);
	}

	private byte[] hentDokument(String ondemandId) {
		try {
			return ondemandBrevConsumer.hentPdf(ondemandId);
		} catch (OndemandDokumentIkkeFunnetException e) {
			return dummypdf;
		}
	}
}
