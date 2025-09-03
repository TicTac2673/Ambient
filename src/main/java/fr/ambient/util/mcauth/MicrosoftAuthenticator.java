package fr.ambient.util.mcauth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fr.ambient.ui.newaltmanager.AltType;
import fr.ambient.ui.newaltmanager.GuiAltManager;
import fr.ambient.ui.newaltmanager.NewAlt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MicrosoftAuthenticator {
    private final String clientId = "54fd49e4-2103-4044-9603-2b028c814ec3";
    private final String clientSecret = "";

    private String username;
    private String uuid;
    private String accessToken;

    private HttpServer currentServer;
    private int serverPort = 59125;
    private String redirectUri = "http://localhost:59125";
    private String loginUri;

    private String authCode;
    private String xboxAccessToken;
    private String xblToken;
    private String xboxUserhash;
    private String xstsToken;

    private String refreshToken;
    private boolean shouldRefreshLogin;

    private GuiAltManager parent;

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int findAvailablePort() {
        for (int port = 59125; port <= 59135; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new RuntimeException("No available ports found in range 59125-59135");
    }

    private void stopCurrentServer() {
        if (currentServer != null) {
            try {
                currentServer.stop(1);
            } catch (Exception e) {
            }
            currentServer = null;
        }
    }

    public MicrosoftAuthenticator() {
        this.shouldRefreshLogin = true;
        this.loginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId +
                "&response_type=code&redirect_uri=" + redirectUri +
                "&scope=XboxLive.signin%20offline_access&prompt=select_account";
    }

    public MicrosoftAuthenticator(GuiAltManager parent) {
        this.parent = parent;
        this.shouldRefreshLogin = true;
        this.loginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId +
                "&response_type=code&redirect_uri=" + redirectUri +
                "&scope=XboxLive.signin%20offline_access&prompt=select_account";
    }

    public MicrosoftAuthenticator(GuiAltManager parent, String clientId, String clientSecret) {
        this.parent = parent;
        this.shouldRefreshLogin = true;
        this.loginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + this.clientId +
                "&response_type=code&redirect_uri=" + redirectUri +
                "&scope=XboxLive.signin%20offline_access&prompt=select_account";
    }

    public MicrosoftAuthenticator(String clientId, String clientSecret) {
        this.shouldRefreshLogin = true;
        this.loginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + this.clientId +
                "&response_type=code&redirect_uri=" + redirectUri +
                "&scope=XboxLive.signin%20offline_access&prompt=select_account";
    }

    public void login() {
        if (shouldRefreshLogin) {
            try {
                stopCurrentServer();
                serverPort = findAvailablePort();
                redirectUri = "http://localhost:" + serverPort;

                String dynamicLoginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId +
                        "&response_type=code&redirect_uri=" + redirectUri +
                        "&scope=XboxLive.signin%20offline_access&prompt=select_account";

                currentServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
                currentServer.createContext("/", new MyHandler(currentServer, this, redirectUri));
                currentServer.setExecutor(null);
                currentServer.start();

                if (Desktop.getDesktop() != null) {
                    Desktop.getDesktop().browse(new URI(dynamicLoginUri));
                }
            } catch (IOException e) {
                return;
            } catch (URISyntaxException e) {
                return;
            }
        } else {
            getToken(false);
        }
    }

    public void loginWithoutBrowseAndCopyIntoiClip() {
        if (shouldRefreshLogin) {
            try {
                stopCurrentServer();
                serverPort = findAvailablePort();
                redirectUri = "http://localhost:" + serverPort;

                String dynamicLoginUri = "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId +
                        "&response_type=code&redirect_uri=" + redirectUri +
                        "&scope=XboxLive.signin%20offline_access&prompt=select_account";

                currentServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
                currentServer.createContext("/", new MyHandler(currentServer, this, redirectUri));
                currentServer.setExecutor(null);
                currentServer.start();

                copy(dynamicLoginUri);
            } catch (IOException e) {
                return;
            }
        } else {
            getToken(false);
        }
    }

    public void getToken(boolean freshLogin) {
        HttpPost post = new HttpPost("https://login.live.com/oauth20_token.srf");
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();

        if (freshLogin) {
            if (authCode == null || authCode.isEmpty()) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Authorization code not received", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("code", authCode));
            urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
            urlParameters.add(new BasicNameValuePair("redirect_uri", redirectUri));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                return;
            }
            post.addHeader("Content-type", "application/x-www-form-urlencoded");
            post.addHeader("Accept", "application/json");

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String json = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    if (parent != null) {
                        parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Failed to get access token", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                    }
                    return;
                }

                JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

                if (jobj.has("access_token") && !jobj.get("access_token").isJsonNull()) {
                    xboxAccessToken = jobj.get("access_token").getAsString();
                } else {
                    if (parent != null) {
                        parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "No access token received", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                    }
                    return;
                }

                if (jobj.has("refresh_token") && !jobj.get("refresh_token").isJsonNull()) {
                    refreshToken = jobj.get("refresh_token").getAsString();
                }

            } catch (IOException e) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Network error during login", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }
        } else {
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
            urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
            urlParameters.add(new BasicNameValuePair("redirect_uri", redirectUri));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                return;
            }
            post.addHeader("Content-type", "application/x-www-form-urlencoded");
            post.addHeader("Accept", "application/json");

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String json = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    return;
                }

                JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

                if (jobj.has("access_token")) {
                    xboxAccessToken = jobj.get("access_token").getAsString();
                }

            } catch (IOException e) {
                return;
            }
        }

        authXBL();
    }

    public void getToken(String token) {
        HttpPost post = new HttpPost("https://login.live.com/oauth20_token.srf");
        ArrayList<NameValuePair> urlParameters = new ArrayList<>();

        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("refresh_token", token));
        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("redirect_uri", redirectUri));

        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            return;
        }
        post.addHeader("Content-type", "application/x-www-form-urlencoded");
        post.addHeader("Accept", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj.has("access_token")) {
                xboxAccessToken = jobj.get("access_token").getAsString();
            }

        } catch (IOException e) {
            return;
        }

        authXBL();
    }

    public void authXBL() {
        if (xboxAccessToken == null || xboxAccessToken.isEmpty()) {
            return;
        }

        HttpPost post = new HttpPost("https://user.auth.xboxlive.com/user/authenticate");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");

        String payload = "{\"Properties\": {\"AuthMethod\": \"RPS\", \"SiteName\": \"user.auth.xboxlive.com\", \"RpsTicket\": \"d=" + xboxAccessToken + "\"},\"RelyingParty\": \"http://auth.xboxlive.com\", \"TokenType\": \"JWT\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Xbox Live authentication failed", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj.has("Token")) {
                xblToken = jobj.get("Token").getAsString();
            } else {
                return;
            }

            if (jobj.has("DisplayClaims")) {
                xboxUserhash = jobj.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
            } else {
                return;
            }

        } catch (IOException e) {
            return;
        }

        authXSTS();
    }

    private void authXSTS() {
        if (xblToken == null || xblToken.isEmpty()) {
            return;
        }

        HttpPost post = new HttpPost("https://xsts.auth.xboxlive.com/xsts/authorize");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");

        String payload = "{\"Properties\": {\"SandboxId\": \"RETAIL\", \"UserTokens\": [\"" + xblToken + "\"]}, \"RelyingParty\": \"rp://api.minecraftservices.com/\", \"TokenType\": \"JWT\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "XSTS authentication failed", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj.has("Token")) {
                xstsToken = jobj.get("Token").getAsString();
            } else {
                return;
            }

        } catch (IOException e) {
            return;
        }

        authMinecraft();
    }

    private void authMinecraft() {
        if (xboxUserhash == null || xstsToken == null) {
            return;
        }

        HttpPost post = new HttpPost("https://api.minecraftservices.com/authentication/login_with_xbox");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");

        String payload = "{\"identityToken\": \"XBL3.0 x=" + xboxUserhash + ";" + xstsToken + "\"}";
        StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Minecraft authentication failed", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj.has("access_token")) {
                accessToken = jobj.get("access_token").getAsString();
            } else {
                return;
            }

        } catch (IOException e) {
            return;
        }

        checkOwnership();
    }

    private void checkOwnership() {
        HttpGet get = new HttpGet("https://api.minecraftservices.com/entitlements/mcstore");
        get.setHeader("Authorization", "Bearer " + accessToken);
        get.setHeader("Accept", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Game ownership check failed", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);
            if (jobj.has("items") && jobj.get("items").getAsJsonArray().size() > 0) {
                getProfile();
            } else {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Account doesn't own Minecraft", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
            }

        } catch (IOException e) {
        }
    }

    private void getProfile() {
        if (accessToken == null || accessToken.isEmpty()) {
            return;
        }

        HttpGet get = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String json = EntityUtils.toString(response.getEntity());

            if (statusCode != 200) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Failed to get profile (Status: " + statusCode + ")", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            JsonObject jobj = new Gson().fromJson(json, JsonObject.class);

            if (jobj == null || !jobj.has("id") || !jobj.has("name")) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Invalid profile data", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            uuid = jobj.get("id").isJsonNull() ? null : jobj.get("id").getAsString();
            username = jobj.get("name").isJsonNull() ? null : jobj.get("name").getAsString();

            if (uuid == null || username == null) {
                if (parent != null) {
                    parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Profile contains null values", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
                return;
            }

            setSession();

        } catch (IOException e) {
            if (parent != null) {
                parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Network error getting profile", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
            }
        }
    }

    private void setSession() {
        if (username == null || uuid == null || accessToken == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        Session session = new Session(username, uuid, accessToken, "mojang");

        mc.setSession(session);

        if (parent != null) {
            parent.addAlt(new NewAlt(parent, session, AltType.MICROSOFT, refreshToken));
            parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Successful", "Logged in as " + username, 3000, fr.ambient.ui.newaltmanager.NotificationStatus.SUCCESS));
        }
    }

    static class MyHandler implements HttpHandler {
        private final HttpServer server;
        private final MicrosoftAuthenticator changer;
        private final String redirectUri;

        public MyHandler(HttpServer server, MicrosoftAuthenticator changer, String redirectUri) {
            this.server = server;
            this.changer = changer;
            this.redirectUri = redirectUri;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String responseURI = t.getRequestURI().toString();

            if (responseURI.contains("code=")) {
                String code = responseURI.substring(responseURI.indexOf("code=") + 5);
                if (code.contains("&")) {
                    code = code.substring(0, code.indexOf("&"));
                }

                changer.authCode = code;
                changer.redirectUri = this.redirectUri;

                String response = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Logged In</title><style>body{margin:0;display:flex;justify-content:center;align-items:center;height:100vh;background-color:#121212;color:#ffffff;font-family:Arial,sans-serif}.message{font-size:1.5rem;text-align:center;}</style></head><body><div class=\"message\">Authentication successful! You can now close this window!</div></body></html>";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        server.stop(1);
                    } catch (InterruptedException e) {
                    }
                }).start();

                new Thread(() -> {
                    try {
                        changer.getToken(true);
                    } catch (Exception e) {
                        if (changer.parent != null) {
                            changer.parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Error processing authentication", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                        }
                    }
                }).start();

            } else if (responseURI.contains("error=")) {
                String response = "<!DOCTYPE html><html><head><title>Login Failed</title></head><body><h1>Login Failed</h1><p>Error: " + responseURI + "</p></body></html>";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                server.stop(1);

                if (changer.parent != null) {
                    changer.parent.setNotification(new fr.ambient.ui.newaltmanager.AltNotification("Login Failed", "Authentication was cancelled or failed", 3000, fr.ambient.ui.newaltmanager.NotificationStatus.FAILURE));
                }
            } else {
                String response = "<!DOCTYPE html><html><head><title>Unexpected Response</title></head><body><h1>Unexpected Response</h1><p>URI: " + responseURI + "</p></body></html>";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                server.stop(1);
            }
        }
    }

    public static void copy(String data) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
    }
}