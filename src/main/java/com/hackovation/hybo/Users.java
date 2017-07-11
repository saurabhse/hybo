package com.hackovation.hybo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;


@Data
@Entity
public class Users {

	private @Id @GeneratedValue Long id;
	private String username;
	private String person_id;
	

	private Users() {}

	public Users(String username, String person_id) {
		this.username = username;
		this.person_id = person_id;
	}
}
// end::code[]