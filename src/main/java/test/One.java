package test;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;


@Entity
public class One {

	@Id
	int id;
	
	@Column(name="STOCK_ID")
	String stockId;
	
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="CHILD_ID")
	Set<Two> setOfTwo;
}
