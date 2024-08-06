package com.isep.appli.repositories;
import com.isep.appli.dbModels.User;

import com.isep.appli.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findUserByEmail(String email);

	User findUserByUsername(String username);
	List<User> findAllByIsLogin(Status status);

	boolean existsByUsernameAndIdNot(String username, Long userId);
	boolean existsByEmailAndIdNot(String username, Long userId);
}
