package no.nav.arenaondemandtojoark.repository;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournaldataRepository extends CrudRepository<Journaldata, Long> {

	List<Journaldata> getAllByFilnavn(String filnavn);

}