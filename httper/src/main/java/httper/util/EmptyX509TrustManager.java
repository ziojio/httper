package httper.util;

import android.annotation.SuppressLint;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

@SuppressLint("CustomX509TrustManager")
public class EmptyX509TrustManager implements X509TrustManager {

    private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return _AcceptedIssuers;
    }

}

