package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import no.nav.arenaondemandtojoark.domain.joark.AvsenderMottaker;
import no.nav.arenaondemandtojoark.domain.joark.Bruker;
import no.nav.arenaondemandtojoark.domain.joark.Dokument;
import no.nav.arenaondemandtojoark.domain.joark.JournalpostType;
import no.nav.arenaondemandtojoark.domain.joark.Sak;
import no.nav.arenaondemandtojoark.domain.joark.Tilleggsopplysning;

import java.util.List;

@Value
@Builder
@Jacksonized
public class OpprettJournalpostRequest {
	JournalpostType journalposttype;
	AvsenderMottaker avsenderMottaker;
	Bruker bruker;
	String tema;
	String tittel;
	String kanal;
	String journalfoerendeEnhet;
	String eksternReferanseId;
	List<Tilleggsopplysning> tilleggsopplysninger;
	Sak sak;
	List<Dokument> dokumenter;
}
