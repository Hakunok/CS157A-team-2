package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.dto.SessionUser;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.AccountRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.repository.PersonRepository;
import com.airchive.util.PasswordUtils;
import com.airchive.util.SecurityUtils;
import com.airchive.util.ValidationUtils;
import java.sql.Connection;

public class PersonAccountService {

  private final PersonRepository personRepository;
  private final AccountRepository accountRepository;
  private final CollectionRepository collectionRepository;

  public PersonAccountService(
      PersonRepository personRepository,
      AccountRepository accountRepository,
      CollectionRepository collectionRepository) {
    this.personRepository = personRepository;
    this.accountRepository = accountRepository;
    this.collectionRepository = collectionRepository;
  }

  public Account createAccount(Person person, Account account) {

    ValidationUtils.validateUsername(account.username());
    ValidationUtils.validateEmail(account.email());
    ValidationUtils.validatePassword(account.passwordHash());
    ValidationUtils.validateName(person.firstName());
    ValidationUtils.validateName(person.lastName());

    if (accountRepository.existsByUsername(account.username())) {
      throw new ValidationException("Username is already taken.");
    }
    if (accountRepository.existsByEmail(account.email()) || personRepository.existsByIdentityEmail(person.identityEmail())) {
      throw new ValidationException("Email is already in use.");
    }

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Person createdPerson = personRepository.create(person, conn);

      String hashed = PasswordUtils.hashPassword(account.passwordHash());
      Account newAccount = new Account(
          0,
          createdPerson.personId(),
          account.email(),
          account.username(),
          hashed,
          account.role(),
          account.isAdmin(),
          null
      );

      Account createdAccount = accountRepository.create(newAccount, conn);
      collectionRepository.createDefaultCollection(createdAccount.accountId(), conn);

      tx.commit();
      return createdAccount;
    }
  }

  public Person createPerson(Person person) {
    ValidationUtils.validateName(person.firstName());
    ValidationUtils.validateName(person.lastName());
    ValidationUtils.validateEmail(person.identityEmail());

    if (personRepository.existsByIdentityEmail(person.identityEmail().toLowerCase()) ||
        accountRepository.existsByEmail(person.identityEmail().toLowerCase())) {
      throw new ValidationException("A person or account with this email already exists.");
    }

    return personRepository.create(person);
  }

  public Account login(String usernameOrEmail, String password) {
    Account account = accountRepository.findByUsernameOrEmail(usernameOrEmail)
        .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

    boolean passwordValid = PasswordUtils.verifyPassword(password, account.passwordHash());
    if (!passwordValid) {
      throw new AuthenticationException("Invalid username or password.");
    }

    return account;
  }

  public void makeAdmin(SessionUser requester, int accountIdToPromote) {
    SecurityUtils.requireAdmin(requester);

    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Account account = accountRepository.findById(accountIdToPromote, conn)
          .orElseThrow(() -> new EntityNotFoundException("Account not found."));

      if (account.isAdmin()) {
        tx.commit();
        return;
      }

      accountRepository.setAdmin(accountIdToPromote, true, conn);
      tx.commit();
    }
  }

  public Account getAccountById(int accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new EntityNotFoundException("Account not found."));
  }

  public Person getPersonById(int personId) {
    return personRepository.findById(personId)
        .orElseThrow(() -> new EntityNotFoundException("Person not found."));
  }

  public Account getAccountByEmail(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Account not found."));
  }

  public Account getAccountByUsername(String username) {
    return accountRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("Account not found."));
  }

  public boolean usernameExists(String username) {
    return accountRepository.existsByUsername(username);
  }

  public boolean emailExists(String email) {
    return accountRepository.existsByEmail(email) || personRepository.existsByIdentityEmail(email);
  }
}