package com.erp.accounting.domain;

import com.erp.core.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
public class JournalEntry extends BaseEntity {

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false)
    private String description;

    /** Business reference, e.g. SO-12 for sales order 12. */
    private String reference;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLine> lines = new ArrayList<>();

    public void addLine(JournalLine line) {
        line.setEntry(this);
        lines.add(line);
    }
}
