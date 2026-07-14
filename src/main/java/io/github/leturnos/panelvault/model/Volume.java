package io.github.leturnos.panelvault.model;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Volume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;
    
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    
    @Column(nullable = false)
    private Boolean owned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Volume() {
    }

    public Volume(Long id, Integer number, LocalDate purchaseDate, BigDecimal purchasePrice, Boolean owned, Work work, User user) {
        this.id = id;
        this.number = number;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.owned = owned;
        this.work = work;
        this.user = user;
    }

    public Volume(VolumeRequestDTO data) {
        this.number = data.number();
        this.purchaseDate = data.purchaseDate();
        this.purchasePrice = data.purchasePrice();
        this.owned = data.owned();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Volume volume = (Volume) o;
        return Objects.equals(id, volume.id) && Objects.equals(number, volume.number) && Objects.equals(purchaseDate, volume.purchaseDate) && Objects.equals(purchasePrice, volume.purchasePrice) && Objects.equals(owned, volume.owned) && Objects.equals(work, volume.work) && Objects.equals(user, volume.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, purchaseDate, purchasePrice, owned, work, user);
    }
}
