package com.example.expensecore.service;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.repository.ExpenseRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

class ExpenseServiceTest {

    @Test
    void testGetAllExpenses() {
        ExpenseRepository expenseRepository = Mockito.mock(ExpenseRepository.class);
        ExpenseService expenseService = new ExpenseService(expenseRepository);

        List<ExpenseDTO> expectedExpenses = Arrays.asList(
            new ExpenseDTO(1, "2023-01-01", "Groceries", -50.0, 1, 1, 1),
            new ExpenseDTO(2, "2023-01-02", "Utilities", -100.0, 2, 2, 2)
        );

        Mockito.when(expenseRepository.getAllExpenses()).thenReturn(expectedExpenses);

        List<ExpenseDTO> actualExpenses = expenseService.getAllExpenses();

        Assertions.assertEquals(expectedExpenses, actualExpenses);
    }

    @Test
    void testGetExpenseById() {
        ExpenseRepository expenseRepository = Mockito.mock(ExpenseRepository.class);
        ExpenseService expenseService = new ExpenseService(expenseRepository);

        ExpenseDTO expectedExpense = new ExpenseDTO(1, "2023-01-01", "Groceries", -50.0, 1, 1, 1);

        Mockito.when(expenseRepository.getExpenseById(1)).thenReturn(expectedExpense);

        ExpenseDTO actualExpense = expenseService.getExpenseById(1);

        Assertions.assertEquals(expectedExpense, actualExpense);
    }

    @Test
    void testAddExpense() {
        ExpenseRepository expenseRepository = Mockito.mock(ExpenseRepository.class);
        ExpenseService expenseService = new ExpenseService(expenseRepository);

        ExpenseDTO expenseDTO = new ExpenseDTO(0, "2023-01-01", "Groceries", -50.0, 1, 1, 1);

        expenseService.addExpense(expenseDTO);

        Mockito.verify(expenseRepository).addExpense(expenseDTO);
    }

    @Test
    void testUpdateExpense() {
        ExpenseRepository expenseRepository = Mockito.mock(ExpenseRepository.class);
        ExpenseService expenseService = new ExpenseService(expenseRepository);

        ExpenseDTO expenseDTO = new ExpenseDTO(1, "2023-01-01", "Groceries", -50.0, 1, 1, 1);

        expenseService.updateExpense(expenseDTO);

        Mockito.verify(expenseRepository).updateExpense(expenseDTO);
    }

    @Test
    void testDeleteExpense() {
        ExpenseRepository expenseRepository = Mockito.mock(ExpenseRepository.class);
        ExpenseService expenseService = new ExpenseService(expenseRepository);

        expenseService.deleteExpense(1);

        Mockito.verify(expenseRepository).deleteExpense(1);
    }
}
