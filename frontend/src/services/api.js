import axios from 'axios';

const API_URL = 'http://localhost:8080/api/sensors';

export const fetchSensorData = () => axios.get(API_URL); 