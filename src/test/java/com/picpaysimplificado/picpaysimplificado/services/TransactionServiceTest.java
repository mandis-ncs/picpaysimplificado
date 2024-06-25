package com.picpaysimplificado.picpaysimplificado.services;

import com.picpaysimplificado.picpaysimplificado.domain.user.User;
import com.picpaysimplificado.picpaysimplificado.domain.user.UserType;
import com.picpaysimplificado.picpaysimplificado.dtos.TransactionDTO;
import com.picpaysimplificado.picpaysimplificado.repositories.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    //mocking dependencies with empty methods
    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AuthorizationService authorizationService;

    @Autowired
    @InjectMocks //replace real dependencies with the mocks above
    private TransactionService transactionService;

    @BeforeEach
    void setup(){ //init mocks of the present class
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should create transaction successfully when everything is OK")
    void createTransactionCase1() throws Exception {
        User sender = new User(1L, "Maria", "Souza", "99999999901",
                "maria@gmail.com", "12345", new BigDecimal(10), UserType.COMMON);

        User receiver = new User(2L, "Joao", "Souza", "99999999902",
                "joao@gmail.com", "12345", new BigDecimal(10), UserType.COMMON);

        //when the method is called should return the user
        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        //when the method is void is not necessary use 'thenReturn' (e.g. validateTransaction)

        //return true (authorized) to any parameters
        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
        transactionService.createTransaction(request);

        //want to verify if the repository was called at least once
        verify(repository, times(1)).save(any());

        sender.setBalance(new BigDecimal(0));
        verify(userService, times(1)).saveUser(sender);

        receiver.setBalance(new BigDecimal(20));
        verify(userService, times(1)).saveUser(receiver);

        //verifying notification when succeed
        verify(notificationService, times(1))
                .sendNotification(sender, "Transaction successful.");

        verify(notificationService, times(1))
                .sendNotification(receiver, "Transaction received.");
    }

    @Test
    @DisplayName("Should throw Exception when transaction is not authorized")
    void createTransactionCase12() throws Exception {
        User sender = new User(1L, "Maria", "Souza", "99999999901",
                "maria@gmail.com", "12345", new BigDecimal(10), UserType.COMMON);

        User receiver = new User(2L, "Joao", "Souza", "99999999902",
                "joao@gmail.com", "12345", new BigDecimal(10), UserType.COMMON);

        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(false);

        //have to verify the exact type of Exception set on the method
        Exception thrown = Assertions.assertThrows(Exception.class, ()-> {
           TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
           transactionService.createTransaction(request);
        });

        Assertions.assertEquals("Transaction is not authorized.", thrown.getMessage());
    }
}