package com.erp.accounting.repository;

import com.erp.accounting.domain.JournalLine;
import com.erp.accounting.dto.TrialBalanceRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {

    @Query("select new com.erp.accounting.dto.TrialBalanceRow(a.code, a.name, a.type, sum(l.debit), sum(l.credit)) "
            + "from JournalLine l join l.account a group by a.code, a.name, a.type order by a.code")
    List<TrialBalanceRow> trialBalance();
}
