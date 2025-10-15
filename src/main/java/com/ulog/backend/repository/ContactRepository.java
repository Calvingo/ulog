package com.ulog.backend.repository;

import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findAllByOwnerAndDeletedFalseOrderByCreatedAtDesc(User owner);

    Optional<Contact> findByIdAndOwnerAndDeletedFalse(Long id, User owner);
}
