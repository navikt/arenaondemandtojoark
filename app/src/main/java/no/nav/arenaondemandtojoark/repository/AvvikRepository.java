package no.nav.arenaondemandtojoark.repository;

import no.nav.arenaondemandtojoark.domain.db.Avvik;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvvikRepository extends CrudRepository<Avvik, Long> {
}
