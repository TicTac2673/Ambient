package fr.ambient;

import cc.polymorphism.annot.IncludeReference;
import cc.polymorphismj2c.annot.Native;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viamcp.ViaMCP;
import fr.ambient.anticheat.AnticheatManager;
import fr.ambient.command.CommandManager;
import fr.ambient.command.impl.*;
import fr.ambient.command.irc.IRC;
import fr.ambient.component.Component;
import fr.ambient.component.ComponentManager;
import fr.ambient.component.impl.misc.AltsComponent;
import fr.ambient.component.impl.misc.BreakerWhitelistComponent;
import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.component.impl.packet.AntiBadPacketComponent;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.component.impl.packet.OutgoingPacketComponent;
import fr.ambient.component.impl.packet.PacketOrderComponent;
import fr.ambient.component.impl.player.ClickPatternComponent;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.component.impl.player.RotationComponent;
import fr.ambient.component.impl.player.RotationPatternComponent;
import fr.ambient.component.impl.ui.ItemRenderComponent;
import fr.ambient.config.ConfigManager;
import fr.ambient.module.ModuleManager;
import fr.ambient.module.impl.combat.*;
import fr.ambient.module.impl.misc.*;
import fr.ambient.module.impl.skyblock.AutoGift;
import fr.ambient.module.impl.movement.*;
import fr.ambient.module.impl.player.*;
import fr.ambient.module.impl.render.*;
import fr.ambient.module.impl.render.hud.*;
import fr.ambient.module.impl.render.misc.Animations;
import fr.ambient.module.impl.render.misc.Camera;
import fr.ambient.module.impl.render.misc.Indicators;
import fr.ambient.module.impl.render.player.*;
import fr.ambient.module.impl.render.widgets.ArmorDisplay;
import fr.ambient.module.impl.render.widgets.Effects;
import fr.ambient.module.impl.render.widgets.ImageRender;
import fr.ambient.module.impl.render.widgets.InventoryDisplay;
import fr.ambient.module.impl.render.world.Ambience;
import fr.ambient.module.impl.render.world.Breadcrumbs;
import fr.ambient.module.impl.render.world.BreakProgress;
import fr.ambient.module.impl.render.world.KillEffects;
import fr.ambient.module.impl.skyblock.Test;
import fr.ambient.theme.ThemeManager;
import fr.ambient.ui.mainmenu.wouf.WoufMainMenuScreen;
import lombok.SneakyThrows;
import org.lwjglx.opengl.Display;

@Native
@IncludeReference
public class ProtectedLaunch {
    public static Cosmetics CUSTOM_CAPE;

