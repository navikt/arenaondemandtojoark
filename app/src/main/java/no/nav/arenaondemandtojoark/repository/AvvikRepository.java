package no.nav.arenaondemandtojoark.repository;

import no.nav.arenaondemandtojoark.domain.db.Avvik;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvvikRepository extends CrudRepository<Avvik, Long> {

	public List<Avvik> getAllByFilnavn(String filnavn);
}
