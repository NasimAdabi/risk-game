package ca.concordia.app.risk.model.modes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;

import ca.concordia.app.risk.exceptions.RiskGameRuntimeException;
import ca.concordia.app.risk.model.cache.RunningGame;
import ca.concordia.app.risk.model.dao.CountryDaoImpl;
import ca.concordia.app.risk.model.xmlbeans.CountryModel;
import ca.concordia.app.risk.model.xmlbeans.PlayerModel;
import ca.concordia.app.risk.services.GameService;

/**
 * This is an abstract class which contains all strategies inside the game
 * 
 * @author i857625
 *
 */
public abstract class AbstractStrategy implements Strategy {

  /**
   * Type of cards
   */
  enum cards {
    Infantry, Cavalry, Artillery
  }

  /**
   * player model
   */
  PlayerModel playerModel;

  /**
   * Constructor
   * 
   * @param playerModel player model
   */
  public AbstractStrategy(PlayerModel playerModel) {
    this.setPlayerModel(playerModel);
  }

  /**
   * This method contains attack strategy implementation
   * 
   * @param countryModelFrom attacking country model
   * @param countryModelTo   defender country model
   * @param numDice          number of dice
   */
  @Override
  public void attack(CountryModel countryModelFrom, CountryModel countryModelTo, String numDice) {
    // check max number of dice doesn't exceed 3 and not less than 1
    // System.out.println(countryModelFrom.getName() + countryModelTo.getName());
    if (numDice.equalsIgnoreCase("-allout")) {
      RunningGame.getInstance().setAllOut(true);
      int maxDiceAttacker = 3;
      if (countryModelFrom.getNumberOfArmies() <= maxDiceAttacker)
        maxDiceAttacker = countryModelFrom.getNumberOfArmies() - 1;
      RunningGame.getInstance().setAttackCountryNameFrom(countryModelFrom.getName());
      RunningGame.getInstance().setAttackCountryNameTo(countryModelTo.getName());
      RunningGame.getInstance().setNumDiceAttacker(maxDiceAttacker);

      int maxDiceDefender = 2;
      if (maxDiceAttacker < maxDiceDefender)
        maxDiceDefender = maxDiceAttacker;
      if (countryModelTo.getNumberOfArmies() < maxDiceDefender)
        maxDiceDefender = countryModelTo.getNumberOfArmies();

      defend(String.valueOf(maxDiceDefender));

    } else {
      if (Integer.parseInt(numDice) > 3 || Integer.parseInt(numDice) < 1) {
        throw new RiskGameRuntimeException("Number of dice should be between 1 and 3");
      }
      // check number of armies within the country
      if (countryModelFrom.getNumberOfArmies() <= Integer.parseInt(numDice)) {
        throw new RiskGameRuntimeException(
            "Number of dice should be less than the number of armies allocated within the country");
      }

      // save countries in running game
      RunningGame.getInstance().setAttackCountryNameFrom(countryModelFrom.getName());
      RunningGame.getInstance().setNumDiceAttacker(Integer.parseInt(numDice));
      RunningGame.getInstance().setAttackCountryNameTo(countryModelTo.getName());
    }

  }

