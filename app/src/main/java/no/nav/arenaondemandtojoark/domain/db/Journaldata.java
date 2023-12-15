package no.nav.arenaondemandtojoark.domain.db;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.NONE;

@Entity(name = "Journaldata")
@Table(name = "journaldata")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Journaldata {

	private static final String JOURNALDATA_ID_SEQUENCE = "journaldata_id_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = JOURNALDATA_ID_SEQUENCE)
	@SequenceGenerator(name = JOURNALDATA_ID_SEQUENCE, sequenceName = JOURNALDATA_ID_SEQUENCE, allocationSize = 1)
	@Column(name = "journaldata_id")
	@Setter(NONE)
	private Long journaldataId;

	@Column(name = "ondemand_id")
	String onDemandId;

	@Column(name = "saksnummer")
	String saksnummer;

	@Column(name = "bruker_id")
	String brukerId;

	@Column(name = "brukertype")
	String brukertype;

	@Enumerated(EnumType.STRING)
	@Column(name = "journalposttype")
	Journalposttype journalposttype;

	@Enumerated(EnumType.STRING)
	@Column(name = "fagomraade")
	Fagomraade fagomraade;

	@Column(name = "journaldato")
	LocalDateTime journaldato;

	@Column(name = "innhold")
	String innhold;

	@Column(name = "mottakernavn")
	String mottakernavn;

	@Column(name = "mottaker_id")
	String mottakerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "utsendingskanal")
	Utsendingskanal utsendingskanal;

	@Column(name = "journalfoerende_enhet")
	String journalfoerendeEnhet;

	@Column(name = "sendt_print_dato")
	LocalDateTime sendtPrintDato;

	@Column(name = "opprettet_av_navn")
	String opprettetAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "dokumentkategori")
	Dokumentkategori dokumentkategori;

	@Column(name = "brevkode")
	String brevkode;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	JournaldataStatus status;

	@Column(name = "filnavn")
	String filnavn;

	@Column(name = "journalpost_id")
	String journalpostId;

	@Column(name = "dokument_info_id")
	String dokumentInfoId;

	@OneToOne(mappedBy = "journaldata", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Avvik avvik;

	//TODO: Prøv å slette dette
	public void setAvvik(Avvik avvik) {
		if (avvik == null) {
			if (this.avvik != null) {
				this.avvik.setJournaldata(null);
			}
		}
		else {
			avvik.setJournaldata(this);
		}
		this.avvik = avvik;
	}
}
