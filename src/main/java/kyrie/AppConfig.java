package kyrie;

import jakarta.servlet.http.HttpServletRequest;

public class AppConfig {

    /**
     * Dynamically builds the base URL. Works on localhost AND Railway
     * automatically!
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String context = request.getContextPath();

        // ✅ Check Railway's forwarded headers first!
        // Railway sends X-Forwarded-Proto and X-Forwarded-Host
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");

        // If behind Railway's proxy, use forwarded values
        if (forwardedProto != null && !forwardedProto.isEmpty()) {
            scheme = forwardedProto;  // Will be "https" on Railway!
        }
        if (forwardedHost != null && !forwardedHost.isEmpty()) {
            host = forwardedHost;     // Will be Railway's domain!
        }

        // ✅ For https or http on standard ports — no port in URL
        if (scheme.equals("https") || port == 80 || port == 443) {
            return scheme + "://" + host + context + "/";
        }

        // ✅ For local development with non-standard ports (8080, 8081, etc.)
        return scheme + "://" + host + ":" + port + context + "/";
    }
}
