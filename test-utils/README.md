# Database Connection Test Utilities

This directory contains utilities for testing the Oracle ATP database connection. These files are particularly useful when setting up the project for the first time or troubleshooting connection issues.

## Available Test Tools

### 1. SimpleDbTest
- **File**: `SimpleDbTest.java`
- **Purpose**: Quick connection test using the application's configuration
- **Usage**: `.\db-test.bat`

### 2. TestOracleConnection
- **File**: `TestOracleConnection.java`
- **Purpose**: Comprehensive connection test with detailed diagnostics
- **Usage**: `.\test-oracle.bat`

## Prerequisites
1. Oracle Wallet files in `src/main/resources/wallet/`
2. Correct database credentials in `application.properties`
3. Maven dependencies installed

## How to Use

1. Place your Oracle Wallet files in `src/main/resources/wallet/`
2. Update the database password in `application.properties`
3. Run either test utility using their respective batch files

### Quick Test
```bash
.\db-test.bat
```

### Detailed Test
```bash
.\test-oracle.bat
```

## Common Issues

1. **Wallet Password**: Make sure the wallet password matches in:
   - `application.properties`
   - Oracle Wallet configuration

2. **File Locations**: Verify that:
   - Wallet files are in the correct directory
   - All required JKS files are present

3. **Dependencies**: Ensure all Oracle JDBC dependencies are available in your Maven local repository

## Note
These utilities are for development and testing purposes only. They should not be used in production environments. 