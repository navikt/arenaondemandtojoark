package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.DokarkivConsumer;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostRequest;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostResponse;
import no.nav.arenaondemandtojoark.consumer.ondemandbrev.OndemandBrevConsumer;
import no.nav.arenaondemandtojoark.domain.Journaldata;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.FerdigstillJournalpostRequestMapper;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.map.OpprettJournalpostRequestMapper;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArenaOndemandToJoarkService {

	private final OndemandBrevConsumer ondemandBrevConsumer;
	private final DokarkivConsumer dokarkivConsumer;

	public ArenaOndemandToJoarkService(OndemandBrevConsumer ondemandBrevConsumer,
									   DokarkivConsumer dokarkivConsumer) {
		this.ondemandBrevConsumer = ondemandBrevConsumer;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	@Handler
	public void processJournaldata(@Body Journaldata journaldata) {
		byte[] pdfDocument = ondemandBrevConsumer.hentPdf(journaldata.getOnDemandId());

		OpprettJournalpostRequest journalpost = OpprettJournalpostRequestMapper.map(journaldata, pdfDocument);
		OpprettJournalpostResponse response = dokarkivConsumer.opprettJournalpost(journalpost);

		FerdigstillJournalpostRequest ferdigstillJournalpostRequest = FerdigstillJournalpostRequestMapper.map(journaldata);

		dokarkivConsumer.ferdigstillJournalpost(response.journalpostId(), ferdigstillJournalpostRequest);
	}
}
