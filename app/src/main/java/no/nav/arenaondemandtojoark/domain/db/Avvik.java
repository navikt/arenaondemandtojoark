package no.nav.arenaondemandtojoark.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class Avvik {

	private static final String AVVIK_ID_SEQUENCE = "avvik_id_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = AVVIK_ID_SEQUENCE)
	@SequenceGenerator(name = AVVIK_ID_SEQUENCE, sequenceName = AVVIK_ID_SEQUENCE, allocationSize = 1)
	@Column(name = "avvik_id")
	private Long avvikId;

	@Column(name = "ondemand_id")
	private String ondemandId;

	@Column(name = "filnavn")
	private String filnavn;

	@Column(name = "feiltype")
	private String feiltype;

	@Column(name = "feilmelding", length = 500)
	private String feilmelding;
}
