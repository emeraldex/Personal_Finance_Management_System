package com.example.expensecore.service;

import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.repository.IncomeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

class IncomeServiceTest {

    @Test
    void testGetAllIncomes() {
        IncomeRepository incomeRepository = Mockito.mock(IncomeRepository.class);
        IncomeService incomeService = new IncomeService(incomeRepository);

        List<IncomeDTO> expectedIncomes = Arrays.asList(
            new IncomeDTO(1, "2023-01-01", "Salary", 5000.0, 1, 1, 1),
            new IncomeDTO(2, "2023-01-02", "Freelance", 2000.0, 2, 2, 2)
        );

        Mockito.when(incomeRepository.getAllIncomes()).thenReturn(expectedIncomes);

        List<IncomeDTO> actualIncomes = incomeService.getAllIncomes();

        Assertions.assertEquals(expectedIncomes, actualIncomes);
    }

    @Test
    void testGetIncomeById() {
        IncomeRepository incomeRepository = Mockito.mock(IncomeRepository.class);
        IncomeService incomeService = new IncomeService(incomeRepository);

        IncomeDTO expectedIncome = new IncomeDTO(1, "2023-01-01", "Salary", 5000.0, 1, 1, 1);

        Mockito.when(incomeRepository.getIncomeById(1)).thenReturn(expectedIncome);

        IncomeDTO actualIncome = incomeService.getIncomeById(1);

        Assertions.assertEquals(expectedIncome, actualIncome);
    }

    @Test
    void testAddIncome() {
        IncomeRepository incomeRepository = Mockito.mock(IncomeRepository.class);
        IncomeService incomeService = new IncomeService(incomeRepository);

        IncomeDTO incomeDTO = new IncomeDTO(0, "2023-01-01", "Salary", 5000.0, 1, 1, 1);

        incomeService.addIncome(incomeDTO);

        Mockito.verify(incomeRepository).addIncome(incomeDTO);
    }

    @Test
    void testUpdateIncome() {
        IncomeRepository incomeRepository = Mockito.mock(IncomeRepository.class);
        IncomeService incomeService = new IncomeService(incomeRepository);

        IncomeDTO incomeDTO = new IncomeDTO(1, "2023-01-01", "Salary", 5000.0, 1, 1, 1);

        incomeService.updateIncome(incomeDTO);

        Mockito.verify(incomeRepository).updateIncome(incomeDTO);
    }

    @Test
    void testDeleteIncome() {
        IncomeRepository incomeRepository = Mockito.mock(IncomeRepository.class);
        IncomeService incomeService = new IncomeService(incomeRepository);

        incomeService.deleteIncome(1);

        Mockito.verify(incomeRepository).deleteIncome(1);
    }
}
