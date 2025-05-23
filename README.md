# AgroApp - Agricultural Sensor Data Management System

AgroApp is a full-stack web application designed to monitor and analyze agricultural sensor data. The system provides real-time insights into soil conditions, enabling farmers to make data-driven decisions for optimal crop management.

## 🌾 Features

- **Real-time Sensor Monitoring**: Track humidity, nitrogen levels, and pH across multiple parcels
- **AI-Powered Insights**: Generate intelligent recommendations based on sensor data
- **Interactive Dashboard**: Modern, responsive web interface built with React
- **Multi-parcel Management**: Monitor different agricultural areas separately
- **Oracle ATP Integration**: Secure cloud database connectivity

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.3
- **Database**: Oracle Autonomous Transaction Processing (ATP)
- **ORM**: JPA/Hibernate
- **Security**: Environment-based configuration
- **API**: RESTful endpoints with CORS support

### Frontend (React)
- **Framework**: React 18 with Vite
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Build Tool**: Vite

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 16+ and npm
- Maven 3.6+
- Oracle ATP database access (optional for development)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Feko2/AgronomAI.git
   cd agroapp
   ```

2. **Backend Configuration**
   
   Create a `.env` file in the root directory or set environment variables:
   ```env
   # Database Configuration
   DB_USERNAME=ADMIN
   DB_PASSWORD=your_database_password
   DB_SERVICE_NAME=your_service_name_high
   WALLET_PASSWORD=your_wallet_password
   
   # Optional: AI Features
   OPENAI_API_KEY=your_openai_api_key
   
   # Development Settings
   SHOW_SQL=true
   FORMAT_SQL=true
   LOG_LEVEL=DEBUG
   APP_LOG_LEVEL=DEBUG
   ```

3. **Oracle Wallet Setup** (Production)
   - Place your Oracle wallet files in `src/main/resources/wallet/`
   - Ensure proper permissions and security

### Running the Application

#### Development Mode ---> ./run-all.bat dev

1. **Start the Backend**
   ```bash
   mvn spring-boot:run
   ```

2. **Start the Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. **Access the Application**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080

#### Production Build ---> ./run-all.bat prod

1. **Build Frontend**
   ```bash
   cd frontend
   npm run build
   ```

2. **Build Backend**
   ```bash
   mvn clean package
   ```

3. **Run Production**
   ```bash
   java -jar target/agroapp-0.0.1-SNAPSHOT.jar
   ```

## 📡 API Endpoints

### Sensor Data
- `GET /api/sensors` - List all sensors
- `GET /api/sensors/{id}` - Get sensor by ID
- `POST /api/sensors` - Create new sensor reading
- `PUT /api/sensors/{id}` - Update sensor reading
- `DELETE /api/sensors/{id}` - Delete sensor reading

### AI Insights
- `POST /api/insights/analyze` - Generate AI-powered insights from sensor data

### Health Check
- `GET /actuator/health` - Application health status

## 🗄️ Database Schema

### Sensors Table
```sql
CREATE TABLE sensors (
    id NUMBER PRIMARY KEY,
    parcela_id VARCHAR2(255) NOT NULL,
    humedad NUMBER(5,2),
    nitrogeno NUMBER(5,2),
    ph NUMBER(4,2),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🛠️ Development Tools

The `test-utils/` directory contains helpful utilities for database testing and setup:

- **SimpleDbTest**: Quick connection verification
- **TestOracleConnection**: Comprehensive connection diagnostics
- **Batch Scripts**: Automated testing tools

## 🔒 Security

- Environment-based configuration for sensitive data
- Oracle wallet security for database connections
- CORS configuration for frontend-backend communication
- No hardcoded credentials in source code

## 🚦 Current Status

### ✅ Implemented Features
- Basic CRUD operations for sensor data
- Responsive React frontend
- Oracle ATP database integration
- AI insights framework
- REST API with proper CORS

### 🔄 Simulated Features (Future Development)
- **AI Insights**: Currently uses mock data, integration with OpenAI API planned
- **Sensor Data**: Sample data for demonstration, real sensor integration planned
- **User Authentication**: Framework ready for implementation
- **Real-time Updates**: WebSocket support planned

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
