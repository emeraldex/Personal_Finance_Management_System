package com.example.expensecore.repository;

import com.example.expensecore.domain.Income;
import com.example.expensecore.dto.IncomeDTO;
import java.util.List;

public interface IncomeRepository {
    List<Income> getAllIncomes();
    Income getIncomeById(int id);
    void addIncome(IncomeDTO incomeDTO);
    void updateIncome(IncomeDTO incomeDTO);
    void deleteIncome(int id);
}
