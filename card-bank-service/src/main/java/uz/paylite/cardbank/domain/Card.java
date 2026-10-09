package uz.paylite.cardbank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import uz.paylite.cardbank.domain.enumeration.CardStatus;
import uz.paylite.cardbank.domain.enumeration.CardType;

/**
 * A Card.
 */
@Entity
@Table(name = "card")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 16, max = 16)
    @Column(name = "pan", length = 16, nullable = false, unique = true)
    private String pan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CardType type;

    @NotNull
    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;

    @NotNull
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotNull
    @Size(min = 14, max = 14)
    @Column(name = "pinfl", length = 14, nullable = false)
    private String pinfl;

    @NotNull
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Card id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPan() {
        return this.pan;
    }

    public Card pan(String pan) {
        this.setPan(pan);
        return this;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public CardType getType() {
        return this.type;
    }

    public Card type(CardType type) {
        this.setType(type);
        return this;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public LocalDate getExpireDate() {
        return this.expireDate;
    }

    public Card expireDate(LocalDate expireDate) {
        this.setExpireDate(expireDate);
        return this;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Card fullName(String fullName) {
        this.setFullName(fullName);
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPinfl() {
        return this.pinfl;
    }

    public Card pinfl(String pinfl) {
        this.setPinfl(pinfl);
        return this;
    }

    public void setPinfl(String pinfl) {
        this.pinfl = pinfl;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public Card phoneNumber(String phoneNumber) {
        this.setPhoneNumber(phoneNumber);
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CardStatus getStatus() {
        return this.status;
    }

    public Card status(CardStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Card createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }
        return getId() != null && getId().equals(((Card) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Card{" +
            "id=" + getId() +
            ", pan='" + getPan() + "'" +
            ", type='" + getType() + "'" +
            ", expireDate='" + getExpireDate() + "'" +
            ", fullName='" + getFullName() + "'" +
            ", pinfl='" + getPinfl() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
