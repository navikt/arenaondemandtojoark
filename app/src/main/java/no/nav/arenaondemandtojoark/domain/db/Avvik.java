package no.nav.arenaondemandtojoark.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "avvik_id")
	private Long avvikId;

	@Column(name = "ondemand_id")
	private String ondemandId;

	@Column(name = "filnavn")
	private String filnavn;

	@Column(name = "feiltype")
	private String feiltype;

	@Column(name = "feilmelding")
	private String feilmelding;
}
