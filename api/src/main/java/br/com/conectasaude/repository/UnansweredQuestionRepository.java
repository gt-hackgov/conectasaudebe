package br.com.conectasaude.repository;

import br.com.conectasaude.model.UnansweredQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnansweredQuestionRepository extends JpaRepository<UnansweredQuestion, Long> {
}
