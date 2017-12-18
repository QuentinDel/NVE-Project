/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

/**
 *
 * @author Rickard
 */

import Network.GameClient.GameClient;
import Network.Util.GameServerLite;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.controls.listbox.builder.ListBoxBuilder;
import de.lessvoid.nifty.controls.textfield.builder.TextFieldBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
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
        //nifty.fromXml("./Interface/scene.xml", "start", this);
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        nifty.addScreen("start", createStartScreen(this, "start"));
        nifty.addScreen("lobby", createLobbyScreen(this, "lobby"));
        nifty.addScreen("hud", createHudScreen(this, "hud"));
        nifty.gotoScreen("start");

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
    
    public void joinTeam(int team) {
        System.out.println("join team: "+team);
        sapp.joinTeam(team);
    }
    
    public void joinRed() {
        joinTeam(1);
    }
    
    public void joinBlue() {
        joinTeam(2);
    }
    
    public void refreshServerbrowser() {
        sapp.queueRefreshMessage();
    }
    
    public void gotoMenu() {
        sapp.getInputManager().setCursorVisible(true);
        nifty.gotoScreen("start");
    }
    
    public void gotoLobby() {
        sapp.getInputManager().setCursorVisible(true);
        nifty.gotoScreen("lobby");
    }
    
    public void gotoHud() {
        sapp.getInputManager().setCursorVisible(false);
        nifty.gotoScreen("hud");
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
    
    public Screen createStartScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("background") {{
                backgroundColor("#000f");
            }});
            layer(new LayerBuilder("foreground") {{
                backgroundColor("#0000");
                childLayoutVertical();
                panel(new PanelBuilder() {{
                    childLayoutCenter();
                    backgroundColor("#f008");
                    alignCenter();
                    height("15%");
                    width("75%");
                    style("nifty-panel-red");
                    control(new LabelBuilder("title", "Supraball ripoff"));
                }});
                panel(new PanelBuilder() {{
                    childLayoutCenter();
                    backgroundColor("#0f08");
                    alignCenter();
                    height("65%");
                    width("75%");
                    panel(new PanelBuilder() {{
                        childLayoutVertical();
                        alignCenter();
                        width("75%");
                        height("80%");
                        panel(new PanelBuilder() {{
                            childLayoutCenter();
                            height("100%");
                            width("80%");
                            control(new ListBoxBuilder("serverbrowser") {{
                                displayItems(15);
                                width("100%");
                                height("100%");
                            }});
                        }});
                        panel(new PanelBuilder() {{
                            childLayoutVertical();
                            height("20%");
                            width("100%");
                            alignCenter();
                            control(new ButtonBuilder("RefreshButton", "Refresh") {{
                                interactOnClick("refreshServerBrowser()");
                            }});
                            control(new TextFieldBuilder("PlayerName", "PlayerName") {{
                                maxLength(20);
                                height("30px");
                                width("20%");
                            }});
                        }});
                    }});
                }});
                panel(new PanelBuilder() {{
                    childLayoutHorizontal();
                    backgroundColor("#f008");
                    alignCenter();
                    height("20%");
                    width("75%");
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("JoinButton", "Join Server") {{
                            valignCenter();
                            alignCenter();
                            interactOnClick("joinServer()");
                        }});
                    }});
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("QuitButton", "Quit") {{
                            valignCenter();
                            alignCenter();
                            x(SizeValue.px(100));
                            y(SizeValue.px(100));

                            interactOnClick("quitGame()");
                        }});
                    }});
                }});
            }});
        }}.build(nifty);
    }
    
    public Screen createLobbyScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder() {{
                    alignCenter();
                    height("30%");
                }});
                panel(new PanelBuilder() {{
                    childLayoutHorizontal();
                    backgroundColor("#f008");
                    alignCenter();
                    height("20%");
                    width("75%");
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("JoinRed", "Join Red") {{
                            valignCenter();
                            alignCenter();
                            interactOnClick("joinRed()");
                        }});
                    }});
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        valignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("JoinBlue", "Join Blue") {{
                            valignCenter();
                            alignCenter();
                            x(SizeValue.px(100));
                            y(SizeValue.px(100));

                            interactOnClick("joinBlue()");
                        }});
                    }});
                }});
            }});
        }}.build(nifty);
    }
    
    public Screen createHudScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("foreground") {{
                
            }});
        }}.build(nifty);
    }

}
