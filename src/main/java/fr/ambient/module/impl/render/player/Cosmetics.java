package fr.ambient.module.impl.render.player;

import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.RenderCapeLayerEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.CosmeticData;
import fr.ambient.util.packet.RequestUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.render.img.ImageObject;
import fr.ambient.util.wings.RenderWings;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Cosmetics extends Module {

    public  BooleanProperty waveyCapes = BooleanProperty.newInstance("Wavey Capes", true);
    public final ModeProperty mode = ModeProperty.newInstance("Cape mode", new String[]{"Ambient","Ambient1","Ambient2","2011","2012","2013","2015","2016","CherryBlossom","BadAiim","Kirby","Kirby2", "None", "Custom"}, "Ambient");

    public final ModeProperty haloMode = ModeProperty.newInstance("Halo Mode", new String[]{"None", "Custom"}, "None");
    public final NumberProperty haloScale = NumberProperty.newInstance("Halo Scale", 0.1f, 1f, 2f, 0.1f);
    public final NumberProperty haloHeight = NumberProperty.newInstance("Halo Height", 0.63f, 0.65f, 1f, 0.01f);

    public final CompositeProperty haloGroup = CompositeProperty.newInstance("Halo", new Property[]{haloMode,haloScale,haloHeight});


    public final ModeProperty wingMode = ModeProperty.newInstance("Wing Mode", new String[]{"None", "Dragon"}, "None");
    public NumberProperty gravity = NumberProperty.newInstance("Gravity", 5f, 25f, 50f, 1F, () -> waveyCapes.getValue()) ;
    public NumberProperty numIterations = NumberProperty.newInstance("Num iterations", 5f, 30f, 50f, 1F, () -> waveyCapes.getValue()) ;
    public NumberProperty maxBend = NumberProperty.newInstance("Max Bend", 5f, 6f, 50f, 1F, () -> waveyCapes.getValue()) ;

    private final HashMap<String, ImageObject> capes = new HashMap<>();
    private final HashMap<String, ImageObject> halos = new HashMap<>();
    private final Map<String, CompletableFuture<ImageObject>> pendingRequests = new ConcurrentHashMap<>();

    private ImageObject notFound = new ImageObject(new ResourceLocation("dogclient/icons/notfound.png"));

    public Cosmetics() {
        super(47,"Adds cosmetics to your player model.", ModuleCategory.RENDER);
        this.setEnabled(false);

        for(String name : mode.getValues()) {
            if(!name.equals("None") && !name.equals("Custom")){
                capes.put(name, new ImageObject(new ResourceLocation("dogclient/cape/" + name + ".png")));
            }
        }

        for(ImageObject o : capes.values()){
            o.loadAsync();
        }
        notFound.loadAsync();


        this.registerProperties(mode,haloGroup, wingMode,waveyCapes,gravity,numIterations,maxBend);
    }

    public String lastCapeMode = "";


    @SneakyThrows
    public CompletableFuture<ImageObject> getCapeLocation(AbstractClientPlayer player) {
        if (!CosmeticComponent.cosData.containsKey(player.getGameProfile().getName())) {
            return CompletableFuture.completedFuture(null);
        }

        CosmeticData cosmeticData = CosmeticComponent.cosData.get(player.getGameProfile().getName());
        String capeId = cosmeticData.getCape();

        if (capeId.equals("None")) {
            return CompletableFuture.completedFuture(null);
        }

        if (capes.containsKey(capeId) && capes.get(capeId).isLoaded) {
            return CompletableFuture.completedFuture(capes.get(capeId));
        }

        if (pendingRequests.containsKey(capeId)) {
            return pendingRequests.get(capeId);
        }

        File saveFolder = new File(Minecraft.getMinecraft().mcDataDir, "ambient/saves");
        saveFolder.mkdirs();
        File saveFile = new File(saveFolder, capeId);


//        CompletableFuture<ImageObject> future = RequestUtil.downloadAndLoad(
//                "https://legitclient.com/api/cosmetic?type=cape&id=" + capeId, saveFile // hello hi sir its cape type !
//        ).thenApply(image -> {
//            capes.put(capeId, image);
//            pendingRequests.remove(capeId);
//            return image;
//        }).exceptionally(ex -> {
//            pendingRequests.remove(capeId);
//            capes.put(capeId, notFound);
//            ChatUtil.display("Query Cape failed " + capeId);
//            System.err.println("Failed to load cape: " + ex.getMessage());
//            return null;
//        });

//        pendingRequests.put(capeId, future);
        return null;
    }

    @SneakyThrows
    public CompletableFuture<ImageObject> getHaloLocation(AbstractClientPlayer player) {
        if (!CosmeticComponent.cosData.containsKey(player.getGameProfile().getName())) {
            return CompletableFuture.completedFuture(null);
        }

        CosmeticData cosmeticData = CosmeticComponent.cosData.get(player.getGameProfile().getName());
        String haloId = cosmeticData.getHalo();

        if (haloId.equals("None")) {
            return CompletableFuture.completedFuture(null);
        }

        if (halos.containsKey(haloId) && halos.get(haloId).isLoaded) {
            return CompletableFuture.completedFuture(halos.get(haloId));
        }

        if (pendingRequests.containsKey(haloId)) {
            return pendingRequests.get(haloId);
        }

        File saveFolder = new File(Minecraft.getMinecraft().mcDataDir, "ambient/saves");
        saveFolder.mkdirs();
        File saveFile = new File(saveFolder, haloId);


//        CompletableFuture<ImageObject> future = RequestUtil.downloadAndLoad(
//                "https://legitclient.com/api/cosmetic?type=halo&id=" + haloId, saveFile // hello hi sir its cape type !
//        ).thenApply(image -> {
//            halos.put(haloId, image);
//            pendingRequests.remove(haloId);
//            return image;
//        }).exceptionally(ex -> {
//            pendingRequests.remove(haloId);
//            halos.put(haloId, notFound);
//            ChatUtil.display("Query Halo failed " + haloId);
//            System.err.println("Failed to load cape: " + ex.getMessage());
//            return null;
//        });

//        pendingRequests.put(haloId, future);
        return null;
    }


    public String getHaloMode(){
        if(this.isEnabled()){
            if(haloMode.is("Custom")){
                return CosmeticComponent.customHaloId;
            }
            return haloMode.getValue();
        }
        return "None";
    }

    public String getCapeMode(){
        if(this.isEnabled()){
            if(mode.is("Custom")){
                return CosmeticComponent.customCapeId;
            }
            return mode.getValue();
        }
        return "None";
    }
    @SubscribeEvent
    private void onCapeRender(RenderCapeLayerEvent event){
        if (!CosmeticComponent.cosData.containsKey(event.getEntityPlayer().getGameProfile().getName())) {
            return;
        }
        if(!CosmeticComponent.cosData.get(event.getEntityPlayer().getGameProfile().getName()).getWing().equals("None")){
            drawWings(event);
        }
    }

    public void drawWings(RenderCapeLayerEvent event){
        if(!event.getEntityPlayer().isInvisible()){
            RenderWings renderWings = new RenderWings();
            renderWings.renderWings(event.getEntityPlayer(), mc.timer.renderPartialTicks);
        }
    }
    public String getCurrMode(){
        if(this.isEnabled() && !wingMode.is("None")){
            return "on";
        }else{
            return "none";
        }
    }


}
