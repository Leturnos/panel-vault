package io.github.leturnos.panelvault.model;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkType type;

    @Column(length = 100)
    private String publisher;

    @Column(length = 100)
    private String author;

    private Integer totalVolumes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status;

    @Column(length = 500)
    private String coverUrl;

    public Work() {
    }

    public Work(Long id, String title, WorkType type, String publisher, String author, Integer totalVolumes, WorkStatus status, String coverUrl) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.publisher = publisher;
        this.author = author;
        this.totalVolumes = totalVolumes;
        this.status = status;
        this.coverUrl = coverUrl;
    }

    public Work(WorkRequestDTO data) {
        this.title = data.title();
        this.type = data.type();
        this.publisher = data.publisher();
        this.author = data.author();
        this.totalVolumes = data.totalVolumes();
        this.status = data.status();
        this.coverUrl = data.coverUrl();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkType getType() {
        return type;
    }

    public void setType(WorkType type) {
        this.type = type;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getTotalVolumes() {
        return totalVolumes;
    }

    public void setTotalVolumes(Integer totalVolumes) {
        this.totalVolumes = totalVolumes;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public void setStatus(WorkStatus status) {
        this.status = status;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Work work)) return false;
        return Objects.equals(id, work.id) && Objects.equals(title, work.title) && Objects.equals(type, work.type) && Objects.equals(publisher, work.publisher) && Objects.equals(author, work.author) && Objects.equals(totalVolumes, work.totalVolumes) && Objects.equals(status, work.status) && Objects.equals(coverUrl, work.coverUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, type, publisher, author, totalVolumes, status, coverUrl);
    }
}
