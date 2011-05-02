package com.xebia.xcoss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="Conference")
@XmlRootElement(name = "conference")
public class Conference {

    @Id
    @Column(name="id")
    @GeneratedValue
    private Integer id;

    private Date date;

    public Date getDate() {
		return date;
	}
    
    public void setDate(Date date) {
		this.date = date;
	}
}