  /**
   * This method contains defend strategy implementation
   * 
   * @param numDice number of dice
   */
  @Override
  public void defend(String numDice) {
    // get Country Model of attackFrom and attackTo
    CountryDaoImpl countryDaoImpl = new CountryDaoImpl();
    CountryModel countryModelAttackFrom = countryDaoImpl.findByName(RunningGame.getInstance(),
        RunningGame.getInstance().getAttackCountryNameFrom());
    CountryModel countryModelAttackTo = countryDaoImpl.findByName(RunningGame.getInstance(),
        RunningGame.getInstance().getAttackCountryNameTo());

    // roll the Attacker dice
    do {
      Random random = new Random();

      int numDice1 = random.nextInt(5) + 1;
      int numDice2 = 0;
      if (RunningGame.getInstance().getNumDiceAttacker() >= 2) {
        numDice2 = random.nextInt(5) + 1;
      }

      int numDice3 = 0;
      if (RunningGame.getInstance().getNumDiceAttacker() == 3) {
        numDice3 = random.nextInt(5) + 1;
      }

      int[] attackerDice;
      if (RunningGame.getInstance().getNumDiceAttacker() == 3) {
        attackerDice = new int[] { numDice1, numDice2, numDice3 };
      } else if (RunningGame.getInstance().getNumDiceAttacker() == 2) {
        attackerDice = new int[] { numDice1, numDice2 };
      } else {
        attackerDice = new int[] { numDice1 };
      }

      // save the dice in running game to compare later
      // RunningGame.getInstance().setAttackerDice(attackerDice);

      // roll dice
      int numDice1D = random.nextInt(5) + 1;
      int numDice2D = 0;
      if (Integer.parseInt(numDice) == 2) {
        numDice2D = random.nextInt(5) + 1;
      }

      int[] defenderDice;
      if (Integer.parseInt(numDice) == 2) {
        defenderDice = new int[] { numDice1D, numDice2D };
      } else {
        defenderDice = new int[] { numDice1D };
      }

      // sort Arrays
      Arrays.sort(attackerDice);
      Arrays.sort(defenderDice);

      for (int i = 0, j = attackerDice.length - 1, tmp; i < j; i++, j--) {
        tmp = attackerDice[i];
        attackerDice[i] = attackerDice[j];
        attackerDice[j] = tmp;
      }

      for (int i = 0, j = defenderDice.length - 1, tmp; i < j; i++, j--) {
        tmp = defenderDice[i];
        defenderDice[i] = defenderDice[j];
        defenderDice[j] = tmp;
      }

      System.out.println("Attacker Dice Roll Results");
      for (int die : attackerDice) {
        System.out.print(die + " ");
      }
      System.out.println();
      System.out.println("Defender Dice Roll Results");
      for (int die : defenderDice) {
        System.out.print(die + " ");
      }
      System.out.println();

      // compare
      switch (numDice) {
      // in case defender use one die
      case "1":
        if (attackerDice[0] > defenderDice[0]) {
          countryModelAttackTo.setNumberOfArmies(countryModelAttackTo.getNumberOfArmies() - 1);
        } else {
          countryModelAttackFrom.setNumberOfArmies(countryModelAttackFrom.getNumberOfArmies() - 1);
        }
        break;

      // in case defender use two dice
      case "2":
        if (attackerDice[0] > defenderDice[0]) {
          countryModelAttackTo.setNumberOfArmies(countryModelAttackTo.getNumberOfArmies() - 1);
        } else {
          countryModelAttackFrom.setNumberOfArmies(countryModelAttackFrom.getNumberOfArmies() - 1);
        }

        if (attackerDice[1] > defenderDice[1]) {
          if (countryModelAttackTo.getNumberOfArmies() > 0) {
            countryModelAttackTo.setNumberOfArmies(countryModelAttackTo.getNumberOfArmies() - 1);
          }
        } else {
          if (countryModelAttackFrom.getNumberOfArmies() > 0) {
            countryModelAttackFrom.setNumberOfArmies(countryModelAttackFrom.getNumberOfArmies() - 1);
          }
        }
        break;

      default:
      }

      if (RunningGame.getInstance().isAllOut()
          && countryModelAttackFrom.getNumberOfArmies() < (RunningGame.getInstance().getNumDiceAttacker())) {
        RunningGame.getInstance().setNumDiceAttacker(countryModelAttackFrom.getNumberOfArmies() - 1);
        numDice = String.valueOf(countryModelAttackFrom.getNumberOfArmies() - 1);
      }

      if (RunningGame.getInstance().isAllOut()
          && countryModelAttackTo.getNumberOfArmies() < Integer.parseInt(numDice)) {
        numDice = String.valueOf(countryModelAttackTo.getNumberOfArmies());
      }

      // Check if defender won
      if (countryModelAttackFrom.getNumberOfArmies() == 1) {
        RunningGame.getInstance().setAttackerWin(false);
        RunningGame.getInstance().setDefenderWin(true);
        // RunningGame.getInstance().setAttackCompleted(true);
        RunningGame.getInstance().setAllOut(false);
        System.out.println("Attacker " + this.getPlayerModel().getName() + " loses");

        // Check if Attacker won
      } else if (countryModelAttackTo.getNumberOfArmies() == 0) {
        RunningGame.getInstance().setAttackerWin(true);
        RunningGame.getInstance().setDefenderWin(false);
        RunningGame.getInstance().setAttackCompleted(true);
        RunningGame.getInstance().setAllOut(false);
        countryModelAttackTo.setPlayerId(this.getPlayerModel().getId());
        if (!RunningGame.getInstance().isCardGiven()) {
          giveCard();
          RunningGame.getInstance().setCardGiven(true);
        }
        // System.out.println(RunningGame.getInstance().getCurrentPlayer().getPlayerModel().getStrategy()
        // + numDice);
        if (!"Human".equalsIgnoreCase(RunningGame.getInstance().getCurrentPlayer().getPlayerModel().getStrategy())) {
          attackMove("1");
        }
        // System.out.println(
        // "Attacker " + this.getPlayerModel().getName() + " has conquered country " +
        // countryModelAttackTo.getName());
      }

      RunningGame.getInstance().getSubject().markAndNotify();

    } while (RunningGame.getInstance().isAllOut()
        && countryModelAttackFrom.getNumberOfArmies() >= (RunningGame.getInstance().getNumDiceAttacker()));

    RunningGame.getInstance().setNumDiceAttacker(0);
    String gameCompleted = RunningGame.getInstance().checkGameCompleted();
    if (!gameCompleted.equalsIgnoreCase("no")) {
      if (!gameCompleted.equalsIgnoreCase("Draw")) {
        GameService.gameWinner = RunningGame.getInstance().getCurrentPlayer().getPlayerModel().getStrategy();
        System.out.println(
            "The game was completed. Player " + RunningGame.getInstance().getCurrentPlayer().getPlayerModel().getName()
                + " won with strategy " + RunningGame.getInstance().getCurrentPlayer().getPlayerModel().getStrategy());
      } else {
        GameService.gameWinner = "Draw";
        System.out.println("The game was a Draw");
      }
      RunningGame.reset();
    }

  }

