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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Avvik")
@Table(name = "avvik")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Avvik {

	public static final int MAX_FEILMELDING_LENGDE = 500;

	@Id
	private Long journaldataId;

	@Column(name = "retryable")
	private boolean retryable;

	@Column(name = "feilmelding", length = MAX_FEILMELDING_LENGDE)
	private String feilmelding;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "journaldata_id")
	private Journaldata journaldata;
}
