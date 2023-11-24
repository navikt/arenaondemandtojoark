package no.nav.arenaondemandtojoark.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class Avvik {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "avvik_id")
	private Long avvikId;

	@Column(name = "ondemand_id")
	private String ondemandId;

	@Column(name = "filnavn")
	private String filnavn; //property i camel?

	@Column(name = "feiltype")
	private String feiltype; // TEKNISK eller FUNKSJONELL?

	@Column(name = "feilmelding")
	private String feilmelding;

}
