package com.felipe.agroapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class OracleConfig {
    
    @Value("${WALLET_PASSWORD:}")
    private String walletPassword;
    
    @PostConstruct
    public void setOracleSystemProperties() {
        String walletPath = "src/main/resources/wallet";
        System.setProperty("oracle.net.tns_admin", walletPath);
        System.setProperty("TNS_ADMIN", walletPath);
        
        // Only set wallet password if provided
        if (walletPassword != null && !walletPassword.isEmpty()) {
            System.setProperty("WALLET_PASSWORD", walletPassword);
        }
    }
} 