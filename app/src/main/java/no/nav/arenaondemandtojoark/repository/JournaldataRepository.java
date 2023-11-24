package no.nav.arenaondemandtojoark.repository;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournaldataRepository extends CrudRepository<Journaldata, Long> {
}