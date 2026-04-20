package kyrie;

import jakarta.servlet.http.HttpServletRequest;

public class AppConfig {

    /**
     * Dynamically builds the base URL. Works on localhost AND Railway
     * automatically!
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();       // http or https
        String host = request.getServerName();   // localhost or railway domain
        int port = request.getServerPort();   // 8081 locally, 443 on Railway
        String context = request.getContextPath();  // /doc_ad_sys or empty

        // Don't include port for standard ports (80 = http, 443 = https)
        if ((scheme.equals("https") && port == 443)
                || (scheme.equals("http") && port == 80)) {
            return scheme + "://" + host + context + "/";
        }

        return scheme + "://" + host + ":" + port + context + "/";
    }
}