  /**
   * This method contains fortification strategy implementation
   * 
   * @param countryModelFrom the country fortify from model
   * @param countryModelTo   the country fortify to model
   * @param numberOfArmies   number of armies to fortify
   */
  @Override
  public void fortify(CountryModel countryModelFrom, CountryModel countryModelTo, int numberOfArmies) {

    PlayerModel activePlayerModel = this.getPlayerModel();
    CountryDaoImpl countryDaoImpl = new CountryDaoImpl();

    AllDirectedPaths<String, DefaultEdge> allDirectedPaths = new AllDirectedPaths<>(
        RunningGame.getInstance().getGraph());
    List<GraphPath<String, DefaultEdge>> allPaths = allDirectedPaths.getAllPaths(countryModelFrom.getName(),
        countryModelTo.getName(), false, 10);
    int counter = 0;
    for (GraphPath<String, DefaultEdge> graphPath : allPaths) {
      List<String> countriesInPath = graphPath.getVertexList();
      boolean connected = true;
      for (String countryName : countriesInPath) {
        CountryModel countryModel = countryDaoImpl.findByName(RunningGame.getInstance(), countryName);
        if (activePlayerModel.getId() != countryModel.getPlayerId()) {
          connected = false;
          break;
        }
      }
      if (connected) {
        counter++;
        break;
      }
    }

    if (counter == 0) {

      List<GraphPath<String, DefaultEdge>> allPathsReverse = allDirectedPaths.getAllPaths(countryModelTo.getName(),
          countryModelFrom.getName(), false, 10);
      int counterNew = 0;

      for (GraphPath<String, DefaultEdge> graphPath : allPathsReverse) {
        List<String> countriesInPath = graphPath.getVertexList();
        boolean connected = true;
        for (String countryName : countriesInPath) {
          CountryModel countryModel = countryDaoImpl.findByName(RunningGame.getInstance(), countryName);
          if (activePlayerModel.getId() != countryModel.getPlayerId()) {
            connected = false;
            break;
          }
        }
        if (connected) {
          counterNew++;
          break;
        }
      }
      if (counterNew == 0) {
        throw new RiskGameRuntimeException("No Path Available!");
      } else {
        countryModelTo.setNumberOfArmies(countryModelTo.getNumberOfArmies() - numberOfArmies);
        countryModelFrom.setNumberOfArmies(countryModelFrom.getNumberOfArmies() + numberOfArmies);
        RunningGame.getInstance().moveToNextPlayer();
        RunningGame.getInstance().exchangeCardsInitialization();
      }
    } else {
      countryModelFrom.setNumberOfArmies(countryModelFrom.getNumberOfArmies() - numberOfArmies);
      countryModelTo.setNumberOfArmies(countryModelTo.getNumberOfArmies() + numberOfArmies);
      RunningGame.getInstance().moveToNextPlayer();
      RunningGame.getInstance().exchangeCardsInitialization();
    }

  }