    @SneakyThrows
    public static void init(String uid) throws Throwable {
        Ambient.getInstance().getConfig().load();

        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        ViaMCP.INSTANCE.getAsyncVersionSlider().setVersion(ProtocolVersion.v1_8.getVersion());

        if (Ambient.getInstance().getConfig().getValue("discord-rp").equals("true")) {
            Ambient.getInstance().getDiscordRP().start();
        }


        // TODO: CHANGE TO FALSE BEFORE BUILD
        final boolean developmentSwitch = true;

        System.err.println("DONE ????");

        CUSTOM_CAPE = new Cosmetics();

        { // Modules
            ModuleManager moduleManager = new ModuleManager();

            moduleManager.register(
                    // Combat
                    new AimAssist(),
                    new AntiBot(),
                    new AutoClicker(),
                    new AutoGoldenHead(),
                    new BackTrack(),
                    new Criticals(),
                    new FakeLag(),
                    new KeepSprint(),
                    new KillAura(),
                    new Reach(),
                    new SumoBot(),
                    new TargetStrafe(),
                    new TickBase(),
                    new Velocity(),
                    new WTap(),

                    // Movement
                    new Flight(),
                    new InvMove(),
                    new Jesus(),
                    new LongJump(),
                    new NoSlowdown(),
                    new QuickStop(),
                    new SafeWalk(),
                    new SaveMoveKey(),
                    new Speed(),
                    new Spider(),
                    new Sprint(),
                    new Step(),

                    // Player
                    new AntiFireBall(),
                    new AntiVoid(),
                    new AutoBed(),
                    new AutoPot(),
                    new AutoSoup(),
                    new AutoTool(),
                    new Blink(),
                    new Breaker(),
                    new ChestStealer(),
                    new DelayRemover(),
                    new FastBreak(),
                    new FastPlace(),
                    new InvManager(),
                    new LegitScaffold(),
                    new NoFall(),
                    new NoRotate(),
                    new Phase(),
                    new Refill(),
                    new Scaffold(),
                    new Timer(),


                    // Render
                    new Ambience(),
                    new Animations(),
                    new ArmorDisplay(),
                    new Breadcrumbs(),
                    new BreakProgress(),
                    new Camera(),
                    new Chams(),
                    new Chat(),
                    CUSTOM_CAPE,
                    new ClickGui(),
                    new Effects(),
                    new ESP(),
                    new HUD(),
                    new HurtColor(),
                    new ImageRender(),
                    new Indicators(),
                    new InventoryDisplay(),
                    new KillEffects(),
                    new NameTags(),
                    new Notification(),
                    new Overlay(),
                    new PlayerRender(),
                    new PostProcessing(),
                    new Scoreboard(),
                    new SessionStats(),
                    new TargetHUD(),


                    // Exploits
                    new AutoDisable(),
                    new Anticheat(),
                    new AutoPlay(),
                    new BedwarsUtils(),
                    new ClientSpoofer(),
                    new ChatBypass(),
                    new Crasher(),
                    new Disabler(),
                    new FastUse(),
                    new Freelook(),
                    new MCF(),
                    new NickHider(),
                    new NoDisconnect(),
                    new Regen(),
                    new Respawn(),
                    new SmoothReset(),
                    new TransactionLogger()
            );


            if (Ambient.getInstance().getConfig().getValue("skyblock").equals("true")) {

                System.out.println("Enabled SkyBlock Addons !");

                // register all ig

                moduleManager.register(new AutoGift());
                moduleManager.register(new Test());
            }

            Ambient.getInstance().getEventBus().register(moduleManager);
            Ambient.getInstance().setModuleManager(moduleManager);
            Ambient.getInstance().setHud(moduleManager.getModule(HUD.class));
        }

        { // Commands
            CommandManager commandManager = new CommandManager();

            commandManager.register(new BindCommand(), new ConfigCommand(), new ToggleCommand(), new ThemeCommand(),
                    new QueueCommand(), new HypixelAPICommand(), new HelpCommand(), new KeyConfigCommand(),
                    new CustomNameCommand(), new IgnCommand(), new HideCommand(), new ClipboardCommand(),
                    new ClickGuiStuck(), new ReloadCommand(), new ScriptCommand(),
                    new ClickPatternCommand(), new PartyCommand(), new RotationPatternCommand(), new HClipCommand(), new ESPCommand(),
                    new CosmeticCommand());

            Ambient.getInstance().getEventBus().register(commandManager);
            Ambient.getInstance().setCommandManager(commandManager);
        }

        { // Components
            ComponentManager componentManager = new ComponentManager();
            Ambient.getInstance().setRotationComponent(new RotationComponent());
            Ambient.getInstance().setRotationPatternComponent(new RotationPatternComponent());
            Ambient.getInstance().setOutgoingPacketComponent(new OutgoingPacketComponent());
            Ambient.getInstance().setAltsComponent(new AltsComponent());
            componentManager.register(Ambient.getInstance().getRotationComponent(), Ambient.getInstance().getOutgoingPacketComponent(), Ambient.getInstance().getRotationPatternComponent(), Ambient.getInstance().getAltsComponent(), new BlinkComponent(), new HypixelComponent(),
                    new PacketOrderComponent(), new CosmeticComponent(), new BreakerWhitelistComponent(), new ItemRenderComponent(), new AntiBadPacketComponent());

            for (Component component : componentManager.getObjects()) {
                Ambient.getInstance().getEventBus().register(component);
            }

            Ambient.getInstance().setComponentManager(componentManager);
        }

        Ambient.getInstance().setConfigManager(new ConfigManager());

        Ambient.getInstance().getConfigManager().init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ambient.getInstance().getConfigManager().stop();
            //todo close backend here
        }));

        Ambient.getInstance().setCheckManager(new AnticheatManager());
        Ambient.getInstance().setThemeManager(new ThemeManager());
        Ambient.getInstance().setIrc(new IRC());
        Display.setTitle("Ambient " + Ambient.getInstance().getVersion() + " | " + Ambient.getInstance().getUsername() + " [" + Ambient.getInstance().getUid() + "]");
        HypixelComponent.loadCheaters();
        HUD hud = Ambient.getInstance().getModuleManager().getModule(HUD.class);
        hud.setEnabled(true);

        //Ambient.getInstance().getBackend().sendEncrypted("config:" + Ambient.getInstance().getToken() + ":load:default");

        Ambient.getInstance().getModuleManager().getModule(Scoreboard.class).setEnabled(true);
        Ambient.getInstance().getModuleManager().getModule(Chat.class).setEnabled(true);

        Ambient.getInstance().getCustomThemeManager().load();
        Ambient.getInstance().getCustomThemeManager().save();

        Ambient.getInstance().setClickPatternComponent(new ClickPatternComponent());

        Ambient.getInstance().setMainMenuScreen(new WoufMainMenuScreen());


    }
}