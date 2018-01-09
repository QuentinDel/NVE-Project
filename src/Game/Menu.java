/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameClient.GameClient;
import Network.Util;
import Network.Util.ChatMessage;
import Network.Util.GameServerLite;
import static Network.Util.MAX_MESSAGE_LENGTH;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.Collection;
import java.util.List;

/**
 * This appstate handles the GUI (menus, HUD etc)
 * 
 * @author Rickard
 */

public class Menu extends BaseAppState implements ScreenController {
    
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private GameClient sapp;
    
    private GameServerLite currentServer;
    private String playerName = "PlayerName";
    
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
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        nifty.addScreen("start", createStartScreen(this, "start"));
        nifty.addScreen("lobby", createLobbyScreen(this, "lobby"));
        nifty.addScreen("hudID", createHudScreen(this, "hud"));
        nifty.addScreen("pause", createPauseScreen(this, "pause"));
        nifty.gotoScreen("start");

        // attach the nifty display to the gui view port as a processor
        sapp.getGuiViewPort().addProcessor(niftyDisplay);
        sapp.getInputManager().setCursorVisible(true);
    }
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String binding, boolean isPressed, float tpf) {
            if (binding.equals("EnableChat")) {
                //The user wants to type something in the chat,
                //delete this binding, add a binding for sending the message and put focus on the textfield
                sapp.enableChat();
                sapp.getInputManager().deleteMapping("EnableChat");
                sapp.getInputManager().addMapping("sendChatMessage", new KeyTrigger(KeyInput.KEY_RETURN));
                sapp.getInputManager().addListener(actionListener, "sendChatMessage");
                TextField input = (TextField) nifty.getCurrentScreen().findNiftyControl("chatMessage", TextField.class);
                input.enable();
                input.setFocus();
            } else if (binding.equals("sendChatMessage")) {
                //The user wants to send a chatmessage,
                //delete this binding, add a binding for typing again and send the message if it is non-empty
                sapp.disableChat();
                sapp.getInputManager().deleteMapping("sendChatMessage");
                sapp.getInputManager().addMapping("EnableChat", new KeyTrigger(KeyInput.KEY_Y));
                sapp.getInputManager().addListener(actionListener, "EnableChat");

                TextField input = nifty.getCurrentScreen().findNiftyControl("chatMessage", TextField.class);
                String message = input.getRealText();
                message = playerName + ": " + message;
                input.setText("");
                input.disable();
                ListBox<String> chatBox = (ListBox<String>) nifty.getCurrentScreen().findNiftyControl("chatBox", ListBox.class);
                if (!message.equals("")) {
                    sapp.queueGameServerMessage(new ChatMessage(message));
                }
            }
        }
    };
    
    public void addMessage(String message) {
        ListBox<String> chatBox = (ListBox<String>) nifty.getCurrentScreen().findNiftyControl("chatBox", ListBox.class);
        chatBox.addItem(message);
    }
    
    private void removeChatControls() {
        if (sapp.getInputManager().hasMapping("EnableChat")) {
            sapp.getInputManager().deleteMapping("EnableChat");
        }
        if (sapp.getInputManager().hasMapping("chatMessage")) {
            sapp.getInputManager().deleteMapping("chatMessage");
        }
    }

    //Populates the serverlist
    public void populateServerbrowser(Collection<GameServerLite> servers) {
        ListBox<GameServerLite> listBox = (ListBox<GameServerLite>) nifty.getCurrentScreen().findNiftyControl("serverbrowser", ListBox.class);
        if (listBox != null) {
            listBox.clear();
            listBox.addAllItems(servers);
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
            if (input != null) {
                playerName = input.getRealText();
            }
            sapp.joinServer(currentServer, playerName);
        } else {
            System.out.println("No server selected");
        }
    }
    
    public void joinTeam(int team) {
        sapp.joinTeam(team);
    }
    
    public void joinRed() {
        joinTeam(Util.RED_TEAM_ID);
    }
    
    public void joinBlue() {
        joinTeam(Util.BLUE_TEAM_ID);
    }
    
    public void refreshServerbrowser() {
        sapp.queueRefreshMessage();
    }
    
    public void resume() {
        sapp.resume();
    }
    
    public void quitServer() {
        sapp.disconnectFromGame();
    }
    
    public void gotoMenu() {
        sapp.getInputManager().setCursorVisible(true);
        nifty.gotoScreen("start");
        removeChatControls();
    }
    
    public void gotoLobby() {
        sapp.getInputManager().setCursorVisible(true);
        nifty.gotoScreen("lobby");
        removeChatControls();
    }
    
    public void gotoHud() {
        sapp.getInputManager().setCursorVisible(false);
        nifty.gotoScreen("hud");
        
        //Init chat controls
        sapp.getInputManager().addMapping("EnableChat", new KeyTrigger(KeyInput.KEY_Y));
        sapp.getInputManager().addListener(actionListener, "EnableChat");
    }
    
    public void gotoPause() {
        sapp.getInputManager().setCursorVisible(true);
        nifty.gotoScreen("pause");
        removeChatControls();
    }
    
    public void setScore(int teamID, int newScore) {
        switch (teamID) {
            case Util.BLUE_TEAM_ID:
                setBlueScore(newScore);
                break;
            case Util.RED_TEAM_ID:
                setRedScore(newScore);
                break;
            default:
                System.out.println("Invalid teamID: " + teamID);
                break;
        }
    }
    
    public void setHealth(int newHealth) {
        Element niftyElement = nifty.getScreen("hud").findElementById("health");
        if (niftyElement != null) {
            niftyElement.getRenderer(TextRenderer.class).setText(String.valueOf(newHealth));
        }
    }
    
    private void setRedScore(int newScore) {
        Element niftyElement = nifty.getScreen("hud").findElementById("redscore");
        if (niftyElement != null) {
            niftyElement.getRenderer(TextRenderer.class).setText(String.valueOf(newScore));
        }
    }
    
    private void setBlueScore(int newScore) {
        Element niftyElement = nifty.getScreen("hud").findElementById("bluescore");
        if (niftyElement != null) {
            niftyElement.getRenderer(TextRenderer.class).setText(String.valueOf(newScore));
        }
    }
    
    public void setTimer(int newTime) {
        Element niftyElement = nifty.getScreen("hud").findElementById("time");
        if (niftyElement != null) {
            niftyElement.getRenderer(TextRenderer.class).setText(String.valueOf(newTime));
        }
    }
    
    @Override
    protected void onDisable() {
        System.out.println("Menu: onDisable");
        sapp.getGuiViewPort().removeProcessor(niftyDisplay);
        sapp.getInputManager().setCursorVisible(false);
    }
    
    @Override
    public void update(float tpf) {
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
    
    //This function returns a start Screen
    public Screen createStartScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("background") {{
                backgroundColor("#000f");
            }});
            layer(new LayerBuilder("foreground") {{
                backgroundColor("#0000");
                childLayoutVertical();
                panel(new PanelBuilder() {{ //Game title
                    childLayoutCenter();
                    backgroundColor("#f008");
                    alignCenter();
                    height("15%");
                    width("75%");
                    style("nifty-panel-red");
                    control(new LabelBuilder("title", "Supraball ripoff"));
                }});
                panel(new PanelBuilder() {{ //Serverbrowser panel
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
                            control(new ListBoxBuilder("serverbrowser") {{ //serverbrowser
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
                            control(new ButtonBuilder("RefreshButton", "Refresh") {{ //Button for refreshing serverlist
                                interactOnClick("refreshServerBrowser()");
                            }});
                            control(new TextFieldBuilder("PlayerName", playerName) {{ //Textfield for playername
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
                        control(new ButtonBuilder("JoinButton", "Join Server") {{ //Button for joining the selected server
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
                        control(new ButtonBuilder("QuitButton", "Quit") {{ //Button for exiting the game
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
    
    //This function returns a start Screen
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
                        control(new ButtonBuilder("JoinRed", "Join Red") {{ //Button for joining red team
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
                        control(new ButtonBuilder("JoinBlue", "Join Blue") {{ //Button for joining blue team
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
    
    //This function returns a start Screen
    public Screen createHudScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("crosshair") {{
                childLayoutCenter();
                alignCenter();
                valignCenter();
                control(new LabelBuilder("crosshair_symbol", "+"));
            }});
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder() {{
                    childLayoutHorizontal();
                    alignCenter();
                    height("8%");
                    panel(new PanelBuilder() {{ //Empty space
                        alignCenter();
                        width("40%");
                    }});
                    panel(new PanelBuilder() {{ //Red score
                        childLayoutCenter();
                        alignCenter();
                        backgroundColor("#f009");
                        width("10%");
                        height("100%");
                        control(new LabelBuilder("redscore", "  0  "));
                    }});
                   
                    panel(new PanelBuilder() {{ //Blue score
                        childLayoutCenter();
                        alignCenter();
                        backgroundColor("#00f9");
                        width("10%");
                        height("100%");
                        control(new LabelBuilder("bluescore", "  0  "));
                    }});
                }});
                panel(new PanelBuilder() {{//Empty space
                    alignCenter();
                    height("84%");
                }});
                
            }});
            layer(new LayerBuilder("chat") {{
                childLayoutVertical();
                panel(new PanelBuilder() {{//Empty space
                    height("80%");
                }});
                panel(new PanelBuilder() {{
                    childLayoutCenter();
                    height("20%");
                    width("30%");
                    backgroundColor("#1116");
                    panel(new PanelBuilder() {{
                        childLayoutVertical();
                        height("95%");
                        width("95%");
                        control(new ListBoxBuilder("chatBox") {{
                            displayItems(7);
                            selectionModeDisabled();
                            hideHorizontalScrollbar();
                            backgroundColor("#1116");
                        }});
                        control(new TextFieldBuilder("chatMessage", "") {{
                            maxLength(MAX_MESSAGE_LENGTH);
                            height("30px");
                            backgroundColor("#1116");
                        }});
                    }});
                }});
                
            }});
        }}.build(nifty);
    }
    
    public Screen createPauseScreen(final Menu menu, String screenID) {
        return new ScreenBuilder(screenID) {{
            controller(menu);
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder() {{
                    alignCenter();
                    height("30%");
                }});
                panel(new PanelBuilder() {{
                    childLayoutVertical();
                    backgroundColor("#f008");
                    alignCenter();
                    height("20%");
                    width("40%");
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        alignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("Resume", "Resume") {{
                            valignCenter();
                            alignCenter();
                            interactOnClick("resume()");
                        }});
                    }});
                    panel(new PanelBuilder() {{
                        childLayoutCenter();
                        alignCenter();
                        height("50%");
                        width("50%");
                        style("nifty-panel-red");
                        control(new ButtonBuilder("Quit", "Quit") {{
                            valignCenter();
                            alignCenter();
                            x(SizeValue.px(100));
                            y(SizeValue.px(100));

                            interactOnClick("quitServer()");
                        }});
                    }});
                }});
            }});
        }}.build(nifty);
    }

}
