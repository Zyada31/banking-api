package com.bank.api.integration;

import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Customer;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountIntegrationTest extends TestContainerConfig
{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup()
    {
        bankAccountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void testCreateAccount_Success() throws Exception
    {
        mockMvc.perform(post("/accounts")
                        .param("customerName", "John Doe")
                        .param("initialDeposit", "500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Account created or deposit successful")));
    }

    @Test
    void testCreateAccount_Fails_MinimumDeposit() throws Exception
    {
        mockMvc.perform(post("/accounts")
                        .param("customerName", "John Doe")
                        .param("initialDeposit", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Minimum deposit amount is $50")));
    }

    @Test
    void testGetAccountBalance_Success() throws Exception
    {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer = customerRepository.save(customer);

        BankAccount account = new BankAccount();
        account.setAccountNumber("ACC-123");
        account.setBalance(new BigDecimal("1000"));
        account.setCustomer(customer);
        bankAccountRepository.save(account);

        mockMvc.perform(get("/accounts/ACC-123/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(1000.0)));
    }

    @Test
    void testGetAccountBalance_AccountNotFound() throws Exception
    {
        mockMvc.perform(get("/accounts/ACC-1234/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Account not found: Bank account not found")));
    }
}
