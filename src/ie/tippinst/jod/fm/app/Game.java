package ie.tippinst.jod.fm.app;

import ie.tippinst.jod.fm.db.Database;
import ie.tippinst.jod.fm.db.Message;
import ie.tippinst.jod.fm.model.Club;
import ie.tippinst.jod.fm.model.Competition;
import ie.tippinst.jod.fm.model.Cup;
import ie.tippinst.jod.fm.model.League;
import ie.tippinst.jod.fm.model.Match;
import ie.tippinst.jod.fm.model.Nation;
import ie.tippinst.jod.fm.model.NonPlayer;
import ie.tippinst.jod.fm.model.Person;
import ie.tippinst.jod.fm.model.Player;
import ie.tippinst.jod.fm.model.Round;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class Game {
	
	private static Game game = null;
	private Database db;
	private Club userClub;

	/* This constructs a new game object with the initial date set at 2 July 2009*/
	private Game(){
		super();
		db = new Database();
		userClub = db.getUserClub();
	}
	
	private Game(String file){
		super();
		db = (Database) load(file);
		userClub = db.getUserClub();
		db.setSaved(true);
	}
	
	/* As this is class is a singleton a new game object is returned only if no previous one has been instantiated*/
	public static Game getInstance(){
		if(game == null)
			game = new Game();
		return game;
	}
	
	/* As this is class is a singleton a new game object is returned only if no previous one has been instantiated*/
	public static Game getInstance(String file){
		if(game == null)
			game = new Game(file);
		return game;
	}
	
	/*This returns all clubs from a particular competition*/
	public List<Club> getTeams(String competition){
		return ((League) db.findCompetition(competition)).getTeams();
	}
	
	/*This returns the squad of a particular club*/
	public List<Player> getSquad(String club){
		return db.findClub(club).getSquad();
	}
	
	/*This returns the fixtures of a particular club*/
	public List<Match> getFixtures(String club){
		return db.findClub(club).getFixtures();
	}
	
	/*This returns all players in the game*/
	public List<Player> getPlayers(){
		List<Player> players = new ArrayList<Player>();
		Iterator<Person> i = db.getPersonList().iterator();
		while(i.hasNext()){
			Person p = i.next();
			if(p instanceof Player){
				players.add((Player) p);
			}
		}
		return players;
	}
	
	//This returns all staff in the game that the user can employ*/
	public List<NonPlayer> getAllStaff(){
		List<NonPlayer> staff = new ArrayList<NonPlayer>();
		Iterator<Person> i = db.getPersonList().iterator();
		while(i.hasNext()){
			Person p = i.next();
			if(p instanceof NonPlayer && p.getId() != 0 && ((NonPlayer) p).getChairmanRole() != 20){
				staff.add((NonPlayer) p);
			}
		}
		return staff;
	}
	
	/*Gets the league table as a String array*/
	public String[][] getLeagueTable(String competition){
		int[][] table;
		League league = (League) db.findCompetition(competition);
		table = league.getTable();
		String[][] tableToReturn = new String[table.length][table[0].length];
		List<Club> teams = league.getTeams();
		List<String> teamNames = new ArrayList<String>();
		Iterator<Club> iterator = teams.iterator();
		while(iterator.hasNext()){
			teamNames.add(iterator.next().getName());
		}
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[0].length; j++){
				if(j == 1){
					tableToReturn[i][j] = teamNames.get(i);
				}
				else{
					tableToReturn[i][j] = table[i][j] + "";
				}
			}
		}
		league.sortTable(tableToReturn);
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[0].length; j++){
				if((j == 8) && (Integer.parseInt(tableToReturn[i][j]) > 0)){
					tableToReturn[i][j] = "+" + tableToReturn[i][j];
				}
			}
		}
		return tableToReturn;
	}
	
	/*Gets league fixtures*/
	public List<String> getLeagueFixtures(String name, String date){
		DateFormat format = new SimpleDateFormat("dd-MMM-yy");
		Calendar cal = new GregorianCalendar();
		try {
			cal.setTime((Date) format.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Match[][] leagueFixtures = null;
		List<String> matches = new ArrayList<String>();
		leagueFixtures = ((League) db.findCompetition(name)).getFixtures();
		for(int i = 0; i < leagueFixtures.length; i++){
			for(int j = 0; j < leagueFixtures[i].length; j++){
				if((leagueFixtures[i][j].getDate().get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) && (leagueFixtures[i][j].getDate().get(Calendar.MONTH) == cal.get(Calendar.MONTH)) && (leagueFixtures[i][j].getDate().get(Calendar.YEAR) == cal.get(Calendar.YEAR))){
					matches.add(leagueFixtures[i][j].toString());
				}
			}
		}
		return matches;
	}
	
	/*Gets information for a player profile*/
	public List<String> getPlayerProfileInfo(String name){
		return((Player) db.findPerson(name)).getPlayerProfileInfo(db.getDate());
	}
	
	/*Gets information for a nonplayer's profile*/
	public List<String> getStaffProfileInfo(String name){
		return ((NonPlayer) db.findPerson(name)).getStaffProfileInfo();
	}
	
	/*Gets a particular club's information*/
	public List<String> getClubInformation(String club){
		return db.findClub(club).getClubInformation();
	}
	
	/*Get the staff of a particular club*/
	public List<NonPlayer> getStaff(String club){
		return db.findClub(club).getStaff();
	}
	
	/*Get the transfer budget of a particular club*/
	public double getTransferBudget(String club){
		return db.findClub(club).getTransferBudget();
	}
	
	/*Returns a sorted list of names of nations in the game*/
	public List<String> getNationNames(){
		List<String> listOfNames = new ArrayList<String>();
		Iterator<Nation> i = db.getNationList().iterator();
		while(i.hasNext()){
			listOfNames.add(i.next().getName());
		}
		Collections.sort(listOfNames);
		return listOfNames;
	}
	
	/*Creates the user in the game*/
	public void createNewUser(String firstName, String surname, Calendar dob, String nationality, String club) {
		
		// Create the user as a new nonplayer object and add them to the list of person objects in the game
		this.userClub = db.findClub(club);
		db.setUserClub(userClub);
		NonPlayer user = new NonPlayer(firstName, surname, db.findNation(nationality), 5000, dob, 100, 200, 1, 20, 1, 1, 1, 1, db.findClub(club));
		user.setRole();
		List<Person> list = db.getPersonList();
		list.add(user);
		db.setPersonList(list);
		List<NonPlayer> staff = user.getCurrentClub().getStaff();
		staff.add(user);
		user.getCurrentClub().setStaff(staff);
		Iterator<NonPlayer> i = staff.iterator();
		NonPlayer chairman = null;
		while(i.hasNext()){
			chairman = i.next();
			if(chairman.getChairmanRole() == 20){
				break;
			}
		}
		db.getMessages().add(new Message((Calendar) db.getDate().clone(), "Welcome to " + this.userClub.getName(), "The chairman of " + this.userClub.getName() + ", " + chairman.getFirstName() + " " + chairman.getLastName() + ", would like to welcome you to the club!"));
		
		Iterator<Person> j = db.getPersonList().iterator();
		// If the user's club already has a manager remove them and assign them to no club
		while(j.hasNext()){
			Person p = j.next();
			if((p instanceof NonPlayer)&&(p.getCurrentClub().getId() == this.userClub.getId())&&(((NonPlayer)p).getManagerRole() == 20)&&(j.hasNext())){
				p.setCurrentClub(null);
				staff.remove(p);
				user.getCurrentClub().setStaff(staff);
				break;
			}
		}
	}
	
	public int continueGame(boolean processFixtures, List<String> players){
		int fixtures = 0;
		//if it is the end of the season move to next season
		if((db.getDate().get(Calendar.DAY_OF_MONTH) == 30) && (db.getDate().get(Calendar.MONTH) == 5)){
			Iterator<Competition> iCompetition = db.getCompetitionList().iterator();
			while(iCompetition.hasNext()){
				Competition c = iCompetition.next();
				if(c.getId() != 0){
					//set match schedule
					c.setMatchSchedule();					
					//move teams to correct divisions and give prize money to clubs
					if(c instanceof League){
						League league = (League) c;
						String[][] table = this.getLeagueTable(league.getName());
						for(int i = 0; i < league.getNumberOfTeams(); i++){
							db.findClub(table[i][1]).setBankBalance(db.findClub(table[i][1]).getBankBalance() + league.getPrizeMoney()[i]);
						}
						int numberOfTeamsRelegated = league.getNumberOfTeamsRelegated();
						if(league.getRelegatedTo().getId() == 0){
							for(int i = table.length - 1; i > (table.length - 1) - numberOfTeamsRelegated; i--){
								int teamToPromote = (int) (Math.random() * league.getRelegatedTo().getTeams().size());
								league.getRelegatedTo().getTeams().get(teamToPromote).setLeague(league);
								league.getRelegatedTo().getTeams().remove(teamToPromote);
							}
						}
						for(int i = table.length - 1; i > (table.length - 1) - numberOfTeamsRelegated; i--){
							db.findClub(table[i][1]).setLeague(league.getRelegatedTo());
						}
						int numberOfTeamsPromoted = league.getNumberOfTeamsPromoted();
						for(int i = 0; i < numberOfTeamsPromoted; i++){
							db.findClub(table[i][1]).setLeague(league.getPromotedTo());
						}
						if(league.getPlayoffs() != null){
							league.getPlayoffs().getRounds().get(league.getPlayoffs().getRounds().size() - 1).getWinners().get(0).setLeague(league.getPromotedTo());
						}
					}
				}
			}
			//set draw schedule
			//create league table
			//generate fixtures
			db.initialiseLeagues();
			db.initialiseCups();
			db.updatePlayers();
			//give clubs tv revenue money
			iCompetition = db.getCompetitionList().iterator();
			while(iCompetition.hasNext()){
				Competition c = iCompetition.next();
				if(c.getId() != 0){
					if(c instanceof League){
						League league = (League) c;
						Iterator<Club> iClub = league.getTeams().iterator();
						while(iClub.hasNext()){
							Club club = iClub.next();
							club.setBankBalance(club.getBankBalance() + league.getTvRevenue());
						}
					}
				}
			}
			//clear all rejected bids
			Iterator<Person> iPerson = db.getPersonList().iterator();
			while(iPerson.hasNext()){
				Person person = iPerson.next();
				//if person's contract is up make them leave the club
				if(person.getContractExpiry().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR)){
					person.setCurrentClub(null);
				}
				if(person instanceof Player){
					Player p = (Player) person;
					p.getRejectedBids().clear();
				}
			}
			
			Iterator<Club> iClub = db.getClubList().iterator();
			while(iClub.hasNext()){
				//offer contract to important players
				Club c = iClub.next();
				Iterator<Player> iPlayer = c.getSquad().iterator();
				while(iPlayer.hasNext()){
					Player p = iPlayer.next();
					if(p.getContractExpiry().get(Calendar.YEAR) - db.getDate().get(Calendar.YEAR) < 2 && p.getStatusOrdinal() != 5){
						c.offerContract(p, p.getWages(), new GregorianCalendar(db.getDate().get(Calendar.YEAR) + 3, 5, 30), p.getStatusOrdinal());
					}
				}
			}
		}
		//if there are no matches to be played move on to next day
		if(!(processFixtures)){
			db.getDate().add(Calendar.DATE, 1);
		}
		
		//check for any playoff that needs to be drawn and make the draw if necessary
		Match[][] leagueFixtures = null;
		List<Round> playoffRounds = null;
		Iterator<Competition> iterator = db.getCompetitionList().iterator();
		while(iterator.hasNext()){
			Competition c = iterator.next();
			if (c.getId() != 0 && c instanceof League) {
				if(((League) c).getPlayoffs() != null && (! processFixtures)){
					Iterator<Round> i = ((League) c).getPlayoffs().getRounds().iterator();
					while(i.hasNext()){
						Round r = i.next();
						if((r.getDrawDate().get(Calendar.DAY_OF_MONTH) == db.getDate().get(Calendar.DAY_OF_MONTH))
										&& (r.getDrawDate().get(Calendar.MONTH) == db.getDate().get(Calendar.MONTH))
										&& (r.getDrawDate().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR))){
							if(r.getRoundNumber() == 1){
								String[][] table = this.getLeagueTable(c.getName());
								r.getTeams().add(db.findClub(table[((League) c).getNumberOfTeamsPromoted()][1]));
								r.getTeams().add(db.findClub(table[((League) c).getNumberOfTeamsPromoted() + 1][1]));
								r.getTeams().add(db.findClub(table[((League) c).getNumberOfTeamsPromoted() + 2][1]));
								r.getTeams().add(db.findClub(table[((League) c).getNumberOfTeamsPromoted() + 3][1]));
								if(r.hasTwoLegs()){
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(0).clone(), r.getTeams().get(3), r.getTeams().get(0), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(3).getHomeGround()));
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(0).clone(), r.getTeams().get(2), r.getTeams().get(1), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(2).getHomeGround()));
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(1).clone(), r.getTeams().get(0), r.getTeams().get(3), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(0).getHomeGround(), true));
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(1).clone(), r.getTeams().get(1), r.getTeams().get(2), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(1).getHomeGround(), true));
								}
								else{
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(0).clone(), r.getTeams().get(3), r.getTeams().get(0), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(3).getHomeGround(), true));
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(0).clone(), r.getTeams().get(2), r.getTeams().get(1), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(2).getHomeGround(), true));
								}
							}
							else if(r.getRoundNumber() == 2){
								Round round = ((League) c).getPlayoffs().getRounds().get(0);
								r.getTeams().addAll(round.getWinners());
								if(r.hasTwoLegs()){
									
								}
								else{
									r.getMatches().add(new Match((Calendar) r.getRoundDate().get(0).clone(), r.getTeams().get(0), r.getTeams().get(1), -1, -1, ((League) c).getPlayoffs(), r.getTeams().get(0).getHomeGround(), true));
								}
							}
						}
					}
				}
				
				//if there are playoff matches to be played then play the matches
				leagueFixtures = ((League) c).getFixtures();
				if (((League) c).getPlayoffs() != null && (! processFixtures)) {
					playoffRounds = ((League) c).getPlayoffs().getRounds();
					Iterator<Round> iRounds = playoffRounds.iterator();
					while (iRounds.hasNext()) {
						Round r = iRounds.next();
						r.playMatches(db.getDate());
					}
				}
				
				//if there are league matches to be played then user's team is involved, make sure they have selected a valid team of 11 players
				for (int i = 0; i < leagueFixtures.length; i++) {
					for (int j = 0; j < leagueFixtures[i].length; j++) {
						if ((leagueFixtures[i][j].getDate().get(Calendar.DAY_OF_MONTH) == db.getDate().get(Calendar.DAY_OF_MONTH))
								&& (leagueFixtures[i][j].getDate().get(Calendar.MONTH) == db.getDate().get(Calendar.MONTH))
								&& (leagueFixtures[i][j].getDate().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR))
								&& (! leagueFixtures[i][j].isPostponed())) {
							if ((!(processFixtures)) && (c.getId() == userClub.getLeague().getId())) {
								if (leagueFixtures[i][j].getHomeTeam().getId() == this.userClub.getId()
										|| leagueFixtures[i][j].getAwayTeam().getId() == this.userClub.getId()) {
									List<Player> selectedTeam = new ArrayList<Player>();
									Iterator<String> iteratorPlayers = players.iterator();
									while (iteratorPlayers.hasNext()) {
										Player p = (Player) db.findPerson(iteratorPlayers.next());
										if (!(p.isInjured()))
											selectedTeam.add(p);
									}
									if(selectedTeam.size() != 11){
										db.getDate().add(Calendar.DATE, -1);
										return 2;
									}
									Collections.sort(selectedTeam);
									this.userClub.setSelectedTeam(selectedTeam);
								}
								fixtures = 1;
							}
							
							//play the league matches
							if (fixtures != 1) {
								if(leagueFixtures[i][j].getHomeTeam().getId() != this.userClub.getId())
									leagueFixtures[i][j].getHomeTeam().setSelectedTeam(leagueFixtures[i][j].getHomeTeam().getBestTeam(leagueFixtures[i][j].getHomeTeam().getAvailablePlayers()));
								if(leagueFixtures[i][j].getAwayTeam().getId() != this.userClub.getId())
									leagueFixtures[i][j].getAwayTeam().setSelectedTeam(leagueFixtures[i][j].getAwayTeam().getBestTeam(leagueFixtures[i][j].getAwayTeam().getAvailablePlayers()));
								leagueFixtures[i][j].generateResult();
							}
						}
					}
				}
				
				//if there are rescheduled league matches to be played then play them
				Iterator<Match> i = ((League) c).getRescheduledMatches().iterator();
				while(i.hasNext()){
					Match m = i.next();
					if((m.getDate().get(Calendar.DAY_OF_MONTH) == db.getDate().get(Calendar.DAY_OF_MONTH))
							&& (m.getDate().get(Calendar.MONTH) == db.getDate().get(Calendar.MONTH))
							&& (m.getDate().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR))
							&& (! m.isPostponed())){
						if(m.getHomeTeam().getId() != this.userClub.getId())
							m.getHomeTeam().setSelectedTeam(m.getHomeTeam().getBestTeam(m.getHomeTeam().getAvailablePlayers()));
						if(m.getAwayTeam().getId() != this.userClub.getId())
							m.getAwayTeam().setSelectedTeam(m.getAwayTeam().getBestTeam(m.getAwayTeam().getAvailablePlayers()));
						m.generateResult();
					}
				}
			}
			
			//check if any cup draws need to be made and if so make them
			else if(c instanceof Cup && c.getId() == 10){
				if(! processFixtures){
					Iterator<Round> i = ((Cup) c).getRounds().iterator();
					while(i.hasNext()){
						Round r = i.next();
						if((r.getDrawDate().get(Calendar.DAY_OF_MONTH) == db.getDate().get(Calendar.DAY_OF_MONTH))
										&& (r.getDrawDate().get(Calendar.MONTH) == db.getDate().get(Calendar.MONTH))
										&& (r.getDrawDate().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR))){
							List<Club> teams = new ArrayList<Club>();
							if(r.getRoundNumber() > 1){
								r.getTeams().addAll(((Cup) c).getRounds().get(r.getRoundNumber() - 2).getWinners());
							}
							teams.addAll(r.getTeams());
							while(teams.size() > 0){
								int teamToDraw = (int) (Math.random() * teams.size());
								Match m = new Match();
								m.setReplay(true);
								m.setDate((Calendar) r.getRoundDate().get(0).clone());
								m.setHomeTeam(teams.get(teamToDraw));
								teams.remove(teamToDraw);
								teamToDraw = (int) (Math.random() * teams.size());
								m.setAwayTeam(teams.get(teamToDraw));
								teams.remove(teamToDraw);
								m.setHomeScore(-1);
								m.setAwayScore(-1);
								m.setCompetition(c);
								m.setStadium(m.getHomeTeam().getHomeGround());
								r.getMatches().add(m);
								//check if clubs already have matches on these dates and if so postpone them
								Iterator<Match> iMatch = m.getHomeTeam().getFixtures().iterator();
								while(iMatch.hasNext()){
									Match match = iMatch.next();
									if(match.getDate().get(Calendar.DAY_OF_YEAR) == m.getDate().get(Calendar.DAY_OF_YEAR) && (! (match.isPostponed()))){
										//postpone original match
										match.setPostponed(true);
										Calendar cal = (Calendar) match.getDate().clone();
										while(match.getHomeTeam().checkForFixture(cal) || match.getAwayTeam().checkForFixture(cal)){
											if(cal.get(Calendar.DAY_OF_WEEK) == 7){
												cal.add(Calendar.DATE, 4);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 1){
												cal.add(Calendar.DATE, 3);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 3){
												cal.add(Calendar.DATE, 8);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 4){
												cal.add(Calendar.DATE, 7);
												
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 6){
												cal.add(Calendar.DATE, 5);
											}
										}
										Match newMatch = new Match(cal, match.getHomeTeam(), match.getAwayTeam(), match.getHomeScore(), match.getAwayScore(), match.getCompetition(), match.getStadium(), match.hasPenalties());
										((League) match.getCompetition()).getExtraMatchDates().add(cal);
										((League) match.getCompetition()).getRescheduledMatches().add(newMatch);
										break;
									}
								}
								iMatch = m.getAwayTeam().getFixtures().iterator();
								while(iMatch.hasNext()){
									Match match = iMatch.next();
									if(match.getDate().get(Calendar.DAY_OF_YEAR) == m.getDate().get(Calendar.DAY_OF_YEAR) && (! (match.isPostponed()))){
										//postpone original match
										match.setPostponed(true);
										Calendar cal = (Calendar) match.getDate().clone();
										while(match.getHomeTeam().checkForFixture(cal) || match.getAwayTeam().checkForFixture(cal)){
											if(cal.get(Calendar.DAY_OF_WEEK) == 7){
												cal.add(Calendar.DATE, 4);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 1){
												cal.add(Calendar.DATE, 3);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 3){
												cal.add(Calendar.DATE, 8);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 4){
												cal.add(Calendar.DATE, 7);
											}
											else if(cal.get(Calendar.DAY_OF_WEEK) == 6){
												cal.add(Calendar.DATE, 5);
											}
										}
										Match newMatch = new Match(cal, match.getHomeTeam(), match.getAwayTeam(), match.getHomeScore(), match.getAwayScore(), match.getCompetition(), match.getStadium(), match.hasPenalties());
										((League) match.getCompetition()).getExtraMatchDates().add(cal);
										((League) match.getCompetition()).getRescheduledMatches().add(newMatch);
										break;
									}
								}
							}
						}
					}
				}
				//play any cup matches
				if (! processFixtures) {
					Iterator<Round> iRounds = ((Cup) c).getRounds().iterator();
					while (iRounds.hasNext()) {
						Round r = iRounds.next();
						r.playMatches(db.getDate());
					}
				}
			}
		}
		if(db.getDate().get(Calendar.MONTH) == 0 || db.getDate().get(Calendar.MONTH) == 6 || db.getDate().get(Calendar.MONTH) == 7){
			//check if there are players for user clubs to buy and if so make offer for player
			Iterator<Person> i = db.getPersonList().iterator();
			DecimalFormat format = new DecimalFormat("000,000");
			while(i.hasNext()){
				Person p = i.next();
				if(p instanceof NonPlayer && ((NonPlayer) p).getManagerRole() == 20 && p.getCurrentClub() != null && p.getId() != 0 && ((NonPlayer) p).getShortlist().size() != 0){
					Player playerToBuy = null;
					int num = 4;
					while(playerToBuy == null){
						playerToBuy = getPlayerToBuy(p, num);
						num--;
						if(num == 0){
							break;
						}
					}
					if(playerToBuy == null){
						// do nothing
					}
					else if(playerToBuy.getCurrentClub().getId() == userClub.getId() && (!(playerToBuy.getBids().contains(p.getCurrentClub()))) && (!(playerToBuy.getRejectedBids().contains(p.getCurrentClub())))){
						db.getMessages().add(new Message((Calendar) db.getDate().clone(), p.getCurrentClub().getName() + " bid for " + playerToBuy.getFirstName() + (playerToBuy.getLastName().equals("") ? "" : " " + playerToBuy.getLastName()), p.getCurrentClub().getName() + " have made a �" + format.format(playerToBuy.getSaleValue()) + " bid to you for " + playerToBuy.getFirstName() + (playerToBuy.getLastName().equals("") ? "" : " " + playerToBuy.getLastName()) + "!"));
						playerToBuy.getBids().add(p.getCurrentClub());
					}
					else{
						//p.getCurrentClub().makeOffer(playerToBuy, playerToBuy.getSaleValue());
						if(p.getCurrentClub().offerContract(playerToBuy, 50000, new GregorianCalendar((db.getDate().get(Calendar.YEAR) + 3), 5, 30), 0))
							playerToBuy.transferPlayer(playerToBuy.getSaleValue(), p.getCurrentClub(), 50000, new GregorianCalendar((db.getDate().get(Calendar.YEAR) + 3), 5, 30), 0);
					}
				}
			}
		}
		db.updateAllPersonAttributes();
		db.updateAllClubAttributes();
		return fixtures;
	}
	
	private Player getPlayerToBuy(Person p, int num){
		Player playerToBuy = null;
		int currentAbility = 0;
		Iterator<Player> targets = ((NonPlayer) p).getShortlist().iterator();
		while(targets.hasNext()){
			Player player = targets.next();
			if(currentAbility < player.getCurrentAbility() && (player.getSaleValue() * num) <= p.getCurrentClub().getTransferBudget()){
				currentAbility = player.getCurrentAbility();
				playerToBuy = player;
			}
		}
		return playerToBuy;
	}
	
	public String getMessageBody(int index){
		return getMessages().get(index).getBody();
	}
	
	public Calendar getMessageDate(int index){
		return getMessages().get(index).getDate();
	}
	
	public void offerContractToPlayer(int value, String player, int wages, int lengthOfContract, int status){
		Calendar c = new GregorianCalendar(db.getDate().get(Calendar.YEAR), 5, 30);
		c.add(Calendar.YEAR, lengthOfContract);
		Calendar cal = new GregorianCalendar(db.getDate().get(Calendar.YEAR), db.getDate().get(Calendar.MONTH), db.getDate().get(Calendar.DATE));
		cal.add(Calendar.DATE, 3);
		if(userClub.offerContract((Player) db.findPerson(player), wages, c, status)){
			((Player) db.findPerson(player)).transferPlayer(value, userClub, (double) wages, c, status);			
			db.getMessages().add(new Message(cal, player + " Accepts Contract", player + " has accepted your contract offer!"));
		}
		else{
			db.getMessages().add(new Message(cal, player + " Rejects Contract", player + " has rejected your contract offer!"));
		}
	}
	
	public boolean makeOfferForPlayer(String manager, String player, int value){
		if(!(checkShortlistForPlayer(player, manager))){
			addPlayerToShortlist(player, manager);
		}
		Calendar c = new GregorianCalendar(db.getDate().get(Calendar.YEAR), db.getDate().get(Calendar.MONTH), db.getDate().get(Calendar.DATE));
		c.add(Calendar.DATE, 3);
		if(userClub.getTransferBudget() - value < 0){
			return false;
		}
		if(userClub.makeOffer((Player) db.findPerson(player), value)){
			db.getMessages().add(new Message(c, "Offer for " + player + " Accepted", db.findPerson(player).getCurrentClub().getName() + " have accepted your �" + value + " offer for " + player + "!  You now have permission to offer him a contract!"));
		}
		else{
			db.getMessages().add(new Message(c, "Offer for " + player + " Rejected", db.findPerson(player).getCurrentClub().getName() + " have rejected your �" + value + " offer for " + player + "!"));
		}
		return true;
	}
	
	public List<Message> getMessages() {
		Iterator<Message> iMessage = db.getMessages().iterator();
		List<Message> list = new ArrayList<Message>();
		while(iMessage.hasNext()){
			Message m = iMessage.next();
			if(db.getDate().getTimeInMillis() >= m.getDate().getTimeInMillis()){
				list.add(m);
			}
		}
		return list;
	}
	
	public List<Player> getShortlist(String manager){		
		return ((NonPlayer) db.findPerson(manager)).getShortlist();
	}
	
	public boolean checkShortlistForPlayer(String player, String manager){		
		if(((NonPlayer) db.findPerson(manager)).getShortlist().contains(db.findPerson(player)))
			return true;
		return false;
	}
	
	public void addPlayerToShortlist(String player, String manager){		
		List<Player> shortlist = ((NonPlayer) db.findPerson(manager)).getShortlist();
		shortlist.add((Player) db.findPerson(player));
		((Player) db.findPerson(player)).getInterested().add(db.findPerson(manager).getCurrentClub());
		((NonPlayer) db.findPerson(manager)).setShortlist(shortlist);
	}
	
	public void removePlayerFromShortlist(String player, String manager){		
		List<Player> shortlist = ((NonPlayer) db.findPerson(manager)).getShortlist();
		shortlist.remove((Player) db.findPerson(player));
		((Player) db.findPerson(player)).getInterested().remove(db.findPerson(manager).getCurrentClub());
		((NonPlayer) db.findPerson(manager)).setShortlist(shortlist);
	}
	
	public Calendar getDate(){
		return db.getDate();
	}
	
	public List<String> getClubFinances(String clubName){
		List<String> list = new ArrayList<String>();
		Club club = db.findClub(clubName);
		DecimalFormat format = new DecimalFormat("000,000");
		list.add("�" + format.format(club.getBankBalance()));
		list.add("�" + format.format(club.getTransferBudget()));
		return list;
	}
	
	public void sellPlayer(String playerName, String clubName){
		DecimalFormat format = new DecimalFormat("000,000");
		Player player = (Player) db.findPerson(playerName);
		db.getMessages().add(new Message((Calendar) db.getDate().clone(), playerName + " moves to " + clubName, playerName + " has moved to " + clubName + " for �" + format.format(player.getSaleValue()) + "!"));
		if(db.findClub(clubName).offerContract(player, 50000, new GregorianCalendar((db.getDate().get(Calendar.YEAR) + 3), 5, 30), 0))
			player.transferPlayer(player.getSaleValue(), db.findClub(clubName), 50000, new GregorianCalendar((db.getDate().get(Calendar.YEAR) + 3), 5, 30), 0);
	}
	
	public void rejectOffer(String playerName, String clubName){
		//System.out.println(clubName);
		((Player) db.findPerson(playerName)).getBids().remove(db.findClub(clubName));
		((Player) db.findPerson(playerName)).getRejectedBids().add(db.findClub(clubName));
	}
	
	public String[] getMatchDates(String leagueName){
		League league = (League) db.findCompetition(leagueName);
		List<Calendar> matchDates = league.getMatchDates();
		String[] matchDatesAsStrings = new String[matchDates.size()];
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
		for(int i = 0; i < matchDates.size(); i++){
			matchDatesAsStrings[i] = dateFormat.format(matchDates.get(i).getTime());
		}
		return matchDatesAsStrings;
	}
	
	public Match getUserMatch(){
		Club c = this.userClub;
		List<Match> fixtures = this.getFixtures(c.getName());
		Iterator<Match> i = fixtures.iterator();
		while(i.hasNext()){
			Match m = i.next();
			if((m.getDate().get(Calendar.DAY_OF_MONTH) == db.getDate().get(Calendar.DAY_OF_MONTH))
							&& (m.getDate().get(Calendar.MONTH) == db.getDate().get(Calendar.MONTH))
							&& (m.getDate().get(Calendar.YEAR) == db.getDate().get(Calendar.YEAR))
							&& (! m.isPostponed())){
				return m;
			}
		}
		return null;
	}
	
	public void save(String fileName) throws FileNotFoundException{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
			oos.writeObject(db);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		db.setSaved(true);
		db.setFileName(fileName);
	}
	
	public Object load(String file){
		Object object = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(file)));
			object = ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public String getUserClubName(){
		return userClub.getName();
	}
	
	public String getUserName(){
		Iterator<NonPlayer> i = userClub.getStaff().iterator();
		NonPlayer np = null;
		while(i.hasNext()){
			np = i.next();
			if(np.getManagerRole() == 20){
				break;
			}
		}
		return np.getFirstName() + " " + np.getLastName();
	}
	public boolean isSaved(){
		return db.isSaved();
	}
	
	public String getFileName(){
		return db.getFileName();
	}
	
	public static void destroyInstance(){
		game = null;
	}
}