package test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Two {

	@Id
	int id;
	
	@Column(name="CLIENT_ID")
	String childId;
	
	@Column(name="NAME")
	String name;
}
