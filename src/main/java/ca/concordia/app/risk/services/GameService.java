package ca.concordia.app.risk.services;

import java.io.File;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.BeanUtils;

import ca.concordia.app.risk.controller.dto.GameStarterDto;
import ca.concordia.app.risk.model.cache.RunningGame;
import ca.concordia.app.risk.model.dao.PlayerDaoImpl;
import ca.concordia.app.risk.model.xmlbeans.GameModel;
import ca.concordia.app.risk.model.xmlbeans.ObjectFactory;
import ca.concordia.app.risk.model.xmlbeans.PlayerModel;
import ca.concordia.app.risk.utility.DateUtils;

/**
 * 
 * @author i857625
 *
 */
public class GameService {

	/**
	 * 
	 */
	ObjectFactory objectFactory;

	/**
	 * 
	 */
	public GameService() {
		this.setObjectFactory(new ObjectFactory());
	}

	/**
	 * 
	 * @param gameStarterDTO
	 * @throws Exception
	 */
	public void initGame(GameStarterDto gameStarterDTO) throws Exception {
		RunningGame.reset();
		XMLGregorianCalendar xmlGregorianCalendar = DateUtils.getXMLDateTime(new Date());
		RunningGame.getInstance().setCreatedDate(xmlGregorianCalendar);
		RunningGame.getInstance().setLastSavedDate(xmlGregorianCalendar);
		RunningGame.getInstance().setAutoSave(gameStarterDTO.isAutoSave());
		RunningGame.getInstance().setPlayers(this.getObjectFactory().createPlayersModel());
		for (int i = 0; i < gameStarterDTO.getPlayersList().size(); i++) {
			PlayerModel playerModel = this.getObjectFactory().createPlayerModel();
			BeanUtils.copyProperties(gameStarterDTO.getPlayersList().get(i), playerModel);
			PlayerDaoImpl playerDaoImpl = new PlayerDaoImpl();
			playerDaoImpl.assignID(playerModel);
			RunningGame.getInstance().getPlayers().getList().add(playerModel);
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void saveGame() throws Exception {
		XMLGregorianCalendar xmlGregorianCalendar = DateUtils.getXMLDateTime(new Date());
		RunningGame.getInstance().setLastSavedDate(xmlGregorianCalendar);
		try {
			File file = new File("saved/game.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(GameModel.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(RunningGame.getInstance(), file);
		} catch (JAXBException e) {
			throw new Exception("Game caanot be saved");
		}
	}

	/**
	 * 
	 * @param objectFactory
	 */
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/**
	 * 
	 * @return
	 */
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}
}