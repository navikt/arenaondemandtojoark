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
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import no.nav.arenaondemandtojoark.exception.DokarkivFunctionalException;
import no.nav.arenaondemandtojoark.exception.OndemandDokumentIkkeFunnetException;
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
			File resource = new ClassPathResource("/MigreringMislyktes.pdf").getFile();
			pdf = Files.readAllBytes(resource.toPath());
		} catch (IOException e) {
			pdf = null;
		}
		dummypdf = pdf;
	}

	@Handler
	public JournalpostrapportElement prosesserJournaldata(Journaldata journaldata) {
		byte[] pdfDocument = hentDokument(journaldata.getOnDemandId());

		OpprettJournalpostRequest journalpost = OpprettJournalpostRequestMapper.map(journaldata, pdfDocument);
		OpprettJournalpostResponse opprettJournalpostResponse = dokarkivConsumer.opprettJournalpost(journalpost);

		if (opprettJournalpostResponse.dokumenter().size() != 1)
			throw new DokarkivFunctionalException("Forventet akkurat ett dokument i opprettJournalpostResponse");

		FerdigstillJournalpostRequest ferdigstillJournalpostRequest = FerdigstillJournalpostRequestMapper.map(journaldata);

		dokarkivConsumer.ferdigstillJournalpost(opprettJournalpostResponse.journalpostId(), ferdigstillJournalpostRequest);

		return new JournalpostrapportElement(opprettJournalpostResponse.journalpostId(),
				opprettJournalpostResponse.dokumenter().get(0).dokumentInfoId(),
				journaldata.getOnDemandId());
	}

	private byte[] hentDokument(String ondemandId) {
		try {
			return ondemandBrevConsumer.hentPdf(ondemandId);
		} catch (OndemandDokumentIkkeFunnetException e) {
			return dummypdf;
		}
	}
}
