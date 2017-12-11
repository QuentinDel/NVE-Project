/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppStates;

/**
 *
 * @author Rickard
 */

import Network.GameClient.GameClient;
import Network.GameServerLite;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class Menu extends BaseAppState implements ScreenController {

    //TODO PROPERLY ADD ASSETS, IT CANT FIND THE START-BACKGROUND (but why can it find the xml files?!?)
    //TODO Listen to the playername textfield, currently writing there doesnt do anything
    
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private GameClient sapp;
    
    private GameServerLite currentServer;
    private String playerName;
    
    @Override
    public void initialize(Application app) {
        System.out.println("Menu: initialize");
        sapp = (GameClient) app;
    }
    
    @Override
    protected void cleanup(Application app) {
        System.out.println("Menu: cleanup");
    }
    
    @Override
    protected void onEnable() {
        System.out.println("Menu: onEnable");
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                sapp.getAssetManager(),
                sapp.getInputManager(),
                sapp.getAudioRenderer(),
                sapp.getGuiViewPort());
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/scene.xml", "start", this);

        // attach the nifty display to the gui view port as a processor
        sapp.getGuiViewPort().addProcessor(niftyDisplay);
        sapp.getInputManager().setCursorVisible(true);
        
        populateServerbrowser(new LinkedBlockingQueue<GameServerLite>()); //Only here for testing purposes
        
    }
    
    public void populateServerbrowser(Collection<GameServerLite> servers) {
        ListBox<GameServerLite> listBox = (ListBox<GameServerLite>) nifty.getCurrentScreen().findNiftyControl("serverbrowser", ListBox.class);
        if (listBox != null) {
            listBox.clear();
            listBox.addAllItems(servers);
            listBox.addItem(new GameServerLite("127.0.0.1", "Castle",  8000,  4, 0, 2, 3)); //Hardcoded servers for testing
            listBox.addItem(new GameServerLite("127.0.0.1", "Beach",   8000,  1, 1, 4, 3));
            listBox.addItem(new GameServerLite("127.0.0.1", "Football", 8000, 0, 0, 2, 0));
        }
    }

    /**
     * When the selection of the ListBox changes this method is called.
     */
    @NiftyEventSubscriber(id="serverbrowser")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<GameServerLite> event) {
        List<GameServerLite> selection = event.getSelection();
        for (GameServerLite selectedItem : selection) {
            currentServer = selectedItem;
        }
    }
    
    public void quitGame() {
        System.out.println("Menu: Quitting game!");
        sapp.stop();
    }
    
    public void joinServer() {
        if (currentServer != null) {
            TextField input = nifty.getCurrentScreen().findNiftyControl("PlayerName", TextField.class);
            String playerName = "Bob"; //Default name
            if (input != null) {
                playerName = input.getRealText();
            }
            sapp.joinServer(currentServer, playerName);
        } else {
            System.out.println("No server selected");
        }
    }
    
    public void refreshServerbrowser() {
        sapp.queueRefreshMessage();
    }
    
    @Override
    protected void onDisable() {
        System.out.println("Menu: onDisable");
        sapp.getGuiViewPort().removeProcessor(niftyDisplay);
        sapp.getInputManager().setCursorVisible(false);
    }
    
    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    @Override
    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    @Override
    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

    public void quit(){
        nifty.gotoScreen("end");
    }

}
