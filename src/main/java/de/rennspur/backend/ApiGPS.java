/*
 *  This file is part of Renspur.
 *  
 *  Copyright (C) 2016  maurer.ruben, maurerruben97@gmail.com
 *  
 *  Rennspur is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Rennspur is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rennspur.backend;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

import de.rennspur.model.Club;
import de.rennspur.model.Clubs;
import de.rennspur.model.Event;
import de.rennspur.model.Race;
import de.rennspur.model.TeamPosition;

/**
 * ApiGPS objects are the service endpoints to handle the incomming GPS-Data
 * from the GPS-Clients.
 * 
 * @author ruben.maurer, burghard.britzke,leon.schlender
 */
@Path("/gps-service")
public class ApiGPS {
	@Inject
	private EntityManagerFactory emf;

	/**
	 * Demonstrates an objects short way via an injected bean to the database
	 * and with data back via servlet to the client as xml.
	 * 
	 * @return A list of clubs.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<Club> getClubs() {
		EntityManager em = emf.createEntityManager();

		Query query = em.createNamedQuery("Club.findAll");
		@SuppressWarnings("unchecked")
		List<Club> clubs = query.getResultList();
		System.out.println("ApiGPS::getClub()");
		return clubs;
	}

	/**
	 * Handles the Post from the GPS-component.
	 * 
	 * @param jsonString
	 *            The received JSON-object as a String.
	 * 
	 * @return ok String with the message of success.
	 */
	@POST
	@Consumes({ MediaType.TEXT_PLAIN })
	@Produces(MediaType.TEXT_PLAIN)
	public String getGPSDataInJSON(String jsonString) {

		
		String key, name,longitude,latitude;
				
		Clubs clubs = new Clubs();  
		
		JSONArray lineItems;
		ArrayList<TeamPosition> positionList;
		JSONObject json;

		/**
		 * Initializing Variables.
		 */
		positionList = new ArrayList<TeamPosition>();
		json = new JSONObject(jsonString);

		/**
		 * Extracting the key value from the JSON Object.
		 */
		try {
			key = json.getString("key");
			System.out.println("Boat key : " + key);
			
			if(key == "" || key == null)
				key = "0";

		} catch (JSONException e) {
			System.out.println("Failed extracting values for key & name!");
			e.printStackTrace();
			key = "0";
		}

		/**
		 * Extracting the position-array from the JSONObject and handling
		 * possible exceptions.
		 */
		try {
			lineItems = json.getJSONArray("status");
		} catch (JSONException e) {
			System.out.println("Failed extracting lineItems array!");
			e.printStackTrace();
			lineItems = null;
		}

		/**
		 * Itterating through the extracted arry from the JSONObject, to get
		 * every position.
		 */
		if (lineItems != null) {
			for (Object o : lineItems) {

				System.out.println(o.toString());
				JSONObject jsonPostitionObject = new JSONObject(o.toString());

				/**
				 * Creating a new TeampositionObject and filling it with the
				 * extracted Data from the JSONObject.
				 * 
				 * TeamPosition newPosition = new TeamPosition();
				 * newPosition.setId(Integer.parseInt(key));
				 * newPosition.setLatitude(Double.parseDouble(latitude));
				 * newPosition.setLongitude(Double.parseDouble(longitude));
				 * newPosition.setTime(convertStringToTimestamp(time));
				 */
				
				TeamPosition newPosition = new TeamPosition();
				
				newPosition.setId(Integer.parseInt(key));
				newPosition.setLatitude(jsonPostitionObject.getDouble("lat"));
				newPosition.setLongitude(jsonPostitionObject.getDouble("long"));
				
				/**
				 * Adding the new now filled TeamPosition object into the
				 * Arraylist.
				 * 
				 * 
				 */
				
				positionList.add(newPosition);

			}
		}

		return " ok";
	}

	/**
	 * Small utility-method to convert a String into the Timestamp format.
	 * 
	 * @param str_date
	 * @return Timestamp
	 */
	public static Timestamp convertStringToTimestamp(String str_date) {
		try {
			DateFormat formatter;
			formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date date = (Date) formatter.parse(str_date);
			Timestamp timeStampDate = new Timestamp(date.getTime());
			return timeStampDate;
		} catch (ParseException e) {
			System.out.println("Exception :" + e);
			return null;
		}
	}
}