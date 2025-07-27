package com.airchive.service;

import com.airchive.db.Transaction;
import com.airchive.entity.Account;
import com.airchive.entity.Person;
import com.airchive.exception.AuthenticationException;
import com.airchive.exception.EntityNotFoundException;
import com.airchive.exception.ValidationException;
import com.airchive.repository.AccountRepository;
import com.airchive.repository.CollectionRepository;
import com.airchive.repository.PersonRepository;
import com.airchive.util.PasswordUtils;
import com.airchive.util.ValidationUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

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

    Map<String, String> errors = validateAccountFields(
        account.username(),
        account.email(),
        account.passwordHash(),
        person.firstName(),
        person.lastName()
    );

    if (!errors.isEmpty()) {
      throw new ValidationException("Account validation failed.");
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
    Map<String, String> errors = new HashMap<>();

    if (person.firstName() == null || person.firstName().isBlank()) {
      errors.put("firstName", "First name is required.");
    } else if (!ValidationUtils.isValidName(person.firstName())) {
      errors.put("firstName", "First name must be letters only, up to 40 characters.");
    }

    if (person.lastName() == null || person.lastName().isBlank()) {
      errors.put("lastName", "Last name is required.");
    } else if (!ValidationUtils.isValidName(person.lastName())) {
      errors.put("lastName", "Last name must be letters only, up to 40 characters.");
    }

    if (person.identityEmail() == null || person.identityEmail().isBlank()) {
      errors.put("email", "Email is required.");
    } else if (!ValidationUtils.isValidEmail(person.identityEmail())) {
      errors.put("email", "Invalid email format.");
    } else if (personRepository.existsByIdentityEmail(person.identityEmail().toLowerCase())
        || accountRepository.existsByEmail(person.identityEmail().toLowerCase())) {
      errors.put("email", "A person with this email already exists.");
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Person validation failed for identity email: " + person.identityEmail());
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

  public void makeAdmin(int accountId) {
    try (Transaction tx = new Transaction()) {
      tx.begin();
      Connection conn = tx.getConnection();

      Account account = accountRepository.findById(accountId, conn)
          .orElseThrow(() -> new EntityNotFoundException("Account not found."));

      if (account.isAdmin()) {
        tx.commit();
        return;
      }

      accountRepository.setAdmin(accountId, true, conn);
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

  public Map<String, String> validateAccountFields(
      String username,
      String email,
      String password,
      String firstName,
      String lastName
  ) {
    Map<String, String> errors = new HashMap<>();

    if (username == null || username.isBlank()) {
      errors.put("username", "Username is required.");
    } else if (!ValidationUtils.isValidUsername(username)) {
      errors.put("username", "Username must be 3–20 characters and contain only a-z, 0-9, ., _, or -.");
    } else if (accountRepository.existsByUsername(username)) {
      errors.put("username", "Username is already taken.");
    }

    if (email == null || email.isBlank()) {
      errors.put("email", "Email is required.");
    } else if (!ValidationUtils.isValidEmail(email.toLowerCase())) {
      errors.put("email", "Invalid email format.");
    } else if (accountRepository.existsByEmail(email.toLowerCase())
        || personRepository.existsByIdentityEmail(email.toLowerCase())) {
      errors.put("email", "Email is already in use.");
    }

    if (password == null || password.isBlank()) {
      errors.put("password", "Password is required.");
    } else if (!ValidationUtils.isValidPassword(password)) {
      errors.put("password", "Password must be at least 8 characters.");
    }

    if (firstName == null || firstName.isBlank()) {
      errors.put("firstName", "First name is required.");
    } else if (!ValidationUtils.isValidName(firstName)) {
      errors.put("firstName", "First name must be letters only, up to 40 characters.");
    }

    if (lastName == null || lastName.isBlank()) {
      errors.put("lastName", "Last name is required.");
    } else if (!ValidationUtils.isValidName(lastName)) {
      errors.put("lastName", "Last name must be letters only, up to 40 characters.");
    }

    return errors;
  }
}
