package com.isep.appli.dbModels;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

import com.isep.appli.models.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(columnDefinition="tinyint(1) default 0")
	private Boolean enabled;

	@Column(columnDefinition="tinyint(1) default 0")
	private Boolean isAdmin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status isLogin;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createAccountAt;

	@UpdateTimestamp
	@Column()
	private LocalDateTime lastLoginAt;


	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", password='" + password + '\'' +
				", enabled=" + enabled +
				", isAdmin=" + isAdmin +
				", isLogin=" + isLogin +
				", createAccountAt=" + createAccountAt +
				", lastLoginAt=" + lastLoginAt +
				'}';
	}

	public long getHoursSinceLastLogin() {
		Duration duration = Duration.between(lastLoginAt, LocalDateTime.now());
		return duration.toHours();
	}

	public String getFormattedDurationSinceLastLogin() {
		Duration duration = Duration.between(lastLoginAt, LocalDateTime.now());
		long totalMinutes = duration.toMinutes();
		long hours = totalMinutes / 60;
		long minutes = totalMinutes % 60;
		return String.format("%02dh%02d", hours, minutes);
	}



}
