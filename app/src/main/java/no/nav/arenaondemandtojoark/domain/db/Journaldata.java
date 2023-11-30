package no.nav.arenaondemandtojoark.domain.db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.NONE;

@Entity
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Journaldata {

	// TODO: SequenceGenerator?
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
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

	@Column(name = "status")
	String status;

	@Column(name = "filnavn")
	String filnavn;

	@Column(name = "journalpostId")
	String journalpostId;

	@Column(name = "dokumentInfoId")
	String dokumentInfoId;

	// Status -> (default) INNLEST, HAR_HENTET_ONDEMAND_DOKUMENT (avvik), HAR_FÅTT_OPPRETTET_JOURNALPOST, HAR_FERDIGSTILT_JOURNALPOST, HAR_LAGET_JOURNALPOSTRAPPORT
	// Status -> INNLEST, AVLEVERT, FEILET

	// ODID 1, 2, 3, 4, 5, 6, 7
	// xm
	/*
	To tabellar:
	1. Journalpostrapport (ondemandId, journalpostId (frå dokarkiv), dokumentInfoId (frå dokarkiv))
		- 1, 3 og 4

	 */

}
