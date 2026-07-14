package io.github.leturnos.panelvault.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "user_work", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "work_id"}))
public class UserWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    public UserWork() {
    }

    public UserWork(Long id, User user, Work work, WorkStatus status, BigDecimal rating) {
        this.id = id;
        this.user = user;
        this.work = work;
        this.status = status;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public void setStatus(WorkStatus status) {
        this.status = status;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserWork userWork = (UserWork) o;
        return Objects.equals(id, userWork.id) && Objects.equals(user, userWork.user) && Objects.equals(work, userWork.work) && status == userWork.status && Objects.equals(rating, userWork.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, work, status, rating);
    }
}
