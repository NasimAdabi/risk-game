package ca.concordia.app.risk.controller.delegate;

import org.springframework.beans.factory.annotation.Autowired;

import ca.concordia.app.risk.controller.dto.ContinentDto;
import ca.concordia.app.risk.controller.dto.CountryDto;
import ca.concordia.app.risk.services.MapService;

public class MapBusinessDelegate {

	@Autowired
	private MapService mapService;

	/**
	 * 
	 * @param continentDto
	 */
	public void addContinent(ContinentDto continentDto) throws Exception{
		mapService.addContinent(continentDto);

	}

	/**
	 * 
	 * @param continentDto
	 */
	public void removeContinent(ContinentDto continentDto) throws Exception{
		mapService.removeContinent(continentDto);
	}

	public void addCountry(CountryDto countryDto) throws Exception{
		mapService.addCountry(countryDto);
		
	}

	public void removeCountry(CountryDto countryDto) throws Exception{
		mapService.removeCountry(countryDto);
		
	}

}
