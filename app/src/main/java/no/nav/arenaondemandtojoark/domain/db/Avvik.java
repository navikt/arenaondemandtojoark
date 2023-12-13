package no.nav.arenaondemandtojoark.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Avvik {

	private static final String AVVIK_ID_SEQUENCE = "avvik_id_seq";

	@Id
	@Column(name = "avvik_id")
	private Long avvikId;

	@Column(name = "ondemand_id")
	private String ondemandId;

	@Column(name = "filnavn")
	private String filnavn;

	@Column(name = "feiltype")
	private String feiltype; //TODO Burde dette være en boolean? isRetryable?

	@Column(name = "feilmelding", length = 500)
	private String feilmelding;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "journaldata_id")
	@MapsId
	private Journaldata journaldata;
}
