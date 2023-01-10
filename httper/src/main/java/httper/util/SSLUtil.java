package httper.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtil {

    public static SSLSocketFactory allowAllSSL() {
        SSLSocketFactory factory = null;
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new EmptyX509TrustManager()}, new SecureRandom());
            factory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return factory;
    }

    public static class EmptyX509TrustManager implements X509TrustManager {

        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws java.security.cert.CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws java.security.cert.CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }

    }
}