package me.lihq.game.living;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import jdk.nashorn.internal.parser.JSONParser;
import me.lihq.game.GameMain;
import me.lihq.game.models.Clue;

import me.lihq.game.models.Room;
import me.lihq.game.screen.elements.RoomTag;

import java.util.ArrayList;
import java.util.List;


/**
 * This class defines the player that the person playing the game will be represented by.
 */
public class Player extends AbstractPerson
{

    /**
     * The personality will be a percent score (0-100) 0 being angry, 50 being neutral, and 100 being happy/nice.
     */
    private int personalityLevel = 50;

    public List<Clue> collectedClues = new ArrayList<>();

    /**
     * The score the player has earned so far.
     */
    private int score = 0;

    /**
     * The room the player is currently exploring.
     */
    private Room currentRoom;

    /**
     * This is the constructor for player, it creates a new playable person
     * @param name - The name for the new player.
     * @param imgSrc - The image used to represent it.
     */
    public Player(String name, String imgSrc, int tileX, int tileY)
    {
        super(name, imgSrc, tileX, tileY);
        importDialogue("Player.JSON");
    }

    /**
     * This method will change the players personality by the given amount.
     * It will cap the personality between 0 and 100.
     * <p>
     * If the change takes it out of these bounds, it will change it to the min or max.
     *
     * @param change - The amount to change by, can be positive or negative
     */
    public void addToPersonality(int change)
    {
        personalityLevel = personalityLevel + change;

        if (personalityLevel < 0) {
            personalityLevel = 0;
        } else if (personalityLevel > 100) {
            personalityLevel = 100;
        }
    }


    /**
     * This Moves the player to a new tile.
     *
     * @param dir the direction that the player should move in.
     */
    public void move(Direction dir)
    {
        System.out.println(this.getTileCoordinates().x + ": " + this.getTileCoordinates().y);
        if (this.state != PersonState.STANDING) {
            return;
        }

        if (this.isOnTriggerTile() && dir.toString().equals(getRoom().getMatRotation(this.tileCoordinates.x, this.tileCoordinates.y))) {
            GameMain.me.getNavigationScreen().initialiseRoomChange();
            return;
        }

        if (!getRoom().isWalkableTile(this.tileCoordinates.x + dir.getDx(), this.tileCoordinates.y + dir.getDy())) {
            setDirection(dir);
            return;
        }

        initialiseMove(dir);
    }

    public void checkForClue()
    {
        int x = getTileCoordinates().x + getDirection().getDx();
        int y = getTileCoordinates().y + getDirection().getDy();


        if (!this.getRoom().isHidingPlace(x, y)) {
            return;
        }

        Clue clueFound = getRoom().getClue(x, y);
        if (clueFound != null) {
            GameMain.me.getNavigationScreen().setRoomTag(new RoomTag("You got a clue"));
            this.collectedClues.add(clueFound);
        } else {
            GameMain.me.getNavigationScreen().setRoomTag(new RoomTag("No clue here"));
        }
    }



    public boolean isOnTriggerTile() {
        return this.getRoom().isTriggerTile(this.tileCoordinates.x, this.tileCoordinates.y);

    }

    /**
     * Getter for personality.
     * @return - Returns the personality of this player.
     */
    public int getPersonality()
    {
        return this.personalityLevel;
    }



    /**
     * This takes the player at its current position, and automatically gets the transition data for the next room and applies it to the player and game
     */
    public void moveRoom()
    {
        if (isOnTriggerTile()) {
            Room.Transition newRoomData = this.getRoom().getTransitionData(this.getTileCoordinates().x, this.getTileCoordinates().y);

            this.setRoom(newRoomData.getNewRoom());


            if (newRoomData.newDirection != null) {
                this.setDirection(newRoomData.newDirection);
                this.updateTextureRegion();
            }

            this.setTileCoordinates(newRoomData.newTileCoordinates.x, newRoomData.newTileCoordinates.y);

            //TODO: Look into making a getter for the players Game this way we can do this.getGame() here instead of GameMain.

            GameMain.me.navigationScreen.updateTiledMapRenderer();
        }
    }

    

}