  /**
   * This method contains reinforcement strategy implementation
   * 
   * @param countryModel   country model
   * @param numberOfArmies number of armies to reinforce
   */
  @Override
  public void reinforce(CountryModel countryModel, int numberOfArmies) {

    if (this.getPlayerModel().getReinforcementNoOfArmies() >= numberOfArmies) {
      countryModel.setNumberOfArmies(countryModel.getNumberOfArmies() + numberOfArmies);
      this.getPlayerModel()
          .setReinforcementNoOfArmies(this.getPlayerModel().getReinforcementNoOfArmies() - numberOfArmies);

      if (this.getPlayerModel().getReinforcementNoOfArmies() == 0) {
        System.out.println("Reinforcement is entirely complete now!");
        RunningGame.getInstance().setReinforceCompleted(true);
        this.getPlayerModel().setPlayingPhase("Attack");
        RunningGame.getInstance().getSubject().markAndNotify();
      }
      System.out
          .println("You have " + this.getPlayerModel().getReinforcementNoOfArmies() + " armies left to reinforce");

    } else {
      throw new RiskGameRuntimeException("Please reduce number of armies, you have "
          + this.getPlayerModel().getReinforcementNoOfArmies() + " armies left to reinforce");
    }
  }

  /**
   * This method used to move the number of armies from attacker to the defender
   * country if the attacker has conquered the defender.
   * 
   * @param num number of armies to move from attackers country to defenders
   *            country
   */
  public void attackMove(String num) {
    if (!RunningGame.getInstance().isAttackCompleted()) {
      throw new RiskGameRuntimeException("You've not conquered any country to move the armies");
    }
    // if (RunningGame.getInstance().isDefenderWin()) {
    // throw new RiskGameRuntimeException("Defender had won, you can't make a
    // move");
    // }
    // check num are not greater than the number of armies in the attackCountryFrom
    CountryDaoImpl countryDaoImpl = new CountryDaoImpl();
    CountryModel countryModelAttackFrom = countryDaoImpl.findByName(RunningGame.getInstance(),
        RunningGame.getInstance().getAttackCountryNameFrom());
    if (countryModelAttackFrom.getNumberOfArmies() - 1 < Integer.parseInt(num)) {
      throw new RiskGameRuntimeException("You don't have enough armies, please reduce the number of armies");
    }
    // at least one army should be there or more depends on the number of dice
    // if (countryModelAttackFrom.getNumberOfArmies() - 1 < Integer.parseInt(num)) {
    if (RunningGame.getInstance().getAttackerDice().size() > Integer.parseInt(num)) {
      throw new RiskGameRuntimeException(
          "You have to move atleast the number of armies equivalent to the dice you used to conquer! The number of dice was "
              + RunningGame.getInstance().getAttackerDice().size());
    }
    // move armies
    CountryModel countryModelAttackTo = countryDaoImpl.findByName(RunningGame.getInstance(),
        RunningGame.getInstance().getAttackCountryNameTo());
    countryModelAttackFrom.setNumberOfArmies(countryModelAttackFrom.getNumberOfArmies() - Integer.parseInt(num));
    countryModelAttackTo.setNumberOfArmies(countryModelAttackTo.getNumberOfArmies() + Integer.parseInt(num));
    RunningGame.getInstance().setAttackCompleted(false);
    RunningGame.getInstance().getSubject().markAndNotify();
  }

  /**
   * This method is used for randomly distribute the cards to players
   */
  private void giveCard() {
    Random random = new Random();
    int num = random.nextInt(3);
    this.getPlayerModel().getCards().getList().add(String.valueOf(cards.values()[num]));
  }

  /**
   * This method sets player model
   * 
   * @param playerModel player model
   */
  public void setPlayerModel(PlayerModel playerModel) {
    this.playerModel = playerModel;
  }

  /**
   * This method returns player model
   * 
   * @return
   */
  public PlayerModel getPlayerModel() {
    return playerModel;
  }
}
