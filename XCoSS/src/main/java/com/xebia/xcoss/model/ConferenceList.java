package com.xebia.xcoss.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conferences")
public class ConferenceList {

    private List<Conference> conferenceList;

    @XmlElement(name="conference")
    public List<Conference> getConferenceList() {
		return conferenceList;
	}
    
    public void setConferenceList(List<Conference> conferenceList) {
		this.conferenceList = conferenceList;
	}
}
