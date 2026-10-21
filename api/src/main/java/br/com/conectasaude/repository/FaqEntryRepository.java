package br.com.conectasaude.repository;

import br.com.conectasaude.model.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaqEntryRepository extends JpaRepository<FaqEntry, Long> {
    List<FaqEntry> findAll();
}