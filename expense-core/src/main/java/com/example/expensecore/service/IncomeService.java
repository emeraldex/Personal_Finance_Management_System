package com.example.expensecore.service;

import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.repository.IncomeRepository;
import java.util.List;

public class IncomeService {
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public List<IncomeDTO> getAllIncomes() {
        return incomeRepository.getAllIncomes();
    }

    public IncomeDTO getIncomeById(int id) {
        return incomeRepository.getIncomeById(id);
    }

    public void addIncome(IncomeDTO incomeDTO) {
        incomeRepository.addIncome(incomeDTO);
    }

    public void updateIncome(IncomeDTO incomeDTO) {
        incomeRepository.updateIncome(incomeDTO);
    }

    public void deleteIncome(int id) {
        incomeRepository.deleteIncome(id);
    }
}
