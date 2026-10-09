package uz.paylite.cardbank.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import uz.paylite.cardbank.domain.Card;
import uz.paylite.cardbank.domain.enumeration.CardType;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Card entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    boolean existsByPan(String pan);

    boolean existsByPinfl(String pinfl);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPanAndType(String pan, CardType type);

    Optional<Card> findByPan(String pan);
}
