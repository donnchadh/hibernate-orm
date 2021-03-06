package org.hibernate.jpa.test.cascade;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Access(AccessType.FIELD)
public class Student {

	@Id @GeneratedValue
	Long id;
	
	String name;
	
	@ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
	private Teacher primaryTeacher;

	@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
	private Teacher favoriteTeacher;
	
	public  Student() {
	}

	public Teacher getFavoriteTeacher() {
		return favoriteTeacher;
	}

	public void setFavoriteTeacher(Teacher lifeCover) {
		this.favoriteTeacher = lifeCover;
	}

	public Teacher getPrimaryTeacher() {
		return primaryTeacher;
	}

	public void setPrimaryTeacher(Teacher relativeTo) {
		this.primaryTeacher = relativeTo;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
