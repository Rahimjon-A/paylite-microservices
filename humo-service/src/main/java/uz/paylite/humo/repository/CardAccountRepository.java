package uz.paylite.humo.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.paylite.humo.domain.CardAccount;

import java.util.Optional;

/**
 * Spring Data JPA repository for the CardAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CardAccountRepository extends JpaRepository<CardAccount, Long> {
    Optional<CardAccount> findByPan(String pan);

    boolean existsByPan(String pan);

    @Modifying
    @Query("""
            update CardAccount a
               set a.balance = a.balance - :amount,
                   a.updatedAt = CURRENT_TIMESTAMP
             where a.pan = :pan
               and a.status = CardAccountStatus.ACTIVE
               and a.balance >= :amount
        """)
    int withdraw(@Param("pan") String pan, @Param("amount") Long amount);

    @Modifying
    @Query("""
            update CardAccount a
               set a.balance = a.balance + :amount,
                   a.updatedAt = CURRENT_TIMESTAMP
             where a.pan = :pan
               and a.status = CardAccountStatus.ACTIVE
        """)
    int deposit(@Param("pan") String pan, @Param("amount") Long amount);
}
