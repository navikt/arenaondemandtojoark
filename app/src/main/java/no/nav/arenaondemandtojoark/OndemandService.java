package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.consumer.ondemandbrev.OndemandBrevConsumer;
import no.nav.arenaondemandtojoark.domain.Journaldata;
import no.nav.arenaondemandtojoark.exception.OndemandDokumentIkkeFunnetException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class OndemandService {

	private final OndemandBrevConsumer ondemandBrevConsumer;
	private static final byte[] dummypdf;

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

	public OndemandService(OndemandBrevConsumer ondemandBrevConsumer) {
		this.ondemandBrevConsumer = ondemandBrevConsumer;
	}

	public byte[] hentDokumentFraOndemand(Journaldata journaldata) {

		try {
			return ondemandBrevConsumer.hentPdf(journaldata.getOnDemandId());
		} catch (OndemandDokumentIkkeFunnetException e) {
			return dummypdf;
		}
	}
}
