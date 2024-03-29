package no.nav.arenaondemandtojoark.repository;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournaldataRepository extends CrudRepository<Journaldata, Long> {

	List<Journaldata> getAllByFilnavnAndStatus(String filnavn, JournaldataStatus status);

	Long countJournaldataByFilnavnAndStatus(String filnavn, JournaldataStatus status);

	@Query("""
				select j from Journaldata j
				left join j.avvik a
				where (a is null or a.retryable = true)
				and j.filnavn = :filnavn
				and j.status in :statuses
			""")
	List<Journaldata> getAllByFilnavnAndStatuses(
			@Param("filnavn") String filnavn,
			@Param("statuses") List<JournaldataStatus> statuses
	);

	// Rapportlaging
	@Query("""
				select new no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement(j.onDemandId, j.journalpostId, j.dokumentInfoId) from Journaldata j
				where j.filnavn = :filnavn
				and j.status = :status
			""")
	List<Rapportelement> getRapportdataByFilnavnAndStatus(
			@Param("filnavn") String filnavn,
			@Param("status") JournaldataStatus status
	);

	@Modifying
	@Query("""
				update Journaldata j
				set j.status = :status
				where j.onDemandId = :onDemandId
			""")
	void updateStatus(
			@Param("onDemandId") String onDemandId,
			@Param("status") JournaldataStatus status
	);

	@Modifying
	@Query("""
				update Journaldata j
				set j.status = 'AVLEVERT', j.rapportfil = :rapportfil
				where j.filnavn = :filnavn
				and j.status = 'PROSESSERT'
			""")
	void updateStatusToAvlevertAndSetRapportfil(
			@Param("filnavn") String filnavn,
			@Param("rapportfil") String rapportfil
 	);
}